package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.bugsnag.android.gradle.internal.GradleVersions
import com.bugsnag.android.gradle.internal.intermediateForRescuedReactNativeBundle
import com.bugsnag.android.gradle.internal.property
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.versionNumber
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Describable
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
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
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

    @get:Optional
    @get:Input
    override val versionCode: Property<Int> = objects.property()

    @get:InputFile
    val bugsnagSourceMaps: RegularFileProperty = objects.fileProperty()

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
    val devEnabled: Property<Boolean> = objects.property()

    @get:Input
    val failOnUploadError: Property<Boolean> = objects.property()

    @TaskAction
    fun uploadJsSourceMap() {
        // Construct a basic request
        val manifestInfo = parseManifestInfo()
        val executable = bugsnagSourceMaps.get().asFile
        val builder = generateUploadCommand(executable.absolutePath, manifestInfo)
        project.logger.lifecycle("Bugsnag: uploading react native sourcemap: ${builder.command()}")

        val process = builder.start()
        val exitCode = process.waitFor()
        val outputMsg = process.inputStream?.bufferedReader().use { it?.readText() }

        outputMsg?.let {
            project.logger.lifecycle("Bugsnag: uploaded react native sourcemap: $outputMsg")
        }

        if (exitCode != 0) {
            val errMsg = process.errorStream.bufferedReader().use { it.readText() }
            val msg = "Bugsnag: source map upload failed. Exit code=$exitCode, msg=$errMsg."
            if (!executable.exists()) {
                project.logger.warn(
                    "Bugsnag: automatic upload of source maps failed because " +
                        "@bugsnag/source-maps could not be located.\n" +
                        "Set `uploadReactNativeMappings=false` if you don't want to upload source maps," +
                        "or install the `@bugsnag/source-maps` node module.\n" +
                        "For further docs, see https://docs.bugsnag.com/platforms/react-native/" +
                        "react-native/showing-full-stacktraces/#uploading-source-maps"
                )
            } else if (failOnUploadError.get()) {
                throw IllegalStateException(msg)
            } else {
                project.logger.error(msg)
            }
        }

        val cliResult = when (exitCode) {
            0 -> "success"
            else -> "failure"
        }
        requestOutputFile.asFile.get().writeText(cliResult)
    }

    private fun generateUploadCommand(executable: String, manifestInfo: AndroidManifestInfo): ProcessBuilder {
        val cmd = mutableListOf(
            executable,
            "upload-react-native",

            "--api-key",
            manifestInfo.apiKey,

            "--app-version",
            manifestInfo.versionName,

            "--app-version-code",
            manifestInfo.versionCode,

            "--platform",
            "android",

            "--source-map",
            sourceMapFileProvider.asFile.get().absolutePath,

            "--bundle",
            bundleJsFileProvider.asFile.get().absolutePath,

            "--project-root",
            projectRootFileProvider.singleFile.absolutePath
        )

        // only supply the endpoint if the user has overridden it (on-prem)
        if (UPLOAD_ENDPOINT_DEFAULT != endpoint.get()) {
            cmd.add("--endpoint")
            cmd.add(endpoint.get())
        }

        if (overwrite.get()) {
            cmd.add("--overwrite")
        }
        if (devEnabled.get()) {
            cmd.add("--dev")
        }
        return ProcessBuilder(cmd)
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
                }
                else -> {
                    project.tasks.register<BugsnagUploadJsSourceMapTaskLegacy>(name, configurationAction)
                }
            }
        }

        internal fun rescueReactNativeSourceBundle(
            rnTask: Task,
            rnBundle: String?,
            project: Project,
            output: BaseVariantOutput
        ): String? {
            // since the Hermes bundle overwrites the JS bundle in the same Task as the Bundle
            // is generated, we need to inject an extra Action to save the JS bundle beforehand
            var rnBundle1 = rnBundle
            val actions = rnTask.actions.toMutableList()
            val indexToInsert = actions.indexOfFirst {
                (it as? Describable)?.displayName?.contains("doLast", ignoreCase = true) == true
            }

            if (indexToInsert != -1) {
                val rnSourceBundle = File(rnBundle1)
                val rnRescuedSourceBundle = intermediateForRescuedReactNativeBundle(project, output)
                actions.add(
                    indexToInsert,
                    Action<Task> {
                        rnSourceBundle.copyTo(rnRescuedSourceBundle)
                    }
                )

                rnTask.actions = actions

                // make sure the remainder of our Task uses the "rescued" source bundle
                rnBundle1 = rnRescuedSourceBundle.absolutePath
            }
            return rnBundle1
        }

        internal fun isHermesEnabled(project: Project): Boolean {
            return project.hasProperty("react") &&
                (project.property("react") as Map<*, *>)["enableHermes"] == true
        }
    }
}

/**
 * Finds the sourcemap output file for the variant.
 *
 * This should always be jsOutputSourceMapFile - for Hermes this is overridden
 * in the RN gradle script, so we calculate the location ourselves.
 *
 * https://github.com/facebook/react-native/blob/master/react.gradle#L116
 */
internal fun findReactNativeSourcemapFile(project: Project, variant: BaseVariant): String {
    val react = project.property("react") as Map<*, *>?
    val bundleAssetName = react?.get("bundleAssetName") as String? ?: "index.android.bundle"
    val jsSourceMapsDir = "${project.buildDir}/generated/sourcemaps/react/${variant.dirName}"
    return "$jsSourceMapsDir/$bundleAssetName.map"
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
