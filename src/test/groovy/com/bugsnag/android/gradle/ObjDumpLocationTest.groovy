package com.bugsnag.android.gradle

import static org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized)
@SuppressWarnings(["PublicInstanceField", "NonFinalPublicField", "NestedForLoop"])
class ObjDumpLocationTest {

    @Parameterized.Parameter(0)
    public Abi abi

    @Parameterized.Parameter(1)
    public String osName

    @Parameterized.Parameter(2)
    public String ndkDir

    @Parameterized.Parameters
    static Collection<Object[]> inputs() {
        Collection<Object> inputs = new ArrayList<>()

        for (Abi abi : Abi.values()) {
            for (String os : ["darwin-x86_64", "linux-x86_64", "windows", "windows-x86_64"]) {
                for (String ndkDir : ["/Users/bob/Library/Android/sdk/ndk-bundle", "/etc/ndk-bundle"]) {
                    inputs.add([abi, os, ndkDir].toArray())
                }
            }
        }
        inputs
    }

    @Parameterized.Parameters
    static Collection<String> os() {
        Arrays.asList("windows", "linux")
    }

    @Test
    void defaultObjDumpLocation() {
        File file = BugsnagUploadNdkTask.calculateObjDumpLocation(ndkDir, abi, osName)
        String exec = osName.startsWith("windows") ? "objdump.exe" : "objdump"
        String expected = "$ndkDir/toolchains/$abi.toolchainPrefix-4.9/prebuilt/$osName/bin/$abi.objdumpPrefix-$exec"
        assertEquals(expected, file.path)
    }
}
