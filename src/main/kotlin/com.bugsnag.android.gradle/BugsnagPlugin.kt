package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.PackageApplication
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.TaskProvider
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
        const val GROUP_NAME = "Bugsnag"
        private const val NDK_PROJ_TASK = "externalNative"
        private const val CLEAN_TASK = "Clean"
        private const val ASSEMBLE_TASK = "assemble"
        private const val BUNDLE_TASK = "bundle"
    }

    override fun apply(project: Project) {
        val bugsnag = project.extensions.create("bugsnag", BugsnagPluginExtension::class.java)

        project.afterEvaluate {
            // Make sure the android plugin has been applied first
            if (!project.plugins.hasPlugin(AppPlugin::class.java)) {
                throw IllegalStateException("Must apply \'com.android.application\' first!")
            }
            if (!bugsnag.isEnabled) {
                return@afterEvaluate
            }

            val android = project.extensions.getByType(AppExtension::class.java)
            android.applicationVariants.all { variant ->
                registerBugsnagTasksForVariant(project, variant, bugsnag)
            }

            if (isNdkProject(bugsnag, android)) {
                registerNdkLibInstallTask(project)
            }
        }
    }

    private fun registerNdkLibInstallTask(project: Project) {
        val tasks = project.tasks
        val cleanTasks: Set<Task> = tasks.filter {
            it.name.startsWith(NDK_PROJ_TASK) && it.name.contains(CLEAN_TASK)
        }.toSet()
        val buildTasks: Set<Task> = tasks.filter {
            it.name.startsWith(NDK_PROJ_TASK) && !it.name.contains(CLEAN_TASK)
        }.toSet()

        tasks.register("bugsnagInstallJniLibsTask", BugsnagInstallJniLibsTask::class.java) { ndkSetupTask ->
            buildTasks.forEach {
                ndkSetupTask.mustRunAfter(cleanTasks)
                it.dependsOn(ndkSetupTask)
                it.doFirst { ndkSetupTask }
            }
        }
    }

    /**
     * Register manifest UUID writing + upload tasks for each Build Variant.
     *
     * See https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
     */
    private fun registerBugsnagTasksForVariant(project: Project,
                                               variant: ApkVariant,
                                               bugsnag: BugsnagPluginExtension) {
        variant.outputs.configureEach {
            val output = it as ApkVariantOutput
            val jvmMinificationEnabled = variant.buildType.isMinifyEnabled || hasDexguardPlugin(project)
            val ndkEnabled = isNdkProject(bugsnag, project.extensions.getByType(AppExtension::class.java))

            // register bugsnag tasks
            val manifestUuidTask = registerManifestUuidTask(project, variant, output)
            val proguardTask = when {
                jvmMinificationEnabled -> registerProguardUploadTask(project, variant, output, bugsnag)
                else -> null
            }
            val symbolFileTask = when {
                ndkEnabled -> registerSharedObjectUploadTask(project, variant, output, bugsnag)
                else -> null
            }
            val releasesTask = registerReleasesUploadTask(project, variant, output, bugsnag)

            // calls get() to ensure that the registered tasks are executed
            // if the packageApplication task will run. This uses the configuration avoidance API,
            // see https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
            findAssembleBundleTasks(project, variant, output).forEach {
                manifestUuidTask.get()
                proguardTask?.get()
                symbolFileTask?.get()
                releasesTask.get()
            }
        }
    }

    private fun registerManifestUuidTask(project: Project,
                                         variant: ApkVariant,
                                         output: ApkVariantOutput): TaskProvider<BugsnagManifestTask> {
        val taskName = "processBugsnag${taskNameForOutput(output)}Manifest"
        return project.tasks.register(taskName, BugsnagManifestTask::class.java) {
            it.variantOutput = output
            it.variant = variant
            val processManifest = output.processManifestProvider.getOrNull()

            if (processManifest != null) {
                processManifest.finalizedBy(it)
                it.dependsOn(processManifest)
                it.mustRunAfter(processManifest)
            }
        }
    }

    /**
     * Creates a bugsnag task to upload proguard mapping file
     */
    private fun registerProguardUploadTask(project: Project,
                                           variant: ApkVariant,
                                           output: ApkVariantOutput,
                                           bugsnag: BugsnagPluginExtension): TaskProvider<BugsnagUploadProguardTask> {
        val taskName = "uploadBugsnag${taskNameForOutput(output)}Mapping"
        return project.tasks.register(taskName, BugsnagUploadProguardTask::class.java) {
            it.variantOutput = output
            it.variant = variant
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, bugsnag.isAutoUpload)
        }
    }

    private fun registerSharedObjectUploadTask(project: Project,
                                               variant: ApkVariant,
                                               output: ApkVariantOutput,
                                               bugsnag: BugsnagPluginExtension): TaskProvider<BugsnagUploadNdkTask> {
        // Create a Bugsnag task to upload NDK mapping file(s)
        val taskName = "uploadBugsnagNdk${taskNameForOutput(output)}Mapping"
        return project.tasks.register(taskName, BugsnagUploadNdkTask::class.java) {
            it.variantOutput = output
            it.variant = variant
            it.variantName = taskNameForVariant(variant)
            it.projectDir = project.projectDir
            it.rootDir = project.rootDir
            it.sharedObjectPath = bugsnag.sharedObjectPath
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, bugsnag.isAutoUpload)
        }
    }

    private fun registerReleasesUploadTask(project: Project,
                                           variant: ApkVariant,
                                           output: ApkVariantOutput,
                                           bugsnag: BugsnagPluginExtension): TaskProvider<BugsnagReleasesTask> {
        val taskName = "bugsnagRelease${taskNameForOutput(output)}Task"
        return project.tasks.register(taskName, BugsnagReleasesTask::class.java) {
            it.variantOutput = output
            it.variant = variant
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, bugsnag.isAutoReportBuilds)
        }
    }

    private fun addTaskToExecutionGraph(task: Task,
                                        variant: ApkVariant,
                                        output: ApkVariantOutput,
                                        project: Project,
                                        bugsnag: BugsnagPluginExtension,
                                        autoUpload: Boolean) {
        if (shouldUploadDebugMappings(output, bugsnag)) {
            findAssembleBundleTasks(project, variant, output).forEach {
                task.dependsOn(it)
                task.mustRunAfter(it)

                if (autoUpload) {
                    it.finalizedBy(task)
                }
            }
        }
    }

    private fun shouldUploadDebugMappings(output: ApkVariantOutput,
                                          bugsnag: BugsnagPluginExtension): Boolean {
        return !output.name.toLowerCase().endsWith("debug") || bugsnag.isUploadDebugBuildMappings
    }

    private fun isNdkProject(bugsnag: BugsnagPluginExtension,
                             android: AppExtension): Boolean {
        val ndk = bugsnag.ndk
        return if (ndk != null) { // always respect user override
            ndk
        } else { // infer whether native build or not
            val usesCmake = android.externalNativeBuild.cmake.path != null
            val usesNdkBuild = android.externalNativeBuild.ndkBuild.path != null
            usesCmake || usesNdkBuild
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
    private fun findAssembleBundleTasks(project: Project,
                                        variant: ApkVariant,
                                        output: ApkVariantOutput): Set<Task> {
        // assemble tasks can be depended on with the packageApplicationProvider
        val tasks = HashSet<Task>()
        tasks.add(variant.packageApplicationProvider.get())
        return tasks
    }

    /**
     * Finds all the task names which can be used to assemble a variant, and replaces 'assemble' with the given
     * prefix.
     *
     * E.g. [bundle, bundleRelease, bundleFooRelease]
     */
    private fun findTaskNamesForPrefix(variant: ApkVariant,
                                       output: ApkVariantOutput,
                                       prefix: String): Set<String> {
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

    fun resolveBundleManifestOutputDirectory(processManifest: ManifestProcessorTask): File {
        // For AGP versions >= 3.3.0 the bundle manifest is output to its own directory
        val method = processManifest.javaClass.getDeclaredMethod("getBundleManifestOutputDirectory")
        return when (val directory = method.invoke(processManifest)) {
            is File -> directory // 3.3.X - 3.5.X returns a File
            is DirectoryProperty -> directory.asFile.get() // 3.6.+ returns a DirectoryProperty
            else -> throw IllegalStateException()
        }
    }

    fun taskNameForVariant(variant: ApkVariant): String {
        return variant.name.capitalize()
    }

    fun taskNameForOutput(output: ApkVariantOutput): String {
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
    fun isRunningAssembleTask(project: Project,
                              variant: ApkVariant,
                              output: ApkVariantOutput): Boolean {
        return isRunningTaskWithPrefix(project, variant, output, ASSEMBLE_TASK)
    }

    /**
     * Whether or not a bundle task is going to be run for this variant
     */
    fun isRunningBundleTask(project: Project,
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
}
