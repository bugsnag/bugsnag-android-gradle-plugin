package com.bugsnag.android.gradle

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.util.UUID

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
        private const val CLEAN_TASK = "Clean"
        const val ASSEMBLE_TASK = "assemble"
        const val BUNDLE_TASK = "bundle"
    }

    override fun apply(project: Project) {
        val bugsnag = project.extensions.create("bugsnag", BugsnagPluginExtension::class.java)
        project.pluginManager.withPlugin("com.android.application") {
            if (!bugsnag.isEnabled) {
                return@withPlugin
            }

            val android = project.extensions.getByType(AppExtension::class.java)
            if (BugsnagManifestUuidTaskV2.isApplicable()) {
                check(android is CommonExtension<*, *, *, *, *, *, *, *>)
                android.onVariants {
                    val variantName = name
                    val taskName = computeManifestTaskNameFor(variantName)
                    val manifestInfoOutputFile = project.computeManifestInfoOutputV2(variantName)
                    val buildUuidProvider = project.newUuidProvider()
                    val manifestUpdater = project.tasks.register(taskName, BugsnagManifestUuidTaskV2::class.java) {
                        it.buildUuid.set(buildUuidProvider)
                        it.manifestInfoProvider.set(manifestInfoOutputFile)
                    }
                    onProperties {
                        artifacts
                            .use(manifestUpdater)
                            .wiredWithFiles(
                                taskInput = BugsnagManifestUuidTaskV2::inputManifest,
                                taskOutput = BugsnagManifestUuidTaskV2::outputManifest
                            )
                            .toTransform(ArtifactType.MERGED_MANIFEST)
                    }
                }
            }

            project.afterEvaluate {
                android.applicationVariants.configureEach { variant ->
                    registerBugsnagTasksForVariant(project, variant, bugsnag)
                }

                if (isNdkProject(bugsnag, android)) {
                    registerNdkLibInstallTask(project)
                }
            }
        }
    }

    private fun registerNdkLibInstallTask(project: Project) {
        val ndkTasks = project.tasks.withType(ExternalNativeBuildTask::class.java)
        val cleanTasks = ndkTasks.filter { it.name.contains(CLEAN_TASK) }.toSet()
        val buildTasks = ndkTasks.filter { !it.name.contains(CLEAN_TASK) }.toSet()

        if (buildTasks.isNotEmpty()) {
            val ndkSetupTask = project.tasks.create("bugsnagInstallJniLibsTask", BugsnagInstallJniLibsTask::class.java)
            ndkSetupTask.mustRunAfter(cleanTasks)
            buildTasks.forEach { it.dependsOn(ndkSetupTask) }
        }
    }

    /**
     * Register manifest UUID writing + upload tasks for each Build Variant.
     *
     * See https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
     */
    private fun registerBugsnagTasksForVariant(
        project: Project,
        variant: ApkVariant,
        bugsnag: BugsnagPluginExtension
    ) {
        variant.outputs.configureEach {
            val output = it as ApkVariantOutput
            val jvmMinificationEnabled = variant.buildType.isMinifyEnabled || hasDexguardPlugin(project)
            val ndkEnabled = isNdkProject(bugsnag, project.extensions.getByType(AppExtension::class.java))

            // register bugsnag tasks
            val manifestInfoFile = registerManifestUuidTask(project, variant, output)
            val proguardTask = when {
                jvmMinificationEnabled -> registerProguardUploadTask(project, variant, output, bugsnag)
                else -> null
            }
            val symbolFileTask = when {
                ndkEnabled -> registerSharedObjectUploadTask(project, variant, output, bugsnag)
                else -> null
            }
            val releasesTask = registerReleasesUploadTask(project, variant, output, bugsnag)

            // Set the manifest info provider to prevent reading
            // the manifest more than once
            proguardTask?.get()?.manifestInfoFile?.set(manifestInfoFile)
            symbolFileTask?.get()?.manifestInfoFile?.set(manifestInfoFile)
            releasesTask.get().manifestInfoFile.set(manifestInfoFile)
        }
    }

    private fun registerManifestUuidTask(
        project: Project,
        variant: ApkVariant,
        output: ApkVariantOutput
    ): Provider<RegularFile> {
        return if (BugsnagManifestUuidTaskV2.isApplicable()) {
            val taskName = computeManifestTaskNameFor(variant.name)
            // This task will have already been created!
            val manifestUpdater = project.tasks
                .withType(BugsnagManifestUuidTaskV2::class.java)
                .named(taskName)
            return manifestUpdater.flatMap(BaseBugsnagManifestUuidTask::manifestInfoProvider)
        } else {
            val taskName = computeManifestTaskNameFor(output.name)
            val manifestInfoOutputFile = project.computeManifestInfoOutputV1(output)
            val buildUuidProvider = project.newUuidProvider()
            project.tasks.register(taskName, BugsnagManifestUuidTask::class.java) {
                it.buildUuid.set(buildUuidProvider)
                it.variantOutput = output
                it.variant = variant
                it.manifestInfoProvider.set(manifestInfoOutputFile)
                val processManifest = output.processManifestProvider.orNull

                if (processManifest != null) {
                    processManifest.finalizedBy(it)
                    it.dependsOn(processManifest)
                }
            }.flatMap(BaseBugsnagManifestUuidTask::manifestInfoProvider)
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
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, bugsnag.isUploadJvmMappings)
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
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, true)
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
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, bugsnag.isReportBuilds)
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
        val ndk = bugsnag.isUploadNdkMappings
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
    internal fun findTaskNamesForPrefix(variant: ApkVariant,
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

    fun taskNameForVariant(variant: ApkVariant): String {
        return variant.name.capitalize()
    }

    fun taskNameForOutput(output: ApkVariantOutput): String {
        return output.name.capitalize()
    }

    private fun computeManifestTaskNameFor(variant: String): String {
        return "processBugsnag${variant.capitalize()}Manifest"
    }

    private fun Project.computeManifestInfoOutputV2(variant: String): Provider<RegularFile> {
        val path = "intermediates/bugsnag/manifestInfoFor${variant.capitalize()}.json"
        return layout.buildDirectory.file(path)
    }

    private fun Project.computeManifestInfoOutputV1(output: ApkVariantOutput): Provider<RegularFile> {
        val path = "intermediates/bugsnag/manifestInfoFor${taskNameForOutput(output)}.json"
        return layout.buildDirectory.file(path)
    }



    private fun Project.newUuidProvider(): Provider<String> {
        return provider { UUID.randomUUID().toString() }
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
}
