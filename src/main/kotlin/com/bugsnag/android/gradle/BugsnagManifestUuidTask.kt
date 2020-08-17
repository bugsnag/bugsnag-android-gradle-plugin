package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.bugsnag.android.gradle.internal.property
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths
import javax.inject.Inject

abstract class BaseBugsnagManifestUuidTask(objects: ObjectFactory) : DefaultTask() {
    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @get:Input
    val buildUuid: Property<String> = objects.property()

    @get:OutputFile
    val manifestInfoProvider: RegularFileProperty = objects.fileProperty()

    fun writeManifestInfo(info: AndroidManifestInfo) {
        info.write(manifestInfoProvider.get().asFile)
    }
}

/**
 * Task to add a unique build UUID to AndroidManifest.xml during the build
 * process. This is used by Bugsnag to identify which proguard mapping file
 * should be used to deobfuscate each crash report.
 *
 * https://docs.gradle.org/current/userguide/custom_tasks.html
 *
 * This task must be called after "process${variantName}Manifest", since it
 * requires that an AndroidManifest.xml exists in `build/intermediates`.
 */
open class BugsnagManifestUuidTask @Inject constructor(objects: ObjectFactory) : BaseBugsnagManifestUuidTask(objects) {

    companion object {
        private const val ASSEMBLE_TASK = "assemble"
        private const val BUNDLE_TASK = "bundle"
    }

    @get:Internal
    internal lateinit var variantOutput: ApkVariantOutput

    @get:Internal
    internal lateinit var variant: ApkVariant

    @TaskAction
    fun updateManifest() {
        val manifestPath = getManifestPaths(project, variant, variantOutput)
        if (manifestPath == null) {
            logger.warn("Bugsnag: Failed to find manifest at $manifestPath for $variantOutput")
        }

        logger.info("Bugsnag: Updating manifest with build UUID: $manifestPath")

        // read the manifest information and store it for subsequent tasks
        val manifestParser = AndroidManifestParser()
        manifestParser.writeBuildUuid(manifestPath!!, buildUuid = buildUuid.get())
        writeManifestInfo(manifestParser.readManifest(manifestPath, logger))
    }

    /**
     * Gets the manifest for a given Variant Output, accounting for any APK splits.
     *
     * Currently supported split types include Density, and ABI. There is also a Language split,
     * but it appears to be broken (see issuetracker)
     *
     * @param project the current project
     * @param variant the variant
     * @param variantOutput the variantOutput
     * @return the manifest path
     *
     * See: https://developer.android.com/studio/build/configure-apk-splits.html#build-apks-filename
     * https://issuetracker.google.com/issues/37085185
     */
    private fun getManifestPaths(project: Project, variant: ApkVariant, variantOutput: ApkVariantOutput): File? {
        val directoryMerged: File?
        val directoryBundle: File
        val manifestPaths = mutableListOf<File?>()
        var getMergedManifest = isRunningAssembleTask(project, variant, variantOutput)
        val getBundleManifest = isRunningBundleTask(project, variant, variantOutput)

        // If the manifest location could not be reliably determined, default to the assemble manifest
        if (!getMergedManifest && !getBundleManifest) {
            getMergedManifest = true
        }
        val processManifest = variantOutput.processManifestProvider.get()
        if (getMergedManifest) {
            directoryMerged = getManifestOutputDir(processManifest)
            if (directoryMerged != null) {
                addManifestPath(manifestPaths, directoryMerged, logger, variantOutput)
            }
        }

        // Attempt to get the bundle manifest directory if required
        if (getBundleManifest) {
            directoryBundle = resolveBundleManifestOutputDirectory(processManifest)
            addManifestPath(manifestPaths, directoryBundle, logger, variantOutput)
        }
        require(manifestPaths.size == 1) { "Unexpected number of manifest paths.$manifestPaths" }
        return manifestPaths[0]
    }

