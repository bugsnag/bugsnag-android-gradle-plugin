package com.bugsnag.android.gradle.internal

import com.bugsnag.android.gradle.Abi
import org.gradle.api.Transformer
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import java.io.File
import org.mockito.Mockito.`when` as whenMock

class NdkToolchainTest {
    @Test
    fun ndk19() {
        val toolchain = TestNdkToolchainImpl(File("/19.2.5345600/"), true)
        assertEquals(NdkToolchain.MappingTool.OBJDUMP, toolchain.preferredMappingTool())
    }

    @Test
    fun ndk21Legacy() {
        val toolchain = TestNdkToolchainImpl(File("/21.1.6352462/"), true)
        assertEquals(NdkToolchain.MappingTool.OBJDUMP, toolchain.preferredMappingTool())
    }

    @Test
    fun ndk21() {
        val toolchain = TestNdkToolchainImpl(File("/21.1.6352462/"), false)
        assertEquals(NdkToolchain.MappingTool.OBJCOPY, toolchain.preferredMappingTool())

        val objcopyPath = toolchain.objcopyForAbi(Abi.ARM64_V8A).toString()
        assertTrue(
            "expected GNU objcopy path, but got: $objcopyPath",
            objcopyPath.contains("/aarch64-linux-android-objcopy")
        )
    }

    @Test
    fun ndk23() {
        val toolchain = TestNdkToolchainImpl(File("/23.0.7599858/"), false)
        assertEquals(NdkToolchain.MappingTool.OBJCOPY, toolchain.preferredMappingTool())

        val objcopyPath = toolchain.objcopyForAbi(Abi.ARM64_V8A).toString()
        assertTrue(
            "expected LLVM objcopy, but got: $objcopyPath",
            objcopyPath.contains("/llvm-objcopy")
        )
    }

    @Test
    fun objcopyOverrides() {
        val toolchain = TestNdkToolchainImpl(
            File("/23.0.7599858/"), false, mapOf(Abi.ARM64_V8A to "arm64-objcopy")
        )

        assertEquals(NdkToolchain.MappingTool.OBJCOPY, toolchain.preferredMappingTool())

        assertEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.ARM64_V8A).toString())
        assertNotEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.X86).toString())
        assertNotEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.X86_64).toString())
        assertNotEquals("arm64-objcopy", toolchain.objcopyForAbi(Abi.ARMEABI).toString())
    }

    @Test
    fun objdumpOverrides() {
        val toolchain = TestNdkToolchainImpl(
            File("/19.2.5345600/"), true, mapOf(Abi.ARM64_V8A to "arm64-objcopy")
        )

        assertEquals(NdkToolchain.MappingTool.OBJDUMP, toolchain.preferredMappingTool())

        assertEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.ARM64_V8A).toString())
        assertNotEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.X86).toString())
        assertNotEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.X86_64).toString())
        assertNotEquals("arm64-objcopy", toolchain.objdumpForAbi(Abi.ARMEABI).toString())
    }
}

private class TestNdkToolchainImpl(
    baseDir: File,
    useLegacyNdkSymbolUpload: Boolean,
    overrides: Map<Abi, String> = emptyMap()
) : NdkToolchain() {
    override val baseDir: Property<File> = Mockito.mock(Property::class.java) as Property<File>
    override val useLegacyNdkSymbolUpload: Property<Boolean> = Mockito.mock(Property::class.java) as Property<Boolean>
    override val overrides: MapProperty<Abi, String> = Mockito.mock(MapProperty::class.java) as MapProperty<Abi, String>
    override val bugsnagNdkVersion: Property<String> = Mockito.mock(Property::class.java) as Property<String>
    override val variantName: Property<String> = Mockito.mock(Property::class.java) as Property<String>

    init {
        whenMock(this.baseDir.get()).thenReturn(baseDir)
        whenMock(this.baseDir.map(any<Transformer<out Any, in File>>()))
            .thenAnswer {
                DefaultProvider {
                    (it.arguments.first() as Transformer<out Any, in File>).transform(baseDir)
                }
            }
        whenMock(this.useLegacyNdkSymbolUpload.get()).thenReturn(useLegacyNdkSymbolUpload)
        whenMock(this.overrides.get()).thenReturn(overrides)
    }
}
