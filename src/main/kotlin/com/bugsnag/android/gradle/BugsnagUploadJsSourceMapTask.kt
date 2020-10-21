package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
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
        project.logger.lifecycle("Uploading sourcemap: $bundleJsFile, JS bundle: $bundleJsFile")
        requestOutputFile.asFile.get().writeText(cliResult)
    }
}
