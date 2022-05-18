package com.bugsnag.android.gradle.internal

import com.android.build.gradle.tasks.ExternalNativeBuildTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File
import kotlin.reflect.full.memberProperties

class ExternalNativeBuildTaskUtil(private val providerFactory: ProviderFactory) {
    private fun getSearchDir(buildTask: Provider<ExternalNativeBuildTask>, propName: String): Provider<File> =
        buildTask.flatMap { task ->
            val soFolder = ExternalNativeBuildTask::class.memberProperties.find { it.name == propName }?.get(task)!!
            when (soFolder) {
                is File -> providerFactory.provider { fixNativeOutputPath(soFolder) }
                is DirectoryProperty -> soFolder.map { fixNativeOutputPath(it.asFile) }
                else -> throw IllegalArgumentException("Unknown type of ${propName}: $soFolder")
            }
        }

    private fun fixNativeOutputPath(taskFolder: File): File {
        return taskFolder.parentFile.parentFile.takeIf { it.parentFile.name == "cxx" } ?: taskFolder
    }

    fun findSearchPaths(buildTask: Provider<ExternalNativeBuildTask>) = arrayOf(
        getSearchDir(buildTask, "objFolder"),
        getSearchDir(buildTask, "soFolder")
    )
}
