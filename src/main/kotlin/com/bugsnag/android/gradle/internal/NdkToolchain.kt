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
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.StopExecutionException
import org.gradle.util.VersionNumber
import java.io.File

abstract class NdkToolchain {
    @get:Input
    abstract val baseDir: Property<File>

    @get:Input
    abstract val useLegacyNdkSymbolUpload: Property<Boolean>

    @get:Input
    abstract val overrides: MapProperty<Abi, String>

    @get:Input
    @get:Optional
    abstract val bugsnagNdkVersion: Property<String>

    fun preferredMappingTool(): MappingTool {
        var legacyUploadRequired = bugsnagNdkVersion.orNull
            ?.let { VersionNumber.parse(it) }
            ?.let { it < MIN_BUGSNAG_ANDROID_VERSION }
        if (legacyUploadRequired == null) {
//            logger.warn(
//                "Cannot detect Bugsnag SDK version for variant ${variant.name}, assuming a modern version is " +
//                    "being used. This can cause problems with NDK symbols if older versions are being used. " +
//                    "Please either specify the Bugsnag SDK version for ${variant.name} directly." +
//                    "See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
//            )

            legacyUploadRequired = false
        }

        if (!useLegacyNdkSymbolUpload.get() && legacyUploadRequired) {
            throw StopExecutionException(
                "Your Bugsnag SDK configured for variant does not support the new NDK " +
                    "symbols upload mechanism. Please set legacyNDKSymbolsUpload or upgrade your " +
                    "Bugsnag SDK. See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
            )
//            throw StopExecutionException(
//                "Your Bugsnag SDK configured for variant ${variant.name} does not support the new NDK " +
//                    "symbols upload mechanism. Please set legacyNDKSymbolsUpload or upgrade your " +
//                    "Bugsnag SDK. See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
//            )
        }

        // useLegacyNdkSymbolUpload force overrides any defaults or options
        if (useLegacyNdkSymbolUpload.get()) {
            return MappingTool.OBJDUMP
        }

        val ndkVersion = version.get()
        return when {
            ndkVersion >= MIN_NDK_OBJCOPY_VERSION -> MappingTool.OBJCOPY
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
        bugsnagNdkVersion.set(other.bugsnagNdkVersion)
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
        val objcopyOverrides = overrides.get()

        return objcopyOverrides[abi]?.let { File(it) }
            ?: locateObjcopy(abi)
    }

    private fun locateObjcopy(abi: Abi): File {
        val relativeExecutablePath = when {
            isLLVMPreferred.get() -> "toolchains/llvm/prebuilt/$osName/bin/${executableName("llvm-objcopy")}"
            else ->
                "toolchains/${abi.toolchainPrefix}-4.9/prebuilt/" +
                    "$osName/bin/${abi.objdumpPrefix}-${executableName("objcopy")}"
        }

        return File(baseDir.get(), relativeExecutablePath)
    }

    enum class MappingTool {
        OBJDUMP,
        OBJCOPY
    }

    companion object {
        /**
         * Minimum `bugsnag-android` version where the new symbol uploading is available, using `objcopy` to produce
         * the symbol files instead of `objdump`
         */
        internal val MIN_BUGSNAG_ANDROID_VERSION = VersionNumber.version(5, 26)

        /**
         * The minimum NDK version where we will use `objcopy` instead of `objdump` to produce the symbol files
         */
        internal val MIN_NDK_OBJCOPY_VERSION = VersionNumber.version(21)

        /**
         * The minimum NDK version where we will use the LLVM toolchain instead of the GNU toolchain
         */
        internal val MIN_NDK_LLVM_VERSION = VersionNumber.version(23)

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

        private fun getBugsnagAndroidNDKVersion(variant: ApkVariant): String? {
            try {
                val bugsnagAndroidCoreVersion = variant.compileConfiguration.resolvedConfiguration.resolvedArtifacts
                    .find {
                        it.moduleVersion.id.group == "com.bugsnag" &&
                            it.moduleVersion.id.name == "bugsnag-plugin-android-ndk"
                    }
                    ?.moduleVersion?.id?.version
                    ?: return null

                return bugsnagAndroidCoreVersion
            } catch (e: Exception) {
                return null
            }
        }

        fun configureNdkToolkit(
            project: Project,
            bugsnag: BugsnagPluginExtension,
            variant: ApkVariant
        ): NdkToolchain {
            val useLegacyNdkSymbolUpload = bugsnag.useLegacyNdkSymbolUpload.get()
            val overrides = bugsnag.objdumpPaths.map { it.mapKeys { (abi, _) -> Abi.findByName(abi)!! } }

            val ndkToolchain = project.objects.newInstance<NdkToolchain>()
            ndkToolchain.baseDir.set(ndkToolchainDirectoryFor(project))
            ndkToolchain.useLegacyNdkSymbolUpload.set(useLegacyNdkSymbolUpload)
            ndkToolchain.overrides.set(overrides)
            ndkToolchain.bugsnagNdkVersion.set(project.provider { getBugsnagAndroidNDKVersion(variant) })

            return ndkToolchain
        }
    }
}

private val NdkToolchain.version get() = baseDir.map { VersionNumber.parse(it.name) }
private val NdkToolchain.isLLVMPreferred get() = version.map { it >= NdkToolchain.MIN_NDK_LLVM_VERSION }
