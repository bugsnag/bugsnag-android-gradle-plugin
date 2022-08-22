package com.bugsnag.android.gradle.internal

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.bugsnag.android.gradle.Abi
import com.bugsnag.android.gradle.BugsnagPluginExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.StopExecutionException
import org.gradle.util.VersionNumber
import java.io.File

private val osName = when {
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

abstract class NdkToolchain {
    @get:Input
    abstract val baseDir: Property<File>

    @get:Input
    abstract val useLegacyNdkSymbolUpload: Property<Boolean>

    @get:Input
    abstract val overrides: MapProperty<Abi, String>

    fun preferredMappingTool(): MappingTool {
        // useLegacyNdkSymbolUpload force overrides any defaults or options
        if (useLegacyNdkSymbolUpload.get()) {
            return MappingTool.OBJDUMP
        }

        val ndkVersion = version.get()
        return when {
            ndkVersion >= VersionNumber.version(23, 0) -> MappingTool.OBJCOPY
            else -> MappingTool.OBJDUMP
        }
    }

    /**
     * Set all the fields of this `NdkToolchain` based on the given [other] `NdkToolchain`
     */
    fun configureWith(other: NdkToolchain) {
        baseDir.set(other.baseDir)
        overrides.set(other.overrides)
        useLegacyNdkSymbolUpload.set(other.useLegacyNdkSymbolUpload)
    }

    private fun executableName(cmdName: String): String {
        return if (osName?.startsWith("windows") == true) "$cmdName.exe" else cmdName
    }

    fun objdumpForAbi(abi: Abi): File {
        val objdumpOverrides = overrides.get()

        return objdumpOverrides[abi]?.let { File(it) } ?: File(
            baseDir.get(),
            "toolchains/${abi.toolchainPrefix}-4.9/prebuilt/" +
                "$osName/bin/${abi.objdumpPrefix}-${executableName("objdump")}"
        )
    }

    fun objcopyForAbi(abi: Abi): File {
        val objdumpOverrides = overrides.get()

        return objdumpOverrides[abi]?.let { File(it) } ?: File(
            baseDir.get(),
            "toolchains/llvm/prebuilt/$osName/bin/${executableName("llvm-objcopy")}"
        )
    }

    enum class MappingTool {
        OBJDUMP,
        OBJCOPY
    }

    companion object {
        /*
         * SdkComponents.ndkDirectory
         * https://developer.android.com/reference/tools/gradle-api/7.2/com/android/build/api/dsl/SdkComponents#ndkDirectory()
         * sometimes fails to resolve when ndkPath is not defined (Cannot query the value of this property because it has
         * no value available.). This means that even `map` and `isPresent` will break.
         *
         * So we also fall back use the old BaseExtension if it appears broken
         */
        private fun ndkToolchainDirectoryFor(project: Project): Provider<File> {
            val extensions = project.extensions
            val sdkComponents = extensions.getByType(AndroidComponentsExtension::class.java)?.sdkComponents

            return project.provider {
                try {
                    return@provider sdkComponents!!.ndkDirectory.get().asFile
                } catch (e: Exception) {
                    return@provider extensions.getByType(BaseExtension::class.java).ndkDirectory.absoluteFile
                }
            }
        }

        private fun isLegacyMappingRequired(variant: ApkVariant): Boolean? {
            val bugsnagAndroidCoreVersion = variant.compileConfiguration.allDependencyConstraints
                .find { it.group == "com.bugsnag" && it.name == "bugsnag-plugin-android-ndk" }
                ?.version
                ?: return null

            return VersionNumber.parse(bugsnagAndroidCoreVersion) < VersionNumber.version(5, 26)
        }

        fun configureNdkToolkit(
            project: Project,
            bugsnag: BugsnagPluginExtension,
            variant: ApkVariant
        ): NdkToolchain {
            val useLegacyNdkSymbolUpload = bugsnag.useLegacyNdkSymbolUpload.get()
            var legacyUploadRequired = isLegacyMappingRequired(variant)

            if (legacyUploadRequired == null) {
                project.logger.warn(
                    "Cannot detect Bugsnag SDK version for variant ${variant.name}, assuming a modern version is " +
                        "being used. This can cause problems with NDK symbols if older versions are being used. " +
                        "Please either specify the Bugsnag SDK version for ${variant.name} directly." +
                        "See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
                )

                legacyUploadRequired = false
            }

            if (!useLegacyNdkSymbolUpload && legacyUploadRequired) {
                throw StopExecutionException(
                    "Your Bugsnag SDK configured for variant ${variant.name} does not support the new NDK " +
                        "symbols upload mechanism. Please set legacyNDKSymbolsUpload or upgrade your " +
                        "Bugsnag SDK. See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
                )
            }

            val overrides = bugsnag.objdumpPaths.map { it.mapKeys { (abi, _) -> Abi.findByName(abi)!! } }
            val ndkToolchain = project.objects.newInstance<NdkToolchain>()
            ndkToolchain.baseDir.set(ndkToolchainDirectoryFor(project))
            ndkToolchain.useLegacyNdkSymbolUpload.set(useLegacyNdkSymbolUpload)
            ndkToolchain.overrides.set(overrides)

            return ndkToolchain
        }
    }
}

val NdkToolchain.version get() = baseDir.map { VersionNumber.parse(it.name) }
