package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.BugsnagUploadNdkTask.Companion.calculateObjDumpLocation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.ArrayList

@RunWith(Parameterized::class)
class ObjDumpLocationTest {

    @Parameterized.Parameter(0)
    lateinit var abi: Abi

    @Parameterized.Parameter(1)
    lateinit var osName: String

    @Parameterized.Parameter(2)
    lateinit var ndkDir: String

    @Test
    fun testDefaultObjDumpLocation() {
        val file = calculateObjDumpLocation(ndkDir, abi, osName)
        val exec = when {
            osName.startsWith("windows") -> "objdump.exe"
            else -> "objdump"
        }
        val expected = "$ndkDir/toolchains/${abi.toolchainPrefix}-4.9/prebuilt/$osName/bin/${abi.objdumpPrefix}-$exec"
        assertEquals(expected, file.path)
    }

    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun inputs(): Collection<Array<Any>> {
            val inputs: MutableCollection<Array<Any>> = ArrayList()
            for (abi in Abi.values()) {
                for (os in listOf("darwin-x86_64", "linux-x86_64", "windows", "windows-x86_64")) {
                    for (ndkDir in listOf("/Users/bob/Library/Android/sdk/ndk-bundle", "/etc/ndk-bundle")) {
                        inputs.add(listOf(abi, os, ndkDir).toTypedArray())
                    }
                }
            }
            return inputs
        }

        @Parameterized.Parameters
        @JvmStatic
        fun os(): Collection<String> = listOf("windows", "linux")
    }
}
