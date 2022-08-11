package com.bugsnag.android.gradle.internal

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.bugsnag.android.gradle.Abi
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.util.VersionNumber
import java.io.File

/*
 * SdkComponents.ndkDirectory
 * https://developer.android.com/reference/tools/gradle-api/7.2/com/android/build/api/dsl/SdkComponents#ndkDirectory()
 * sometimes fails to resolve when ndkPath is not defined (Cannot query the value of this property because it has
 * no value available.). This means that even `map` and `isPresent` will break.
 *
 * So we also fall back use the old BaseExtension if it appears broken
 */
val Project.ndkToolchain: Provider<File>
    get() {
        val sdkComponents = extensions.getByType(AndroidComponentsExtension::class.java)?.sdkComponents

        return provider {
            try {
                return@provider sdkComponents!!.ndkDirectory.get().asFile
            } catch (e: Exception) {
                return@provider extensions.getByType(BaseExtension::class.java).ndkDirectory.absoluteFile
            }
        }
    }

val osName = when {
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

class NdkToolchain(
    val baseDir: File,
    private val objdumpOverrides: Map<Abi, String>
) {
    val version = VersionNumber.parse(baseDir.name)
    val objcopy: File = File(baseDir, "toolchains/llvm/prebuilt/$osName/bin/${executableName("llvm-objcopy")}")

    fun isLLVM(): Boolean = version >= VersionNumber.version(23, 0)

    private fun executableName(cmdName: String): String {
        return if (osName?.startsWith("windows") == true) "$cmdName.exe" else cmdName
    }

    fun objdumpForAbi(abi: Abi): File {
        return objdumpOverrides[abi]?.let { File(it) } ?: File(
            baseDir,
            "toolchains/${abi.toolchainPrefix}-4.9/prebuilt/" +
                "$osName/bin/${abi.objdumpPrefix}-${executableName("objdump")}"
        )
    }
}
