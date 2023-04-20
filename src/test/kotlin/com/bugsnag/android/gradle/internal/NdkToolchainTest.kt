package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.Abi
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.io.File
import org.mockito.Mockito.`when` as whenMock

class NdkToolchainTest {
    private var ndkDir: File? = null

    @After
    fun cleanupNdkDir() {
        ndkDir?.deleteRecursively()
    }

    @Test
    fun ndk19() {
        val ndkDir = setupFakeNdk(19, 2, 5345600)
        val toolchain = TestNdkToolchainImpl(ndkDir, true)
        assertEquals(NdkToolchain.MappingTool.OBJDUMP, toolchain.preferredMappingTool())
    }

    @Test
    fun ndk21Legacy() {
        val ndkDir = setupFakeNdk(21, 1, 6352462)
        val toolchain = TestNdkToolchainImpl(ndkDir, true)
        assertEquals(NdkToolchain.MappingTool.OBJDUMP, toolchain.preferredMappingTool())
    }

    @Test
    fun ndk21() {
        val ndkDir = setupFakeNdk(21, 1, 6352462)
        val toolchain = TestNdkToolchainImpl(ndkDir, false)
        assertEquals(NdkToolchain.MappingTool.OBJCOPY, toolchain.preferredMappingTool())

        val objcopyPath = toolchain.objcopyForAbi(Abi.ARM64_V8A).toString()
        assertTrue(
            "expected GNU objcopy path, but got: $objcopyPath",
            objcopyPath.contains("/aarch64-linux-android-objcopy")
        )
    }

    @Test
    fun ndk23() {
        val ndkDir = setupFakeNdk(23, 0, 7599858)
        val toolchain = TestNdkToolchainImpl(ndkDir, false)
        assertEquals(NdkToolchain.MappingTool.OBJCOPY, toolchain.preferredMappingTool())

        val objcopyPath = toolchain.objcopyForAbi(Abi.ARM64_V8A).toString()
        assertTrue(
            "expected LLVM objcopy, but got: $objcopyPath",
            objcopyPath.contains("/llvm-objcopy")
        )
    }

    @Test
    fun objcopyOverrides() {
        val ndkDir = setupFakeNdk(23, 0, 7599858)
        val toolchain = TestNdkToolchainImpl(
            ndkDir, false, mapOf(Abi.ARM64_V8A to "arm64-objcopy")
        )

        assertEquals(NdkToolchain.MappingTool.OBJCOPY, toolchain.preferredMappingTool())

        assertEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.ARM64_V8A).toString())
        assertNotEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.X86).toString())
        assertNotEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.X86_64).toString())
        assertNotEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.ARMEABI).toString())
    }

    @Test
    fun objdumpOverrides() {
        val ndkDir = setupFakeNdk(19, 2, 5345600)
        val toolchain = TestNdkToolchainImpl(
            ndkDir, true, mapOf(Abi.ARM64_V8A to "arm64-objcopy")
        )

        assertEquals(NdkToolchain.MappingTool.OBJDUMP, toolchain.preferredMappingTool())

        assertEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.ARM64_V8A).toString())
        assertNotEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.X86).toString())
        assertNotEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.X86_64).toString())
        assertNotEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.ARMEABI).toString())
    }

    fun setupFakeNdk(major: Int, minor: Int, micro: Int): File {
        val newNdkDir = File(System.getProperty("java.io.tmpdir"), "ndk$major.$minor.$micro")
        newNdkDir.mkdirs()

        val packageXml = File(newNdkDir, "package.xml")
        packageXml.writeText(
            """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <ns2:repository xmlns:ns2="http://schemas.android.com/repository/android/common/02" xmlns:ns3="http://schemas.android.com/repository/android/common/01" xmlns:ns4="http://schemas.android.com/repository/android/generic/01" xmlns:ns5="http://schemas.android.com/repository/android/generic/02" xmlns:ns6="http://schemas.android.com/sdk/android/repo/addon2/01" xmlns:ns7="http://schemas.android.com/sdk/android/repo/addon2/02" xmlns:ns8="http://schemas.android.com/sdk/android/repo/repository2/01" xmlns:ns9="http://schemas.android.com/sdk/android/repo/repository2/02" xmlns:ns10="http://schemas.android.com/sdk/android/repo/sys-img2/02" xmlns:ns11="http://schemas.android.com/sdk/android/repo/sys-img2/01">
            <license id="android-sdk-license" type="text"/>
            <localPackage path="ndk;23.1.7779620" obsolete="false"><type-details xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns5:genericDetailsType"/>
                <revision><major>$major</major><minor>$minor</minor><micro>$micro</micro></revision>
                <display-name>NDK (Side by side) $major.$minor.$micro</display-name>
                <uses-license ref="android-sdk-license"/>
                <dependencies><dependency path="patcher;v4"/></dependencies>
            </localPackage>
            </ns2:repository>
            """.trimIndent()
        )

        ndkDir = newNdkDir
        return newNdkDir
    }
}

private class TestNdkToolchainImpl(
    baseDir: File,
    useLegacyNdkSymbolUpload: Boolean?,
    overrides: Map<Abi, String> = emptyMap()
) : NdkToolchain() {
    override val baseDir: DirectoryProperty = Mockito.mock(DirectoryProperty::class.java)
    override val useLegacyNdkSymbolUpload: Property<Boolean> = Mockito.mock(Property::class.java) as Property<Boolean>
    override val overrides: MapProperty<Abi, String> = Mockito.mock(MapProperty::class.java) as MapProperty<Abi, String>
    override val bugsnagNdkVersion: Property<String> = Mockito.mock(Property::class.java) as Property<String>
    override val variantName: Property<String> = Mockito.mock(Property::class.java) as Property<String>

    init {
        val baseDirectory: Directory = Mockito.mock(Directory::class.java)
        whenMock(baseDirectory.asFile).thenReturn(baseDir)

        whenMock(this.baseDir.asFile).thenReturn(DefaultProvider { baseDir })
        whenMock(this.baseDir.map(any<Transformer<out Any, in Directory>>()))
            .thenAnswer {
                DefaultProvider {
                    (it.arguments.first() as Transformer<out Any, in Directory>).transform(baseDirectory)
                }
            }
        whenMock(this.useLegacyNdkSymbolUpload.orNull).thenReturn(useLegacyNdkSymbolUpload)
        whenMock(this.overrides.get()).thenReturn(overrides)
    }
}
