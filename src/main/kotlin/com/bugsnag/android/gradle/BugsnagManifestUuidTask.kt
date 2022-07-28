package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.internal.VariantTaskCompanion
import com.bugsnag.android.gradle.internal.forBuildOutput
import com.bugsnag.android.gradle.internal.property
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class BugsnagManifestUuidTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @get:Input
    val buildUuid: Property<String> = objects.property()

    @get:OutputFile
    val manifestInfoProvider: RegularFileProperty = objects.fileProperty()

    // NONE because we only care about its contents, not location.
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    val inputManifest: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputManifest: RegularFileProperty = objects.fileProperty()

    @get:Input
    @get:Optional
    val versionCode: Property<Int> = objects.property()

    @get:Input
    @get:Optional
    val versionName: Property<String> = objects.property()

    @TaskAction
    fun updateManifest() {
        val manifestParser = AndroidManifestParser()
        val output = outputManifest.asFile.get()
        manifestParser.writeBuildUuid(
            inputManifest.asFile.get(),
            outputManifest.asFile.get(),
            buildUuid = buildUuid.get()
        )
        writeManifestInfo(
            manifestParser.readManifest(output, logger).withOverrides(versionCode.orNull, versionName.orNull)
        )
    }

    fun writeManifestInfo(info: AndroidManifestInfo) {
        info.write(manifestInfoProvider.get().asFile)
    }

    companion object : VariantTaskCompanion<BugsnagManifestUuidTask> {
        fun manifestInfoForOutput(project: Project, output: BaseVariantOutput): Provider<RegularFile> =
            forBuildOutput(project, output).flatMap { it.manifestInfoProvider }

        override fun taskNameFor(variantOutputName: String): String =
            "processBugsnag${variantOutputName.capitalize()}Manifest"
    }
}
