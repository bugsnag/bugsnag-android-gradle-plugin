package com.bugsnag.android.gradle

@SuppressWarnings('DuplicateStringLiteral')
enum Abi {

    ARMEABI(
        "armeabi",
        "arm-linux-androideabi",
        "arm-linux-androideabi"
    ),
    ARMEABI_V7A(
        "armeabi-v7a",
        "arm-linux-androideabi",
        "arm-linux-androideabi"
    ),
    ARM64_V8A(
        "arm64-v8a",
        "aarch64-linux-android",
        "aarch64-linux-android"
    ),
    X86(
        "x86",
        "x86",
        "i686-linux-android"
    ),
    X86_64(
        "x86_64",
        "x86_64",
        "x86_64-linux-android"
    )

    final String abiName
    final String toolchainPrefix
    final String objdumpPrefix

    Abi(String abiName, String toolchainPrefix, String objdumpPrefix) {
        this.abiName = abiName
        this.toolchainPrefix = toolchainPrefix
        this.objdumpPrefix = objdumpPrefix
    }

    static Abi findByName(String abiName) {
        for (Abi value : values()) {
            if (value.abiName == abiName) {
                return value
            }
        }
        null
    }
}
