package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.android.gradle.Abi.Companion.findByName
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.util.zip.GZIPOutputStream

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
open class BugsnagUploadNdkTask : DefaultTask() {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    private var symbolPath: File? = null
    lateinit var variantName: String
    lateinit var projectDir: File
    lateinit var rootDir: File
    var sharedObjectPath: String? = null
    lateinit var variantOutput: ApkVariantOutput
    lateinit var variant: ApkVariant
    lateinit var manifestInfoProvider: Property<AndroidManifestInfo>

    @TaskAction
    fun upload() {
        symbolPath = findSymbolPath(variantOutput)
        if (symbolPath == null) {
            return
        }
        val logger = project.logger
        logger.lifecycle("Symbolpath: $symbolPath")
        val soFiles = mutableSetOf<Pair<File, String>>()
        resolveExternalNativeBuildTasks().forEach { task ->
            val objFolder = task.objFolder
            val soFolder = task.soFolder
            soFiles.addAll(findSharedObjectFiles(objFolder))
            soFiles.addAll(findSharedObjectFiles(soFolder))
        }
        if (sharedObjectPath != null) {
            val file = File(projectDir.path, sharedObjectPath!!)
            soFiles.addAll(findSharedObjectFiles(file))
        }

        // sort SO files alphabetically by architecture for consistent request order
        val files = soFiles.toList().sortedBy { it.second }
        processFiles(files)
    }

    private fun processFiles(files: Collection<Pair<File, String>?>) {
        for (pair in files) {
            processFile(pair!!.second, pair.first)
        }
    }

    private fun processFile(arch: String, sharedObject: File) {
        project.logger.lifecycle("Found shared object file $arch $sharedObject")
        val outputFile = generateSymbolsForSharedObject(sharedObject, arch)
        if (outputFile != null) {
            uploadSymbols(outputFile, arch, sharedObject.name)
        }
    }

    private fun resolveExternalNativeBuildTasks(): Collection<ExternalNativeBuildTask> {
        return variant.externalNativeBuildProviders.mapNotNull { it.orNull }
    }

    /**
     * Searches the subdirectories of a given path and executes a block on
     * any shared object files
     * @param dir The parent path to search. Each subdirectory should
     * represent an architecture
     */
    private fun findSharedObjectFiles(dir: File): Collection<Pair<File, String>> {
        project.logger.lifecycle("Checking dir: $dir")
        val sharedObjectFiles = mutableSetOf<Pair<File, String>>()
        if (dir.exists()) {
            for (arch in dir.listFiles()) {
                for (file in arch.listFiles()) {
                    if (file.name.endsWith(".so")) {
                        sharedObjectFiles.add(Pair(file, arch.name))
                    }
                }
            }
        }
        return sharedObjectFiles
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
        val logger = project.logger
        if (objDumpPath != null) {
            val outReader: Reader? = null
            try {
                val outputDir = File(project.buildDir, "bugsnag")
                if (!outputDir.exists()) {
                    outputDir.mkdir()
                }
                val outputFile = File(outputDir, "$arch.gz")
                val errorOutputFile = File(outputDir, "$arch.error.txt")
                logger.lifecycle("Creating symbol file at \${outputFile}")

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
                    logger.error("failed to generate symbols for " + arch + " see "
                        + errorOutputFile.toString() + " for more details")
                    null
                }
            } catch (e: Exception) {
                logger.error("failed to generate symbols for " + arch + " " + e.message, e)
            } finally {
                outReader?.close()
            }
        } else {
            logger.error("Unable to upload NDK symbols: Could not find objdump location for $arch")
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
            project.logger.warn("Skipping upload of empty/invalid mapping file: $mappingFile")
            return
        }
        val mpEntity = MultipartEntity()
        mpEntity.addPart("soSymbolFile", FileBody(mappingFile))
        mpEntity.addPart("arch", StringBody(arch))
        mpEntity.addPart("sharedObjectName", StringBody(sharedObjectName))
        val bugsnag = project.extensions.getByType(BugsnagPluginExtension::class.java)
        var projectRoot = bugsnag.projectRoot
        if (projectRoot == null) {
            projectRoot = projectDir.toString()
        }
        mpEntity.addPart("projectRoot", StringBody(projectRoot))
        val request = BugsnagMultiPartUploadRequest()
        request.variant = variant
        request.variantOutput = variantOutput
        request.uploadMultipartEntity(project, mpEntity, manifestInfoProvider.get())
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
            check(!(!objDumpFile.exists() || !objDumpFile.canExecute())) { "Failed to find executable objdump at $objDumpFile" }
            return objDumpFile
        } catch (ex: Throwable) {
            project.logger.error("Error attempting to calculate objdump location: " + ex.message)
        }
        return null
    }

    private fun getObjDumpOverride(arch: String): String? {
        val bugsnag = project.extensions.getByType(BugsnagPluginExtension::class.java)
        val paths = bugsnag.objdumpPaths
        return paths?.get(arch)
    }

    companion object {
        private const val VALID_SO_FILE_THRESHOLD = 1024
        private fun findSymbolPath(variantOutput: ApkVariantOutput?): File? {
            val resources = variantOutput!!.processResourcesProvider.orNull ?: return null
            return resources.property("textSymbolOutputFile") as File
                ?: throw IllegalStateException("Could not find symbol path")
        }

        /**
         * Outputs the contents of stdout into the gzip file output file
         *
         * @param stdout The input stream
         * @param outputFile The output file
         */
        private fun outputZipFile(stdout: InputStream, outputFile: File?) {
            var zipStream: GZIPOutputStream? = null
            try {
                zipStream = GZIPOutputStream(FileOutputStream(outputFile))
                val buffer = ByteArray(8192)
                var len: Int
                while (stdout.read(buffer).also { len = it } != -1) {
                    zipStream.write(buffer, 0, len)
                }
            } finally {
                zipStream?.close()
                stdout.close()
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
        private fun calculateObjDumpLocation(ndkDir: String?, abi: Abi, osName: String): File {
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
    }
}
