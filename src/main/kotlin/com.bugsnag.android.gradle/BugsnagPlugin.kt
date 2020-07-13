package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import java.io.File

/**
 * Gradle plugin to automatically upload ProGuard mapping files to Bugsnag.
 *
 * This plugin creates Gradle Tasks, and hooks them into a typical build
 * process. Knowledge of the Android build lifecycle is required to
 * understand how we attach tasks as dependencies.
 *
 * Run `gradle tasks --all` in an Android app project to see all tasks and
 * dependencies.
 *
 * Further reading:
 * https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Tasks
 * https://docs.gradle.org/current/userguide/custom_tasks.html
 */
class BugsnagPlugin : Plugin<Project> {

    companion object {
        private const val GROUP_NAME = "Bugsnag"
        private const val NDK_PROJ_TASK = "externalNative"
        private const val CLEAN_TASK = "Clean"
        private const val ASSEMBLE_TASK = "assemble"
        private const val BUNDLE_TASK = "bundle"
    }

    override fun apply(project: Project) {
        val bugsnag = project.extensions.create("bugsnag", BugsnagPluginExtension::class.java)

        project.afterEvaluate {
            // Make sure the android plugin has been applied first
            if (project.plugins.hasPlugin(AppPlugin::class.java)) {
                val android = project.extensions.getByType(AppExtension::class.java)
                android.applicationVariants.all { variant ->
                    applyBugsnagToVariant(variant, project, bugsnag, android)
                }
            } else {
                throw IllegalStateException("Must apply \'com.android.application\' first!")
            }

            if (isNdkProject(bugsnag, project.extensions.getByType(AppExtension::class.java))) {
                setupNdkProject(project)
            }
        }
    }

    private fun setupNdkProject(project: Project) {
        val tasks = project.tasks
        val cleanTasks: Set<Task> = tasks.filter {
            it.name.startsWith(NDK_PROJ_TASK) && it.name.contains(CLEAN_TASK)
        }.toSet()
        val buildTasks: Set<Task>  = tasks.filter {
            it.name.startsWith(NDK_PROJ_TASK) && !it.name.contains(CLEAN_TASK)
        }.toSet()

        tasks.register("bugsnagInstallJniLibsTask", BugsnagNdkSetupTask::class.java) { ndkSetupTask ->
            buildTasks.forEach {
                ndkSetupTask.mustRunAfter(cleanTasks)
                it.dependsOn(ndkSetupTask)
                it.doFirst { ndkSetupTask }
            }
        }
    }

    /**
     * Create tasks for each Build Variant
     * See https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
     */
    private fun applyBugsnagToVariant(variant: BaseVariant, project: Project,
                                      bugsnag: BugsnagPluginExtension, android: AppExtension) {
        if (!bugsnag.isEnabled) {
            return
        }

        variant.outputs.forEach { output ->
            val deps = BugsnagTaskDeps(variant, output)
            setupManifestUuidTask(project, deps)

            if (variant.buildType.isMinifyEnabled || hasDexguardPlugin(project)) {
                setupMappingFileUpload(project, deps, bugsnag)
            }

            setupNdkMappingFileUpload(project, deps, bugsnag)
            setupReleasesTask(project, deps, bugsnag)
        }
    }

    private fun shouldUploadDebugMappings(output: BaseVariantOutput, bugsnag: BugsnagPluginExtension): Boolean {
        return !output.name.toLowerCase().endsWith("debug") || bugsnag.isUploadDebugBuildMappings
    }

    /**
     * Creates a bugsnag task to upload proguard mapping file
     */
    private fun setupMappingFileUpload(project: Project, deps: BugsnagTaskDeps, bugsnag: BugsnagPluginExtension) {
        val taskName = "uploadBugsnag${taskNameForOutput(deps.output)}Mapping"
        project.tasks.register(taskName, BugsnagUploadProguardTask::class.java) { uploadTask ->
            uploadTask.partName = "proguard"
            prepareUploadTask(uploadTask, deps, project, bugsnag)
        }
    }

