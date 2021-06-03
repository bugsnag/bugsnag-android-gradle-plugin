package com.bugsnag.android.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * An AGP-4-compatible implementation of [BugsnagManifestUuidTask].
 */
open class BugsnagManifestUuidTaskV2 @Inject constructor(
    objects: ObjectFactory
) : BaseBugsnagManifestUuidTask(objects) {

    // NONE because we only care about its contents, not location.
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    val inputManifest: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputManifest: RegularFileProperty = objects.fileProperty()

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    fun updateManifest() {
        val manifestParser = AndroidManifestParser()
        val output = outputManifest.asFile.get()
        manifestParser.writeBuildUuid(
            inputManifest.asFile.get(),
            outputManifest.asFile.get(),
            buildUuid = buildUuid.get()
        )
        writeManifestInfo(manifestParser.readManifest(output, logger))
    }
}
