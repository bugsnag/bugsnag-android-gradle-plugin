package com.bugsnag.android.gradle.internal

import com.android.build.gradle.tasks.ExternalNativeBuildTask
import org.gradle.api.provider.Provider

object ExternalNativeBuildTaskUtil {
    fun findSearchPath(buildTask: Provider<ExternalNativeBuildTask>) =
        buildTask.flatMap { it.soFolder }.map { soFolder ->
            soFolder.asFile.parentFile.parentFile.takeIf { it.parentFile.name == "cxx" }
                ?: soFolder.asFile.parentFile.parentFile.parentFile.takeIf { it.parentFile.name == "cxx" }
                ?: soFolder.asFile
        }
}