    private fun setupNdkMappingFileUpload(project: Project, deps: BugsnagTaskDeps, bugsnag: BugsnagPluginExtension) {
        if (isNdkProject(bugsnag, project.extensions.getByType(AppExtension::class.java))) {
            // Create a Bugsnag task to upload NDK mapping file(s)
            val taskName = "uploadBugsnagNdk${taskNameForOutput(deps.output)}Mapping"
            project.tasks.register(taskName, BugsnagUploadNdkTask::class.java) { uploadNdkTask ->
                prepareUploadTask(uploadNdkTask, deps, project, bugsnag)

                uploadNdkTask.variantName = taskNameForVariant(deps.variant)
                uploadNdkTask.projectDir = project.projectDir
                uploadNdkTask.rootDir = project.rootDir
                uploadNdkTask.sharedObjectPath = bugsnag.sharedObjectPath
            }
        }
    }

    private fun isNdkProject(bugsnag: BugsnagPluginExtension, android: AppExtension): Boolean {
        val ndk = bugsnag.ndk
        return if (ndk != null) { // always respect user override
            ndk
        } else { // infer whether native build or not
            val usesCmake = android.externalNativeBuild.cmake.path != null
            val usesNdkBuild = android.externalNativeBuild.ndkBuild.path != null
            usesCmake || usesNdkBuild
        }
    }

    private fun setupReleasesTask(project: Project, deps: BugsnagTaskDeps, bugsnag: BugsnagPluginExtension) {
        val taskName = "bugsnagRelease${taskNameForOutput(deps.output)}Task"
        val releasesTask = project.tasks.create(taskName, BugsnagReleasesTask::class.java)
        setupBugsnagTask(releasesTask, deps)

        if (shouldUploadDebugMappings(deps.output, bugsnag)) {
            findAssembleBundleTasks(deps.variant, deps.output, project).forEach {
                releasesTask.mustRunAfter(it)

                if (bugsnag.isAutoReportBuilds) {
                    it.finalizedBy(releasesTask)
                }
            }
        }
    }

    private fun setupBugsnagTask(task: Task, deps: BugsnagTaskDeps) {
        task.group = GROUP_NAME
        task.setProperty("variantOutput", deps.output)
        task.setProperty("variant", deps.variant)
    }

    private fun prepareUploadTask(uploadTask: Task, deps: BugsnagTaskDeps, project: Project, bugsnag: BugsnagPluginExtension) {
        setupBugsnagTask(uploadTask, deps)
        uploadTask.setProperty("applicationId", deps.variant.applicationId)

        if (shouldUploadDebugMappings(deps.output, bugsnag)) {
            findAssembleBundleTasks(deps.variant, deps.output, project).forEach {
                uploadTask.mustRunAfter(it)

                    if (bugsnag.isAutoUpload) {
                        it.finalizedBy(uploadTask)
                    }
            }
        }
    }

    /**
     * Fetches all the assemble and bundle tasks in the current project that match the variant
     *
     * Expected behaviour: [assemble, assembleJavaExampleRelease, assembleJavaExample, assembleRelease,
     * bundle, bundleJavaExampleRelease, bundleJavaExample, bundleRelease]
     *
     * @param output the variantOutput
     * @param project the current project
     * @return the assemble + bundle tasks
     */
    private fun findAssembleBundleTasks(variant: BaseVariant, output: BaseVariantOutput, project: Project): Set<Task> {
        val taskNames = HashSet<String>()
        taskNames.addAll(findTaskNamesForPrefix(variant, output, ASSEMBLE_TASK))
        taskNames.addAll(findTaskNamesForPrefix(variant, output, BUNDLE_TASK))

        return project.tasks.filter {
            taskNames.contains(it.name)
        }.toSet()
    }

