package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.SharedObjectMappingFileFactory.UNITY_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.register
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject

/**
 * Task that generates Unity shared object mapping files for upload to Bugsnag.
 */
internal open class BugsnagGenerateUnitySoMappingTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates Unity mapping files for upload to Bugsnag"
    }

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(UNITY_SO_MAPPING_DIR))

    @get:OutputDirectory
    val unitySharedObjectDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(UNITY_SO_COPY_DIR))

    @get:InputDirectory
    val rootProjectDir: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun generateMappingFiles() {
        logger.lifecycle("Generating Unity mapping files")

        // TODO search Unity internal build too
        val symbolArchives = getUnitySymbolArchives(rootProjectDir)
        logger.info("Found symbol archives: $symbolArchives")

        if (symbolArchives.isEmpty()) {
            logger.warn("Bugsnag did not find symbols.zip which is required to fully symbolicate " +
                "Unity stackframes. Please ensure that symbols.zip generation is enabled in build " +
                "settings and that it hasn't been removed from the filesystem. See " +
                "https://docs.unity3d.com/ScriptReference/EditorUserBuildSettings" +
                "-androidCreateSymbolsZip.html")
        }

        symbolArchives.forEach { archive ->
            val copyDir = unitySharedObjectDir.asFile.get()
            copyDir.mkdirs()
            val zipFile = ZipFile(archive)
            val entries = zipFile.entries()

            // extract SO files from archive and generate mapping files for each
            entries.asSequence()
                .filter(::isUnitySharedObjectFile)
                .map { zipEntry ->
                    extractSoFileFromArchive(zipEntry, copyDir, zipFile)
                }
                .forEach { sharedObjectFile ->
                    generateUnitySoMappingFile(sharedObjectFile)
                }
        }

        // TODO check ABI and avoid unnecessary generation
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

    private fun extractSoFileFromArchive(
        entry: ZipEntry,
        copyDir: File,
        zipFile: ZipFile
    ): File {
        val entryFile = File(entry.name)
        val sharedObjectName = entryFile.name
        val arch = entryFile.parentFile.name
        val archDir = File(copyDir, arch)
        archDir.mkdir()
        val dst = File(archDir, sharedObjectName)
        logger.lifecycle("Copying zip entry $entry to $dst")

        // copy entry to intermediate dir
        zipFile.getInputStream(entry).use { sharedObjectStream ->
            val source = sharedObjectStream.source().buffer()
            val sink = dst.sink()
            source.readAll(sink)
        }
        return dst
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

        /**
         * Intermediate path where libunity and other Unity SO files are copied
         * after being extracted from the Gzip archive
         */
        private const val UNITY_SO_COPY_DIR = "intermediates/bugsnag/unitySoFiles"

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

        internal fun isUnitySharedObjectFile(entry: ZipEntry): Boolean {
            val name = entry.name
            val extensionMatch = name.endsWith(".sym.so") || name.endsWith(".sym")
            val nameMatch = name.contains("libunity") || name.contains("libil2cpp")
            return extensionMatch && nameMatch
        }
    }
}
