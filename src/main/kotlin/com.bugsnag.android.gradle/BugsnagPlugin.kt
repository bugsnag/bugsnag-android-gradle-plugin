package com.bugsnag.android.gradle

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.android.gradle.BugsnagInstallJniLibsTask.Companion.resolveBugsnagArtifacts
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

    private val releasesUploadClient = UploadRequestClient()
    private val proguardUploadClient = UploadRequestClient()
    private val ndkUploadClient = UploadRequestClient()

    override fun apply(project: Project) {
        // After Gradle 5.2, this can use service injection for injecting ObjectFactory
        val bugsnag = project.extensions.create(
            "bugsnag",
            BugsnagPluginExtension::class.java,
            project.objects
        )
        project.pluginManager.withPlugin("com.android.application") {
            if (!bugsnag.enabled.get()) {
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
                registerNdkLibInstallTask(project, bugsnag, android)
            }
        }
    }

    private fun registerNdkLibInstallTask(
        project: Project,
        bugsnag: BugsnagPluginExtension,
        android: AppExtension
    ) {
        val ndkTasks = project.tasks.withType(ExternalNativeBuildTask::class.java)
        val cleanTasks = ndkTasks.filter { it.name.contains(CLEAN_TASK) }.toSet()
        val buildTasks = ndkTasks.filter { !it.name.contains(CLEAN_TASK) }.toSet()

        if (buildTasks.isNotEmpty()) {
            val ndkSetupTask = BugsnagInstallJniLibsTask.register(project, "bugsnagInstallJniLibsTask") {
                val files = resolveBugsnagArtifacts(project)
                bugsnagArtifacts.from(files)
            }

            if (isNdkUploadEnabled(bugsnag, android)) {
                ndkSetupTask.configure {
                    it.mustRunAfter(cleanTasks)
                }
                buildTasks.forEach { it.dependsOn(ndkSetupTask) }
            }
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
            val ndkEnabled = isNdkUploadEnabled(bugsnag, project.extensions.getByType(AppExtension::class.java))

            // skip tasks for variant if JVM/NDK minification not enabled
            if (!jvmMinificationEnabled && !ndkEnabled) {
                return@configureEach
            }

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
            proguardTask?.get()?.let { task ->
                task.manifestInfoFile.set(manifestInfoFile)
                val mappingFileProvider = createMappingFileProvider(project, variant, output)
                task.mappingFileProperty.set(mappingFileProvider)
                releasesTask.get().jvmMappingFileProperty.set(mappingFileProvider)
                task.uploadRequestClient.set(proguardUploadClient)
            }

            symbolFileTask?.get()?.let { task ->
                val ndkSearchDirs = symbolFileTask.get().searchDirectories
                releasesTask.get().ndkMappingFileProperty.set(ndkSearchDirs)
                task.uploadRequestClient.set(ndkUploadClient)
            }
            releasesTask.get().uploadRequestClient.set(releasesUploadClient)
            releasesTask.get().manifestInfoFile.set(manifestInfoFile)
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
        val outputName = taskNameForOutput(output)
        val taskName = "uploadBugsnag${outputName}Mapping"
        val path = "intermediates/bugsnag/requests/proguardFor${outputName}.json"
        val requestOutputFile = project.layout.buildDirectory.file(path)
        return project.tasks.register(taskName, BugsnagUploadProguardTask::class.java) {
            it.requestOutputFile.set(requestOutputFile)
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, bugsnag.uploadJvmMappings.get())
            it.configureWith(bugsnag)
        }
    }

    private fun registerSharedObjectUploadTask(project: Project,
                                               variant: ApkVariant,
                                               output: ApkVariantOutput,
                                               bugsnag: BugsnagPluginExtension): TaskProvider<BugsnagUploadNdkTask> {
        // Create a Bugsnag task to upload NDK mapping file(s)
        val outputName = taskNameForOutput(output)
        val taskName = "uploadBugsnagNdk${outputName}Mapping"
        val path = "intermediates/bugsnag/requests/ndkFor${outputName}.json"
        val requestOutputFile = project.layout.buildDirectory.file(path)
        return project.tasks.register(taskName, BugsnagUploadNdkTask::class.java) {
            it.requestOutputFile.set(requestOutputFile)
            it.projectRoot.set(bugsnag.projectRoot.getOrElse(project.projectDir.toString()))
            it.searchDirectories.set(getSearchDirectories(project, variant))
            it.variantOutput = output
            it.objDumpPaths.set(bugsnag.objdumpPaths)
            addTaskToExecutionGraph(it, variant, output, project, bugsnag, true)
            it.configureWith(bugsnag)
        }
    }

    private fun registerReleasesUploadTask(
        project: Project,
        variant: ApkVariant,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension
    ): TaskProvider<out BugsnagReleasesTask> {
        val outputName = taskNameForOutput(output)
        val taskName = "bugsnagRelease${outputName}Task"
        val path = "intermediates/bugsnag/requests/releasesFor${outputName}.json"
        val requestOutputFile = project.layout.buildDirectory.file(path)
        return BugsnagReleasesTask.register(project, taskName) {
            this.requestOutputFile.set(requestOutputFile)
            retryCount.set(bugsnag.retryCount)
            timeoutMillis.set(bugsnag.requestTimeoutMs)
            releasesEndpoint.set(bugsnag.releasesEndpoint)
            sourceControlProvider.set(bugsnag.sourceControl.provider)
            sourceControlRepository.set(bugsnag.sourceControl.repository)
            sourceControlRevision.set(bugsnag.sourceControl.revision)
            metadata.set(bugsnag.metadata)
            builderName.set(bugsnag.builderName)
            gradleVersion.set(project.gradle.gradleVersion)
            addTaskToExecutionGraph(this, variant, output, project, bugsnag, bugsnag.reportBuilds.get())
            configureMetadata()
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
        return !output.name.toLowerCase().endsWith("debug") || bugsnag.uploadDebugBuildMappings.get()
    }

    private fun isNdkUploadEnabled(bugsnag: BugsnagPluginExtension,
                                   android: AppExtension): Boolean {
        val usesCmake = android.externalNativeBuild.cmake.path != null
        val usesNdkBuild = android.externalNativeBuild.ndkBuild.path != null
        val default = usesCmake || usesNdkBuild
        return bugsnag.uploadNdkMappings.getOrElse(default)
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
        val bugsnag = extensions.findByType(BugsnagPluginExtension::class.java)!!
        return provider {
            when {
                bugsnag.autoUpdateBuildUuid.get() -> UUID.randomUUID().toString()
                else -> AndroidManifestParser.IGNORE_BUILD_UUID
            }
        }
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