    /**
     * Finds all the task names which can be used to assemble a variant, and replaces 'assemble' with the given
     * prefix.
     *
     * E.g. [bundle, bundleRelease, bundleFooRelease]
     */
    private fun findTaskNamesForPrefix(variant: BaseVariant, output: BaseVariantOutput, prefix: String): Set<String> {
        val variantName = output.name.split("-")[0].capitalize()
        val assembleTask = variant.assembleProvider.getOrNull()

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

    private fun setupManifestUuidTask(project: Project, deps: BugsnagTaskDeps) {
        val taskName = "processBugsnag${taskNameForOutput(deps.output)}Manifest"
        project.tasks.register(taskName, BugsnagManifestTask::class.java) { manifestTask ->
            setupBugsnagTask(manifestTask, deps)
            val processManifest = deps.output.processManifestProvider.getOrNull()

            if (processManifest != null) {
                processManifest.finalizedBy(manifestTask)
                manifestTask.dependsOn(processManifest)

                val resourceTasks: Set<Task> = project.tasks.filter {
                    val name = it.name.toLowerCase()
                    name.startsWith(BUNDLE_TASK) && name.endsWith("resources")
                }.toSet()

                resourceTasks.forEach {
                    it.dependsOn(manifestTask)
                }
            }
        }
    }

    fun resolveBundleManifestOutputDirectory(processManifest: ManifestProcessorTask): File {
        // For AGP versions >= 3.3.0 the bundle manifest is output to its own directory
        val method = processManifest.javaClass.getDeclaredMethod("getBundleManifestOutputDirectory")
        return when (val directory = method.invoke(processManifest)) {
            is File -> directory // 3.3.X - 3.5.X returns a File
            is DirectoryProperty -> directory.asFile.get() // 3.6.+ returns a DirectoryProperty
            else -> throw IllegalStateException()
        }
    }

    fun taskNameForVariant(variant: BaseVariant): String {
        return variant.name.capitalize()
    }

    fun taskNameForOutput(output: BaseVariantOutput): String {
        return output.name.capitalize()
    }

    /**
     * Returns true if the DexGuard plugin has been applied to the project
     */
    fun hasDexguardPlugin(project: Project): Boolean {
        return project.pluginManager.hasPlugin("dexguard")
    }

    /**
     * Returns true if a project has configured multiple variant outputs.
     *
     * This calculation is based on a heuristic - the number of variantOutputs in a project must be
     * greater than the number of variants.
     */
    fun hasMultipleOutputs(android: AppExtension): Boolean {
        val variants: DomainObjectSet<ApplicationVariant> = android.applicationVariants
        val variantSize = variants.count()
        var outputSize = 0

        variants.forEach { variant ->
            outputSize += variant.outputs.count()
        }
        return outputSize > variantSize
    }

    /**
     * Whether or not an assemble task is going to be run for this variant
     */
    fun isRunningAssembleTask(variant: BaseVariant, output: BaseVariantOutput, project: Project): Boolean {
        return isRunningTaskWithPrefix(variant, output, project, ASSEMBLE_TASK)
    }

    /**
     * Whether or not a bundle task is going to be run for this variant
     */
    fun isRunningBundleTask(variant: BaseVariant, output: BaseVariantOutput, project: Project): Boolean {
        return isRunningTaskWithPrefix(variant, output, project, BUNDLE_TASK)
    }

    /**
     * Whether or any of a list of the task names for a prefix are going to be run by checking the list
     * against all of the tasks in the task graph
     */
    private fun isRunningTaskWithPrefix(variant: BaseVariant, output: BaseVariantOutput,
                                        project: Project, prefix: String): Boolean {
        val taskNames = HashSet<String>()
        taskNames.addAll(findTaskNamesForPrefix(variant, output, prefix))

        return project.gradle.taskGraph.allTasks.any { task ->
            taskNames.any {
                task.name.endsWith(it)
            }
        }
    }

    class BugsnagTaskDeps(val variant: BaseVariant, val output: BaseVariantOutput)
}
