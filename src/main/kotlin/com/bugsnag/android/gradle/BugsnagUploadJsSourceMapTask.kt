package com.bugsnag.android.gradle

import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

sealed class BugsnagUploadJsSourceMapTask @Inject constructor(
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
    val bundleJsFileProvider: RegularFileProperty = objects.fileProperty()

    @get:InputFile
    val sourceMapFileProvider: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    @get:InputFiles
    abstract val projectRootFileProvider: ConfigurableFileCollection

    @get:Input
    val overwrite: Property<Boolean> = objects.property()

    @get:Input
    val endpoint: Property<String> = objects.property()

    @get:Input
    val devEnabled: Property<String> = objects.property()

    @get:Input
    val failOnUploadError: Property<Boolean> = objects.property()

    @TaskAction
    fun uploadJsSourceMap() {
        // Construct a basic request
        val manifestInfo = parseManifestInfo()

        val builder = ProcessBuilder(
            "echo", // TODO this is a placeholder until bugsnag-source-maps is ready

            "--api-key",
            manifestInfo.apiKey,

            "--app-version",
            manifestInfo.versionName,

            "--app-version-code",
            manifestInfo.versionCode,

            "--dev",
            devEnabled.get(),

            "--platform",
            "android",

            "--source-map",
            sourceMapFileProvider.asFile.get().absolutePath,

            "--bundle",
            bundleJsFileProvider.asFile.get().absolutePath,

            "--overwrite",
            overwrite.get().toString(),

            "--endpoint",
            endpoint.get(),

            "--project-root",
            projectRootFileProvider.singleFile.absolutePath
        )
        builder.redirectError(ProcessBuilder.Redirect.INHERIT)

        project.logger.lifecycle("Bugsnag: uploading react native sourcemap: ${builder.command()}")

        val process = builder.start()
        val exitCode = process.waitFor()

        if (exitCode != 0 && failOnUploadError.get()) {
            throw IllegalStateException(
                "Bugsnag: source map upload failed, $exitCode." +
                    "Please ensure that bugsnag-source-maps is installed and source maps are enabled."
            )
        }

        val cliResult = when (exitCode) {
            0 -> "success"
            else -> "failure"
        }
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

        /**
         * Registers the appropriate subtype to this [project] with the given [name] and
         * [configurationAction]
         */
        internal fun register(
            project: Project,
            name: String,
            configurationAction: BugsnagUploadJsSourceMapTask.() -> Unit
        ): TaskProvider<out BugsnagUploadJsSourceMapTask> {
            return when {
                project.gradle.versionNumber() >= GradleVersions.VERSION_5_3 -> {
                    project.tasks.register<BugsnagUploadJsSourceMapTaskGradle53Plus>(name, configurationAction)
                } else -> {
                    project.tasks.register<BugsnagUploadJsSourceMapTaskLegacy>(name, configurationAction)
                }
            }
        }
    }
}

/**
 * Legacy [BugsnagUploadJsSourceMapTask] task that requires using [ProjectLayout.configurableFiles].
 */
internal open class BugsnagUploadJsSourceMapTaskLegacy @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : BugsnagUploadJsSourceMapTask(objects) {

    @get:InputFiles
    override val projectRootFileProvider: ConfigurableFileCollection = projectLayout.configurableFiles()
}

internal open class BugsnagUploadJsSourceMapTaskGradle53Plus @Inject constructor(
    objects: ObjectFactory
) : BugsnagUploadJsSourceMapTask(objects) {

    @get:InputFiles
    override val projectRootFileProvider: ConfigurableFileCollection = objects.fileCollection()
}
