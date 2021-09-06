package com.bugsnag.android.gradle

import com.android.build.gradle.BaseExtension
import com.bugsnag.android.gradle.SharedObjectMappingFileFactory.SharedObjectType.NDK
import com.bugsnag.android.gradle.SharedObjectMappingFileFactory.SharedObjectType.UNITY
import com.bugsnag.android.gradle.internal.outputZipFile
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

/**
 * Generates a mapping file for the supplied shared object file.
 *
 * Currently this only supports NDK SO mapping files but in future this will support
 * other platforms which require different SO mapping support.
 */
internal object SharedObjectMappingFileFactory {

    enum class SharedObjectType {
        NDK,
        UNITY
    }

    internal data class Params(
        val sharedObject: File,
        val abi: Abi,
        val objDumpPaths: Map<String, String>,
        val outputDirectory: File,
        val sharedObjectType: SharedObjectType = NDK
    )

    /**
     * Uses objdump to create a symbols file for the given shared object file.
     *
     * @param project the gradle project
     * @param params the parameters required to generate a SO mapping file
     * @return the output file location, or null on error
     */
    fun generateSoMappingFile(project: Project, params: Params): File? {
        // Get the path the version of objdump to use to get symbols
        val arch = params.abi.abiName
        val objDumpPath = getObjDumpExecutable(project, params.objDumpPaths, arch)
        val logger = project.logger

        if (objDumpPath == null) {
            logger.error("Bugsnag: Unable to upload NDK symbols: Could not find objdump location for $arch")
            return null
        }

        try {
            val archDir = prepareArchDirectory(params, arch)
            val sharedObjectName = params.sharedObject.name
            val dst = File(archDir, "$sharedObjectName.gz")
            val processBuilder = getObjDumpCommand(objDumpPath, params)
            logger.info(
                "Bugsnag: Creating symbol file for $sharedObjectName at $dst," +
                    "running ${processBuilder.command()}"
            )
            makeSoMappingFile(dst, processBuilder)
            return dst
        } catch (e: Exception) {
            logger.error("Bugsnag: failed to generate symbols for $arch ${e.message}", e)
        }
        return null
    }

    /**
     * Gets the command used to generate the SO mapping file with objdump.
     * This differs for NDK and Unity SO files.
     */
    private fun getObjDumpCommand(objDumpPath: File, params: Params): ProcessBuilder {
        val soPath = params.sharedObject.path
        val objdump = objDumpPath.path
        return when (params.sharedObjectType) {
            NDK -> ProcessBuilder(objdump, "--dwarf=info", "--dwarf=rawline", soPath)
            UNITY -> ProcessBuilder(objdump, "--sym", soPath)
        }
    }

    private fun makeSoMappingFile(dst: File, processBuilder: ProcessBuilder) {
        // ensure any errors are dumped to stderr
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
        val process = processBuilder.start()
        outputZipFile(process.inputStream, dst)

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalStateException(
                "Failed to generate symbols for $dst," +
                    " objdump exited with code $exitCode"
            )
        }
    }

    private fun prepareArchDirectory(params: Params, arch: String): File {
        val rootDir = params.outputDirectory
        return File(rootDir, arch).apply {
            mkdir()
        }
    }

    /**
     * Gets the path to the objdump executable to use to get symbols from a shared object
     * @param arch The arch of the shared object
     * @return The objdump executable, or null if not found
     */
    private fun getObjDumpExecutable(project: Project, objDumpPaths: Map<String, String>, arch: String): File? {
        try {
            val override = getObjDumpOverride(objDumpPaths, arch)
            val objDumpFile: File
            objDumpFile = override?.let { File(it) } ?: findObjDump(project, arch)
            check((objDumpFile.exists() && objDumpFile.canExecute())) {
                "Failed to find executable objdump at $objDumpFile"
            }
            return objDumpFile
        } catch (ex: Throwable) {
            project.logger.error("Bugsnag: Error attempting to calculate objdump location: " + ex.message)
        }
        return null
    }

    private fun getObjDumpOverride(objDumpPaths: Map<String, String>, arch: String) = objDumpPaths[arch]

    private fun findObjDump(project: Project, arch: String): File {
        val abi = Abi.findByName(arch)
        val android = project.extensions.getByType(BaseExtension::class.java)
        val ndkDir = android.ndkDirectory.absolutePath
        val osName = calculateOsName()
        checkNotNull(abi) { "Failed to find ABI for $arch" }
        checkNotNull(osName) { "Failed to calculate OS name" }
        return calculateObjDumpLocation(ndkDir, abi, osName)
    }

    @JvmStatic
    fun calculateObjDumpLocation(ndkDir: String?, abi: Abi, osName: String): File {
        val executable = if (osName.startsWith("windows")) "objdump.exe" else "objdump"
        return File(
            "$ndkDir/toolchains/${abi.toolchainPrefix}-4.9/prebuilt/" +
                "$osName/bin/${abi.objdumpPrefix}-$executable"
        )
    }

    private fun calculateOsName(): String? {
        return when {
            Os.isFamily(Os.FAMILY_MAC) -> "darwin-x86_64"
            Os.isFamily(Os.FAMILY_UNIX) -> "linux-x86_64"
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                when {
                    "x86" == System.getProperty("os.arch") -> "windows"
                    else -> "windows-x86_64"
                }
            }
            else -> null
        }
    }
}