    private fun addManifestPath(manifestPaths: MutableList<File?>, directory: File, logger: Logger, variantOutput: ApkVariantOutput) {
        val manifestFile = Paths.get(directory.toString(), variantOutput.dirName, "AndroidManifest.xml").toFile()
        if (manifestFile.exists()) {
            logger.info("Bugsnag: Found manifest at $manifestFile")
            manifestPaths.add(manifestFile)
        } else {
            logger.info("Bugsnag: Failed to find manifest at $manifestFile")
        }
    }

    private fun getManifestOutputDir(processManifest: ManifestProcessorTask): File? {
        return try {
            val outputDir = processManifest.javaClass.getMethod("getManifestOutputDirectory").invoke(processManifest)
            if (outputDir is File) {
                outputDir
            } else {
                // gradle 4.7 introduced a provider API for lazy evaluation of properties,
                // AGP subsequently changed the API from File to Provider<File>
                // see https://docs.gradle.org/4.7/userguide/lazy_configuration.html
                @Suppress("UNCHECKED_CAST") val dir = (outputDir as Provider<Directory?>).orNull
                dir?.asFile
            }
        } catch (exc: Throwable) {
            logger.warn("Bugsnag: failed to find manifestOutputDir", exc)
            null
        }
    }

    private fun resolveBundleManifestOutputDirectory(processManifest: ManifestProcessorTask): File {
        // For AGP versions >= 3.3.0 the bundle manifest is output to its own directory
        val method = processManifest.javaClass.getDeclaredMethod("getBundleManifestOutputDirectory")
        return when (val directory = method.invoke(processManifest)) {
            is File -> directory // 3.3.X - 3.5.X returns a File
            is DirectoryProperty -> directory.asFile.get() // 3.6.+ returns a DirectoryProperty
            else -> throw IllegalStateException()
        }
    }

    /**
     * Whether or not an assemble task is going to be run for this variant
     */
    private fun isRunningAssembleTask(project: Project,
                              variant: ApkVariant,
                              output: ApkVariantOutput): Boolean {
        return isRunningTaskWithPrefix(project, variant, output, ASSEMBLE_TASK)
    }

    /**
     * Whether or not a bundle task is going to be run for this variant
     */
    private fun isRunningBundleTask(project: Project,
                            variant: ApkVariant,
                            output: ApkVariantOutput): Boolean {
        return isRunningTaskWithPrefix(project, variant, output, BUNDLE_TASK)
    }

    /**
     * Whether or any of a list of the task names for a prefix are going to be run by checking the list
     * against all of the tasks in the task graph
     */
    private fun isRunningTaskWithPrefix(project: Project,
                                        variant: ApkVariant,
                                        output: ApkVariantOutput,
                                        prefix: String): Boolean {
        val taskNames = HashSet<String>()
        taskNames.addAll(findTaskNamesForPrefix(variant, output, prefix))

        return project.gradle.taskGraph.allTasks.any { task ->
            taskNames.any {
                task.name.endsWith(it)
            }
        }
    }



    /**
     * Finds all the task names which can be used to assemble a variant, and replaces 'assemble' with the given
     * prefix.
     *
     * E.g. [bundle, bundleRelease, bundleFooRelease]
     */
    internal fun findTaskNamesForPrefix(variant: ApkVariant,
        output: ApkVariantOutput,
        prefix: String): Set<String> {
        val variantName = output.name.split("-")[0].capitalize()
        val assembleTask = variant.assembleProvider.orNull

        val taskNames = HashSet<String>()
        taskNames.add(prefix)

        if (assembleTask != null) {
            val assembleTaskName = assembleTask.name
            val buildTypeTaskName = assembleTaskName.replace(variantName, "")
            val buildType = buildTypeTaskName.replace(ASSEMBLE_TASK, "")
            val variantTaskName = assembleTaskName.replace(buildType, "")

            taskNames.add(assembleTaskName.replace(ASSEMBLE_TASK, prefix))
            taskNames.add(buildTypeTaskName.replace(ASSEMBLE_TASK, prefix))
            taskNames.add(variantTaskName.replace(ASSEMBLE_TASK, prefix))
        }
        return taskNames
    }
}
