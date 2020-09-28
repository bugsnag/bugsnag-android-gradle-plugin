package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.bugsnag.android.gradle.SharedObjectMappingFileFactory.UNITY_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.mapProperty
import com.bugsnag.android.gradle.internal.register
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/**
 * Task that generates Unity shared object mapping files for upload to Bugsnag.
 */
internal open class BugsnagGenerateUnitySoMappingTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Generates Unity mapping files for upload to Bugsnag"
    }

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:Input
    val objDumpPaths: MapProperty<String, String> = objects.mapProperty()

    @get:OutputDirectory
    val intermediateOutputDir: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir(UNITY_SO_MAPPING_DIR))

    @TaskAction
    fun generateMappingFiles() {
        logger.lifecycle("Generating Unity mapping files")
        // TODO generate mapping file here
    }

    companion object {
        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagGenerateUnitySoMappingTask.() -> Unit
        ): TaskProvider<out BugsnagGenerateUnitySoMappingTask> {
            return project.tasks.register(name, configurationAction)
        }
    }
}
