package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.internal.clearDir
import com.bugsnag.android.gradle.internal.includesAbi
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

/**
 * Task that generates Unity shared object mapping files for upload to Bugsnag.
 */
internal open class BugsnagGenerateUnitySoMappingTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates Unity mapping files for upload to Bugsnag"
    }

    @get:Input
    override val manifestInfo: Property<AndroidManifestInfo> = objects.property()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()

    @get:OutputDirectory
    val unitySharedObjectDir: DirectoryProperty = objects.directoryProperty()

    @get:Internal
    val rootProjectDir: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun generateMappingFiles() {
        logger.lifecycle("Generating Unity mapping files")
        // search the internal Gradle build + exported Gradle build locations
        val symbolArchives = getUnitySymbolArchives(rootProjectDir)
        val copyDir = unitySharedObjectDir.asFile.get()
        val outputDir = intermediateOutputDir.asFile.get()
        copyDir.clearDir()
        outputDir.clearDir()

        val sharedObjectFiles = copySoFilesFromBuildDir(copyDir).toMutableList()

        if (symbolArchives.isEmpty() && sharedObjectFiles.isEmpty()) {
            logger.warn(
                "Bugsnag did not find any Unity SO files in Temp/StagingArea/symbols," +
                    "or a symbols.zip. At least one of these options is required to fully symbolicate " +
                    "Unity stackframes. Please ensure that symbols.zip generation is enabled in build " +
                    "settings and that it hasn't been removed from the filesystem. See " +
                    "https://docs.unity3d.com/ScriptReference/EditorUserBuildSettings" +
                    "-androidCreateSymbolsZip.html"
            )
            return
        }

        sharedObjectFiles.addAll(extractSoFilesFromGzipArchive(symbolArchives, copyDir))
        logger.info("Extracted Unity SO files: $sharedObjectFiles")

        // generate mapping files for each SO file
        sharedObjectFiles.forEach { sharedObjectFile ->
            generateUnitySoMappingFile(sharedObjectFile)
        }
    }

    /**
     * Extracts the libunity/libil2cpp SO files from inside a GZIP archive,
     * which is where the files are located for exported Gradle projects
     */
    private fun extractSoFilesFromGzipArchive(symbolArchives: List<File>, copyDir: File): List<File> {
        copyDir.mkdirs()
        return symbolArchives.flatMap { archive ->
            val zipFile = ZipFile(archive)
            val entries = zipFile.entries()

            // extract SO files from archive
            entries.toList()
                .filter { isUnitySharedObjectFile(it.name) }
                .mapNotNull { entry ->
                    val src = zipFile.getInputStream(entry).source().buffer()
                    copySoFile(File(entry.name), copyDir, src)
                }
        }
    }

    /**
     * Extracts the libunity/libil2cpp SO files from inside /Temp/StagingArea.
     * Unity projects using Gradle output build artefacts to /Temp/gradleOut
     * (when not exported), so the SO files can be found by traversing the filesystem.
     */
    private fun copySoFilesFromBuildDir(copyDir: File): List<File> {
        val unityExportDir = rootProjectDir.asFile.get().parentFile
        val stagingAreaDir = File(unityExportDir, "StagingArea")
        val unity2018LibDir = File(stagingAreaDir, "symbols")

        val soFiles = unity2018LibDir.walkTopDown()
            .filter { isUnitySharedObjectFile(it.name) }
            .toMutableList()

        // copy file to intermediates/bugsnag
        return soFiles.mapNotNull { sharedObjectFile ->
            val src = sharedObjectFile.source().buffer()
            copySoFile(sharedObjectFile, copyDir, src)
        }
    }

    private fun copySoFile(sharedObjectFile: File, copyDir: File, src: BufferedSource): File? {
        // append .so extension if not already present (required for pipeline symbolication)
        val sharedObjectName = when (sharedObjectFile.extension) {
            "so" -> sharedObjectFile.name
            else -> "${sharedObjectFile.name}.so"
        }
        val arch = sharedObjectFile.parentFile.name

        // avoid generating unnecessary symbols
        if (!variantOutput.includesAbi(arch)) {
            return null
        }

        val archDir = File(copyDir, arch)
        archDir.mkdir()
        val dst = File(archDir, sharedObjectName)
        logger.info("Copying entry $sharedObjectName to $dst")

        // copy entry to intermediate dir
        src.use {
            val sink = dst.sink()
            it.readAll(sink)
        }
        return dst
    }

    private fun generateUnitySoMappingFile(sharedObjectFile: File) {
        val arch = sharedObjectFile.parentFile.name
        val params = SharedObjectMappingFileFactory.Params(
            sharedObjectFile,
            requireNotNull(Abi.findByName(arch)),
            objDumpPaths.get(),
            intermediateOutputDir.get().asFile,
            SharedObjectMappingFileFactory.SharedObjectType.UNITY
        )
        SharedObjectMappingFileFactory.generateSoMappingFile(project, params)
    }

    /**
     * The directory below the exported symbols. When Unity exports a project to an Android Gradle project
     * the symbols are exported as an archive in the same directory.
     *
     * Unity 2018 exports to <output-dir>/<project-name>/<rootProjectDir>, whereas Unity 2019 exports
     * to <output-dir>/<rootProjectDir>. As there's no way of distinguishing the two via Gradle
     * both locations are searched.
     */
    private fun getUnitySymbolArchives(rootProjectDir: DirectoryProperty): List<File> {
        val unity2019ProjectDir = rootProjectDir.get().asFile
        val unity2018ProjectDir = unity2019ProjectDir.parentFile
        val files = mutableListOf<File>()
        files.addAll(getUnitySymbolArchive(unity2019ProjectDir))
        files.addAll(getUnitySymbolArchive(unity2018ProjectDir))
        return files
    }

    private fun getUnitySymbolArchive(projectDir: File): List<File> {
        val exportDir = projectDir.parentFile

        return exportDir.listFiles()?.filter { file ->
            val name = file.name
            val projectName = projectDir.name
            isUnitySymbolsArchive(name, projectName)
        } ?: emptyList()
    }

    companion object {

        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagGenerateUnitySoMappingTask.() -> Unit
        ): TaskProvider<out BugsnagGenerateUnitySoMappingTask> {
            return project.tasks.register(name, configurationAction)
        }

        internal fun isUnitySymbolsArchive(name: String, projectName: String): Boolean {
            return name.endsWith("symbols.zip") && name.startsWith(projectName)
        }

        internal fun isUnitySharedObjectFile(name: String): Boolean {
            val extensionMatch = name.endsWith(".sym.so") || name.endsWith(".sym")
            val nameMatch = name.contains("libunity") || name.contains("libil2cpp")
            return extensionMatch && nameMatch
        }
    }
}
