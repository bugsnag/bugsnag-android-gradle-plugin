package com.bugsnag.android.gradle

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import static org.junit.Assert.*

@RunWith(Parameterized.class)
class ObjDumpLocationTest {

    @Parameterized.Parameter(0)
    public Abi abi

    @Parameterized.Parameter(1)
    public String osName

    @Parameterized.Parameter(2)
    public String ndkDir

    @Parameterized.Parameters
    static Collection<Object[]> inputs() {
        Collection<Object> inputs = new ArrayList<>();

        for (Abi abi: Abi.values()) {
            for (String os: ["darwin-x86_64", "linux-x86_64", "windows", "windows-x86_64"]) {
                for (String ndkDir: ["/Users/bob/Library/Android/sdk/ndk-bundle", "/etc/ndk-bundle"]) {
                    inputs.add([abi, os, ndkDir].toArray())
                }
            }
        }
        return inputs
    }

    @Parameterized.Parameters
    static Collection<String> os() {
        return Arrays.asList("windows", "linux")
    }

    @Test
    void defaultObjDumpLocation() {
        File file = BugsnagUploadNdkTask.calculateObjDumpLocation(ndkDir, abi, osName)
        String expected = "$ndkDir/toolchains/$abi.toolchainPrefix-4.9/prebuilt/$osName/bin/$abi.objdumpPrefix-objdump"
        assertEquals(expected, file.path)
    }

}
