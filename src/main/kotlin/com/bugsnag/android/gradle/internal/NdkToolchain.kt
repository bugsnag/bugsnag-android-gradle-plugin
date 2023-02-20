package com.bugsnag.android.gradle.internal

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariant
import com.bugsnag.android.gradle.Abi
import com.bugsnag.android.gradle.BugsnagGenerateUnitySoMappingTask
import com.bugsnag.android.gradle.BugsnagPluginExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.StopExecutionException
import org.semver.Version
import java.io.File

abstract class NdkToolchain {
    // Internal rather than InputDirectory because this is an
    // absolute path otherwise and would break build caching.
    @get:Internal
    abstract val baseDir: DirectoryProperty

    @get:Input
    abstract val useLegacyNdkSymbolUpload: Property<Boolean>

    @get:Input
    abstract val overrides: MapProperty<Abi, String>

    @get:Input
    @get:Optional
    abstract val bugsnagNdkVersion: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    private val logger: Logger = Logging.getLogger(this::class.java)

    fun preferredMappingTool(): MappingTool {
        var legacyUploadRequired = bugsnagNdkVersion.orNull
            ?.let { Version.parse(it) }
            ?.let { it < MIN_BUGSNAG_ANDROID_VERSION }
        if (legacyUploadRequired == null) {
            logger.warn(
                "Cannot detect Bugsnag SDK version for variant ${variantName.get()}. To use the new upload " +
                    "functionality, version 5.26.0 of bugsnag-android is required. If this is not the case, " +
                    "please either upgrade the dependency or set bugsnag.useLegacyNdkSymbolUpload to use the legacy " +
                    "upload mechanism. See https://docs.bugsnag.com/build-integrations/gradle/#ndk-symbol-files " +
                    "for details."
            )

            legacyUploadRequired = false
        }

        if (!useLegacyNdkSymbolUpload.get() && legacyUploadRequired) {
            throw StopExecutionException(
                "Your Bugsnag SDK configured for variant ${variantName.get()} does not support the new NDK " +
                    "symbols upload mechanism. Please set legacyNDKSymbolsUpload or upgrade your " +
                    "Bugsnag SDK. See https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/ for details."
            )
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
        variantName.set(other.variantName)
    }

    private fun executableName(cmdName: String): String {
        return if (osName?.startsWith("windows") == true) "$cmdName.exe" else cmdName
    }

    fun objdumpForAbi(abi: Abi): File {
        val objdumpOverrides = overrides.get()

        return objdumpOverrides[abi]?.let { File(it) } ?: File(
            baseDir.asFile.get(),
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

        return File(baseDir.asFile.get(), relativeExecutablePath)
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
        internal val MIN_BUGSNAG_ANDROID_VERSION = Version(5, 26, 0)

        /**
         * The minimum NDK version where we will use `objcopy` instead of `objdump` to produce the symbol files
         */
        internal val MIN_NDK_OBJCOPY_VERSION = Version(21, 0, 0)

        /**
         * The minimum NDK version where we will use the LLVM toolchain instead of the GNU toolchain
         */
        internal val MIN_NDK_LLVM_VERSION = Version(23, 0, 0)

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

        private fun getBugsnagAndroidNDKVersion(variant: ApkVariant): String? {
            return try {
                val bugsnagAndroidCoreVersion = variant.compileConfiguration.resolvedConfiguration.resolvedArtifacts
                    .find {
                        it.moduleVersion.id.group == "com.bugsnag" &&
                            it.moduleVersion.id.name == "bugsnag-plugin-android-ndk"
                    }
                    ?.moduleVersion?.id?.version

                bugsnagAndroidCoreVersion
            } catch (e: Exception) {
                null
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
            ndkToolchain.baseDir.set(project.extensions.getByType(BaseExtension::class.java).ndkDirectory)
            ndkToolchain.useLegacyNdkSymbolUpload.set(useLegacyNdkSymbolUpload)
            ndkToolchain.overrides.set(overrides)

            // we disable the bugsnag-android version check if Unity is enabled otherwise we end up with mutation errors
            if (!BugsnagGenerateUnitySoMappingTask
                .isUnityLibraryUploadEnabled(bugsnag, project.extensions.findByType(BaseExtension::class.java)!!)
            ) {
                ndkToolchain.bugsnagNdkVersion.set(project.provider { getBugsnagAndroidNDKVersion(variant) })
            }
            ndkToolchain.variantName.set(variant.name)

            return ndkToolchain
        }
    }
}

private val NdkToolchain.version get() = baseDir.map { Version.parse(it.asFile.name) }
private val NdkToolchain.isLLVMPreferred get() = version.map { it >= NdkToolchain.MIN_NDK_LLVM_VERSION }
