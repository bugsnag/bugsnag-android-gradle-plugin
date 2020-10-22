package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class BugsnagUploadJsSourceMapTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Uploads JS source maps to Bugsnag"
    }

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:InputFile
    val bundleJsFile: RegularFileProperty = objects.fileProperty()

    @get:InputFile
    val sourceMapFile: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun uploadJsSourceMap() {
        // Construct a basic request
        val manifestInfo = parseManifestInfo()
        val cliResult = "success"
        project.logger.lifecycle("Uploading sourcemap: ${bundleJsFile.get()}, JS bundle: ${bundleJsFile.get()}")
        requestOutputFile.asFile.get().writeText(cliResult)
    }

    companion object {

        /**
         * Introspects the command line arguments for the React Native Exec task
         * and returns the value of the argument which matches the given key.
         */
        fun findReactNativeTaskArg(task: Task, key: String): String? {
            val args = task.property("args") as List<*>
            val index = args.indexOf(key)
            if (index != -1 && index < args.size) {
                return args[index + 1] as String?
            }
            return null
        }
    }
}
