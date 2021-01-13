package com.bugsnag.android.gradle

enum class Abi(val abiName: String, val toolchainPrefix: String, val objdumpPrefix: String) {
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
    );

    companion object {
        fun findByName(abiName: String) = values().firstOrNull { it.abiName == abiName }
    }
}
