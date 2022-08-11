package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.Abi
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import java.io.File

abstract class AbstractSoMappingTask(objects: ObjectFactory) : DefaultTask() {

    @get:Input
    val forceLegacyMapping: Property<Boolean> = objects.property<Boolean>().convention(true)

    @get:Input
    abstract val objDumpOverrides: MapProperty<String, String>

    @get:Input
    abstract val ndkDirectory: Property<File>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    protected val ndkToolchain by lazy {
        NdkToolchain(ndkDirectory.get(), objDumpOverrides.get().mapKeys { Abi.findByName(it.key)!! })
    }

    protected open fun objcopy(inputFile: File, abi: Abi): ProcessBuilder {
        return ProcessBuilder(
            ndkToolchain.objcopy.path,
            "--compress-debug-sections=zlib",
            "--only-keep-debug",
            inputFile.path,
            "-" // output to stdout
        )
    }

    protected open fun objdump(inputFile: File, abi: Abi): ProcessBuilder {
        val objdump = ndkToolchain.objdumpForAbi(abi).path
        return ProcessBuilder(
            objdump,
            "--dwarf=info",
            "--dwarf=rawline",
            inputFile.path
        )
    }

    fun generateMappingFile(soFile: File, abi: Abi): File? {
        try {
            val process =
                if (ndkToolchain.isLLVM() && !forceLegacyMapping.get()) objcopy(soFile, abi)
                else objdump(soFile, abi)

            val dst = outputFileFor(soFile, abi)
            makeSoMappingFile(dst, process)

            return dst
        } catch (e: Exception) {
            logger.error("Bugsnag: failed to generate symbols for $abi ${e.message}", e)
        }

        return null
    }

    private fun makeSoMappingFile(dst: File, processBuilder: ProcessBuilder) {
        // ensure any errors are dumped to stderr
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
        val process = processBuilder.start()
        outputZipFile(process.inputStream, dst)

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalStateException(
                "Failed to generate symbols for $dst, objdump exited with code $exitCode"
            )
        }
    }

    protected open fun outputFileFor(soFile: File, abi: Abi): File {
        return File(prepareArchDirectory(abi), "${soFile.name}.gz")
    }

    private fun prepareArchDirectory(abi: Abi): File {
        val rootDir = outputDirectory.get().asFile
        return File(rootDir, abi.abiName).apply {
            if (!isDirectory) {
                mkdir()
            }
        }
    }
}
