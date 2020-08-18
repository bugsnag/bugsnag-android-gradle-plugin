package com.bugsnag.android.gradle

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.Abi.Companion.findByName
import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.md5HashCode
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import okhttp3.RequestBody.Companion.asRequestBody
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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.io.InputStream
import java.io.Reader
import javax.inject.Inject

/**
 * Task to upload shared object mapping files to Bugsnag.
 *
 * Reads meta-data tags from the project's AndroidManifest.xml to extract a
 * build UUID (injected by BugsnagManifestTask) and a Bugsnag API Key:
 *
 * https://developer.android.com/guide/topics/manifest/manifest-intro.html
 * https://developer.android.com/guide/topics/manifest/meta-data-element.html
 *
 * This task must be called after shared object files are generated, so
 * it is usually safe to have this be the absolute last task executed during
 * a build.
 */
sealed class BugsnagUploadNdkTask(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), AndroidManifestInfoReceiver, BugsnagFileUploadTask {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    @get:Internal
    internal val uploadRequestClient: Property<UploadRequestClient> = objects.property()

    @get:Internal
    override val httpClientHelper: Property<BugsnagHttpClientHelper> = objects.property()

    @Input
    val projectRoot: Property<String> = objects.property()

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir("intermediates/bugsnag"))

    @get:Input
    override val failOnUploadError: Property<Boolean> = objects.property()

    @get:Input
    override val overwrite: Property<Boolean> = objects.property()

    @get:Input
    override val endpoint: Property<String> = objects.property()

    @get:Input
    override val retryCount: Property<Int> = objects.property()

    @get:Input
    override val timeoutMillis: Property<Long> = objects.property()

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:InputFiles
    abstract val searchDirectories: ConfigurableFileCollection

    @TaskAction
    fun upload() {
        logger.lifecycle("Starting ndk upload")
        val searchDirs = searchDirectories.files.toList()
        val files = findSharedObjectMappingFiles(variantOutput, searchDirs)
        logger.lifecycle("Processing shared object files")
        processFiles(files)
        requestOutputFile.asFile.get().writeText("OK")
    }

    private fun findSharedObjectMappingFiles(
        variantOutput: ApkVariantOutput,
        searchDirectories: List<File>
    ): Collection<File> {
        val splitArch = variantOutput.getFilter(VariantOutput.FilterType.ABI)
        return searchDirectories.flatMap { findSharedObjectFiles(it, splitArch) }
            // sort SO files alphabetically by architecture for consistent request order
            .toSortedSet(compareBy { it.parentFile.name })
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
            processFile(file)
        }
    }

    private fun processFile(sharedObject: File) {
        val arch = sharedObject.parentFile.name
        val outputFile = generateSymbolsForSharedObject(sharedObject, arch)
        if (outputFile != null) {
            uploadSymbols(outputFile, arch, sharedObject.name)
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
                val outputDir = intermediateOutputDir.asFile.get()
                val outputFile = File(outputDir, "$arch.gz")
                val errorOutputFile = File(outputDir, "$arch.error.txt")
                logger.info("Bugsnag: Creating symbol file  for $arch at $outputFile")

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
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     * @param arch the arch that is being uploaded
     * @param sharedObjectName the original shared object name
     */
    private fun uploadSymbols(mappingFile: File, arch: String?, sharedObjectName: String?) {
        // a SO file may not contain debug info. if that's the case then the mapping file should be very small,
        // so we try and reject it here as otherwise the event-worker will reject it with a 400 status code.
        if (!mappingFile.exists() || mappingFile.length() < VALID_SO_FILE_THRESHOLD) {
            logger.warn("Bugsnag: Skipping upload of empty/invalid mapping file: $mappingFile")
            return
        }

        val request = BugsnagMultiPartUploadRequest.from(this)
        logger.lifecycle("Bugsnag: Attempting to upload shared object mapping " +
            "file for $sharedObjectName-$arch from $mappingFile")

        val manifestInfo = parseManifestInfo()
        val mappingFileHash = mappingFile.md5HashCode()
        val response = uploadRequestClient.get().makeRequestIfNeeded(manifestInfo, mappingFileHash) {
            logger.lifecycle("Bugsnag: Attempting to upload shared object mapping " +
                "file for $sharedObjectName-$arch from $mappingFile")
            request.uploadMultipartEntity(parseManifestInfo()) { builder ->
                builder.addFormDataPart("soSymbolFile", mappingFile.name, mappingFile.asRequestBody())

                if (arch != null) {
                    builder.addFormDataPart("arch", arch)
                }
                if (sharedObjectName != null) {
                    builder.addFormDataPart("sharedObjectName", sharedObjectName)
                }
                builder.addFormDataPart("projectRoot", projectRoot.get())
            }
        }
        requestOutputFile.asFile.get().writeText(response)
        logger.lifecycle("Bugsnag: shared object mapping file complete for $mappingFile")
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
        private const val VALID_SO_FILE_THRESHOLD = 1024

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
            return File("$ndkDir/toolchains/${abi.toolchainPrefix}-4.9/prebuilt/$osName/bin/${abi.objdumpPrefix}-$executable")
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
            configurationAction: BugsnagUploadNdkTask.() -> Unit
        ): TaskProvider<out BugsnagUploadNdkTask> {
            val gradleVersion = project.gradle.versionNumber()
            return when {
                gradleVersion >= GradleVersions.VERSION_5_3 -> {
                    project.tasks.register<BugsnagUploadNdkTask53Plus>(name, configurationAction)
                }
                else -> {
                    project.tasks.register<BugsnagUploadNdkTaskLegacy>(name, configurationAction)
                }
            }
        }
    }
}

/** A legacy [BugsnagUploadNdkTask] that uses [ProjectLayout.configurableFiles]. */
internal open class BugsnagUploadNdkTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagUploadNdkTask(objects, projectLayout) {
    @Suppress("DEPRECATION") // Here for backward compat
    @get:InputFiles
    override val searchDirectories: ConfigurableFileCollection = projectLayout.configurableFiles()
}

internal open class BugsnagUploadNdkTask53Plus @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagUploadNdkTask(objects, projectLayout) {
    @get:InputFiles
    override val searchDirectories: ConfigurableFileCollection = objects.fileCollection()
}
