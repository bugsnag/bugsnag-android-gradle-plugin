package com.bugsnag.android.gradle

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.Abi.Companion.findByName
import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.io.InputStream
import java.io.Reader
import javax.inject.Inject

/**
 * Task that generates shared object mapping files for upload to Bugsnag.
 */
sealed class BugsnagGenerateNdkSoMappingTask(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates NDK mapping files for upload to Bugsnag"
    }

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(SO_MAPPING_DIR))

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:InputFiles
    abstract val searchDirectories: ConfigurableFileCollection

    @TaskAction
    fun generateMappingFiles() {
        logger.lifecycle("Generating NDK mapping files")
        val searchDirs = searchDirectories.files.toList()
        val files = findSharedObjectMappingFiles(variantOutput, searchDirs)
        processFiles(files)
    }

    private fun findSharedObjectMappingFiles(
        variantOutput: ApkVariantOutput,
        searchDirectories: List<File>
    ): Collection<File> {
        val splitArch = variantOutput.getFilter(VariantOutput.FilterType.ABI)
        return searchDirectories.flatMap { findSharedObjectFiles(it, splitArch) }
            .toSortedSet(compareBy { it.absolutePath })
    }

    /**
     * Searches the subdirectories of a given path for SO files. These are added to a
     * collection and returned if they should be uploaded by the current task.
     *
     * If the variantOutput is an APK split the splitArch parameter should be non-null,
     * as this allows the avoidance of unnecessary uploads of all architectures for each split.
     *
     * @param searchDirectory The parent path to search. Each subdirectory should
     * represent an architecture
     * @param abiArchitecture The architecture of the ABI split, or null if this is not an APK split.
     */
    private fun findSharedObjectFiles(
        searchDirectory: File,
        abiArchitecture: String?
    ): Collection<File> {
        return if (searchDirectory.exists() && searchDirectory.isDirectory) {
            searchDirectory.walkTopDown()
                .onEnter { archDir -> abiArchitecture == null || archDir.name == abiArchitecture }
                .filter { file -> file.extension == "so" }
                .toSet()
        } else {
            emptySet()
        }
    }

    private fun processFiles(files: Collection<File>) {
        logger.info("Bugsnag: Found shared object files for upload: $files")

        files.forEach { file ->
            val arch = file.parentFile.name
            val outputFile = generateSymbolsForSharedObject(file, arch)
            if (outputFile != null) {
                logger.info("Bugsnag: Created symbol file for $arch at $outputFile")
            }
        }
    }

    /**
     * Uses objdump to create a symbols file for the given shared object file
     * @param sharedObject the shared object file
     * @param arch the arch of the file
     * @return the output file location, or null on error
     */
    private fun generateSymbolsForSharedObject(sharedObject: File, arch: String): File? {
        // Get the path the version of objdump to use to get symbols
        val objDumpPath = getObjDumpExecutable(arch)
        val logger = logger
        if (objDumpPath != null) {
            val outReader: Reader? = null
            try {
                val rootDir = intermediateOutputDir.asFile.get()
                val archDir = File(rootDir, arch)
                archDir.mkdir()

                val outputFile = File(archDir, "${sharedObject.name}.gz")
                val errorOutputFile = File(archDir, "${sharedObject.name}.error.txt")
                logger.info("Bugsnag: Creating symbol file for ${sharedObject.name} at $outputFile")

                // Call objdump, redirecting output to the output file
                val builder = ProcessBuilder(objDumpPath.toString(),
                    "--dwarf=info", "--dwarf=rawline", sharedObject.toString())
                builder.redirectError(errorOutputFile)
                val process = builder.start()

                // Output the file to a zip
                val stdout = process.inputStream
                outputZipFile(stdout, outputFile)
                return if (process.waitFor() == 0) {
                    outputFile
                } else {
                    logger.error("Bugsnag: failed to generate symbols for " + arch + " see "
                        + errorOutputFile.toString() + " for more details")
                    null
                }
            } catch (e: Exception) {
                logger.error("Bugsnag: failed to generate symbols for " + arch + " " + e.message, e)
            } finally {
                outReader?.close()
            }
        } else {
            logger.error("Bugsnag: Unable to upload NDK symbols: Could not find objdump location for $arch")
        }
        return null
    }

    /**
     * Gets the path to the objdump executable to use to get symbols from a shared object
     * @param arch The arch of the shared object
     * @return The objdump executable, or null if not found
     */
    private fun getObjDumpExecutable(arch: String): File? {
        try {
            val override = getObjDumpOverride(arch)
            val objDumpFile: File
            objDumpFile = override?.let { File(it) } ?: findObjDump(project, arch)
            check((objDumpFile.exists() && objDumpFile.canExecute())) {
                "Failed to find executable objdump at $objDumpFile"
            }
            return objDumpFile
        } catch (ex: Throwable) {
            logger.error("Bugsnag: Error attempting to calculate objdump location: " + ex.message)
        }
        return null
    }

    private fun getObjDumpOverride(arch: String): String? {
        return objDumpPaths.get()[arch]
    }

    companion object {
        internal const val SO_MAPPING_DIR = "intermediates/bugsnag/soMappings"

        /**
         * Outputs the contents of stdout into the gzip file output file
         *
         * @param stdout The input stream
         * @param outputFile The output file
         */
        private fun outputZipFile(stdout: InputStream, outputFile: File) {
            stdout.source().use { source ->
                outputFile.sink().gzip().buffer().use { gzipSink ->
                    gzipSink.writeAll(source)
                }
            }
        }

        private fun findObjDump(project: Project, arch: String): File {
            val abi = findByName(arch)
            val android = project.extensions.getByType(AppExtension::class.java)
            val ndkDir = android.ndkDirectory.absolutePath
            val osName = calculateOsName()
            checkNotNull(abi) { "Failed to find ABI for $arch" }
            checkNotNull(osName) { "Failed to calculate OS name" }
            return calculateObjDumpLocation(ndkDir, abi, osName)
        }

        @JvmStatic
        fun calculateObjDumpLocation(ndkDir: String?, abi: Abi, osName: String): File {
            val executable = if (osName.startsWith("windows")) "objdump.exe" else "objdump"
            return File("$ndkDir/toolchains/${abi.toolchainPrefix}-4.9/prebuilt/" +
                "$osName/bin/${abi.objdumpPrefix}-$executable")
        }

        private fun calculateOsName(): String? {
            return when {
                Os.isFamily(Os.FAMILY_MAC) -> "darwin-x86_64"
                Os.isFamily(Os.FAMILY_UNIX) -> "linux-x86_64"
                Os.isFamily(Os.FAMILY_WINDOWS) -> {
                    if ("x86" == System.getProperty("os.arch")) "windows" else "windows-x86_64"
                }
                else -> null
            }
        }

        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagGenerateNdkSoMappingTask.() -> Unit
        ): TaskProvider<out BugsnagGenerateNdkSoMappingTask> {
            val gradleVersion = project.gradle.versionNumber()
            return when {
                gradleVersion >= GradleVersions.VERSION_5_3 -> {
                    project.tasks.register<BugsnagGenerateNdkSoMappingTask53Plus>(name, configurationAction)
                }
                else -> {
                    project.tasks.register<BugsnagGenerateNdkSoMappingTaskLegacy>(name, configurationAction)
                }
            }
        }
    }
}

/** A legacy [BugsnagGenerateNdkSoMappingTask] that uses [ProjectLayout.configurableFiles]. */
internal open class BugsnagGenerateNdkSoMappingTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagGenerateNdkSoMappingTask(objects, projectLayout) {
    @Suppress("DEPRECATION") // Here for backward compat
    @get:InputFiles
    override val searchDirectories: ConfigurableFileCollection = projectLayout.configurableFiles()
}

internal open class BugsnagGenerateNdkSoMappingTask53Plus @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagGenerateNdkSoMappingTask(objects, projectLayout) {
    @get:InputFiles
    override val searchDirectories: ConfigurableFileCollection = objects.fileCollection()
}
