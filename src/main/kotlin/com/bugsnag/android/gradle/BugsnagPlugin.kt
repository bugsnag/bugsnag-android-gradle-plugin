package com.bugsnag.android.gradle

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.android.gradle.BugsnagInstallJniLibsTask.Companion.resolveBugsnagArtifacts
import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.NDK_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.TASK_JNI_LIBS
import com.bugsnag.android.gradle.internal.UNITY_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.computeManifestInfoOutputV1
import com.bugsnag.android.gradle.internal.computeManifestInfoOutputV2
import com.bugsnag.android.gradle.internal.hasDexguardPlugin
import com.bugsnag.android.gradle.internal.intermediateForGenerateJvmMapping
import com.bugsnag.android.gradle.internal.intermediateForMappingFileRequest
import com.bugsnag.android.gradle.internal.intermediateForNdkSoRequest
import com.bugsnag.android.gradle.internal.intermediateForReleaseRequest
import com.bugsnag.android.gradle.internal.intermediateForUnitySoRequest
import com.bugsnag.android.gradle.internal.intermediateForUploadSourcemaps
import com.bugsnag.android.gradle.internal.newUploadRequestClientProvider
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.taskNameForGenerateJvmMapping
import com.bugsnag.android.gradle.internal.taskNameForGenerateNdkMapping
import com.bugsnag.android.gradle.internal.taskNameForGenerateUnityMapping
import com.bugsnag.android.gradle.internal.taskNameForManifestUuid
import com.bugsnag.android.gradle.internal.taskNameForUploadJvmMapping
import com.bugsnag.android.gradle.internal.taskNameForUploadNdkMapping
import com.bugsnag.android.gradle.internal.taskNameForUploadRelease
import com.bugsnag.android.gradle.internal.taskNameForUploadSourcemaps
import com.bugsnag.android.gradle.internal.taskNameForUploadUnityMapping
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File
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
    }

    @Suppress("LongMethod")
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

            val httpClientHelperProvider = BugsnagHttpClientHelper.create(
                project,
                bugsnag
            )

            val releasesUploadClientProvider = newUploadRequestClientProvider(project, "releases")
            val proguardUploadClientProvider = newUploadRequestClientProvider(project, "proguard")
            val ndkUploadClientProvider = newUploadRequestClientProvider(project, "ndk")
            val unityUploadClientProvider = newUploadRequestClientProvider(project, "unity")

            val android = project.extensions.getByType(AppExtension::class.java)
            if (BugsnagManifestUuidTaskV2.isApplicable()) {
                check(android is CommonExtension<*, *, *, *, *, *, *, *>)
                android.onVariants {
                    if (!bugsnag.enabled.get()) {
                        return@onVariants
                    }
                    val variant = VariantFilterImpl(name)
                    if (!isVariantEnabled(bugsnag, variant)) {
                        return@onVariants
                    }
                    val variantName = name
                    val taskName = taskNameForManifestUuid(variantName)
                    val manifestInfoOutputFile = project.computeManifestInfoOutputV2(variantName)
                    val buildUuidProvider = project.newUuidProvider()
                    val manifestUpdater = project.tasks.register(
                        taskName,
                        BugsnagManifestUuidTaskV2::class.java
                    ) {
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
                if (!bugsnag.enabled.get()) {
                    return@afterEvaluate
                }
                android.applicationVariants.configureEach { variant ->
                    val filterImpl = VariantFilterImpl(variant.name)
                    if (!isVariantEnabled(bugsnag, filterImpl)) {
                        return@configureEach
                    }
                    registerBugsnagTasksForVariant(
                        project,
                        android,
                        variant,
                        bugsnag,
                        httpClientHelperProvider,
                        releasesUploadClientProvider,
                        proguardUploadClientProvider,
                        ndkUploadClientProvider,
                        unityUploadClientProvider
                    )
                }
                registerNdkLibInstallTask(project)
            }
        }
    }

    private fun isVariantEnabled(
        bugsnag: BugsnagPluginExtension,
        variant: VariantFilterImpl
    ): Boolean {
        bugsnag.filter.execute(variant)
        return variant.variantEnabled ?: true
    }

    private fun registerNdkLibInstallTask(project: Project) {
        val ndkTasks = project.tasks.withType(ExternalNativeBuildTask::class.java)
        val cleanTasks = ndkTasks.filter { it.name.contains(CLEAN_TASK) }.toSet()
        val buildTasks = ndkTasks.filter { !it.name.contains(CLEAN_TASK) }.toSet()

        if (buildTasks.isNotEmpty()) {
            val ndkSetupTask = BugsnagInstallJniLibsTask.register(project, TASK_JNI_LIBS) {
                val files = resolveBugsnagArtifacts(project)
                bugsnagArtifacts.from(files)
            }
            ndkSetupTask.configure { it.mustRunAfter(cleanTasks) }
            buildTasks.forEach { it.dependsOn(ndkSetupTask) }
        }
    }

    /**
     * Register manifest UUID writing + upload tasks for each Build Variant.
     *
     * See https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
     */
    @Suppress("LongParameterList", "LongMethod", "ComplexMethod")
    private fun registerBugsnagTasksForVariant(
        project: Project,
        android: AppExtension,
        variant: ApkVariant,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        releasesUploadClientProvider: Provider<out UploadRequestClient>,
        proguardUploadClientProvider: Provider<out UploadRequestClient>,
        ndkUploadClientProvider: Provider<out UploadRequestClient>,
        unityUploadClientProvider: Provider<out UploadRequestClient>
    ) {
        variant.outputs.configureEach { output ->
            check(output is ApkVariantOutput) {
                "Expected variant output to be ApkVariantOutput but found ${output.javaClass}"
            }
            val jvmMinificationEnabled = project.isJvmMinificationEnabled(variant)
            val ndkEnabled = isNdkUploadEnabled(bugsnag, android)
            val unityEnabled = isUnityLibraryUploadEnabled(bugsnag, android)
            val reactNativeEnabled = isReactNativeUploadEnabled(bugsnag)

            // skip tasks for variant if JVM/NDK/Unity minification not enabled
            if (!jvmMinificationEnabled && !ndkEnabled && !unityEnabled && !reactNativeEnabled) {
                return@configureEach
            }

            // register bugsnag tasks
            val manifestInfoFileProvider = registerManifestUuidTask(project, variant, output)
            val mappingFilesProvider = createMappingFileProvider(project, variant, output, android)

            val generateProguardTaskProvider = when {
                jvmMinificationEnabled -> registerGenerateProguardTask(
                    project,
                    output,
                    bugsnag,
                    manifestInfoFileProvider,
                    mappingFilesProvider
                )
                else -> null
            }

            val uploadProguardTaskProvider = when {
                jvmMinificationEnabled -> registerUploadProguardTask(
                    project,
                    output,
                    bugsnag,
                    httpClientHelperProvider,
                    manifestInfoFileProvider,
                    proguardUploadClientProvider,
                    generateProguardTaskProvider
                )
                else -> null
            }
            val generateNdkMappingProvider = when {
                ndkEnabled -> registerGenerateSoMappingTask(
                    project,
                    variant,
                    output,
                    bugsnag,
                    android,
                    manifestInfoFileProvider
                )
                else -> null
            }
            val uploadNdkMappingProvider = when {
                ndkEnabled && generateNdkMappingProvider != null -> {
                    registerUploadNdkTask(
                        project,
                        output,
                        bugsnag,
                        httpClientHelperProvider,
                        manifestInfoFileProvider,
                        ndkUploadClientProvider,
                        generateNdkMappingProvider
                    )
                }
                else -> null
            }

            val generateUnityMappingProvider = when {
                unityEnabled -> registerGenerateUnityMappingTask(
                    project,
                    output,
                    bugsnag,
                    manifestInfoFileProvider
                )
                else -> null
            }
            val uploadUnityMappingProvider = when {
                unityEnabled && generateUnityMappingProvider != null -> {
                    registerUploadUnityTask(
                        project,
                        output,
                        bugsnag,
                        httpClientHelperProvider,
                        manifestInfoFileProvider,
                        unityUploadClientProvider,
                        generateUnityMappingProvider
                    )
                }
                else -> null
            }

            val uploadSourceMapProvider = when {
                reactNativeEnabled -> registerUploadSourceMapTask(
                    project,
                    variant,
                    output,
                    bugsnag,
                    manifestInfoFileProvider
                )
                else -> null
            }

            val releaseUploadTask = registerReleasesUploadTask(
                project,
                variant,
                output,
                bugsnag,
                manifestInfoFileProvider,
                releasesUploadClientProvider,
                mappingFilesProvider,
                generateNdkMappingProvider != null,
                httpClientHelperProvider
            )

            val releaseAutoUpload = bugsnag.reportBuilds.get()
            variant.register(project, releaseUploadTask, releaseAutoUpload)

            if (generateNdkMappingProvider != null && uploadNdkMappingProvider != null) {
                variant.register(project, generateNdkMappingProvider, ndkEnabled)
                variant.register(project, uploadNdkMappingProvider, ndkEnabled)
            }
            if (generateUnityMappingProvider != null && uploadUnityMappingProvider != null) {
                variant.register(project, generateUnityMappingProvider, unityEnabled)
                variant.register(project, uploadUnityMappingProvider, unityEnabled)
            }
            if (generateProguardTaskProvider != null && uploadProguardTaskProvider != null) {
                val jvmAutoUpload = bugsnag.uploadJvmMappings.get()
                variant.register(project, generateProguardTaskProvider, jvmAutoUpload)
                variant.register(project, uploadProguardTaskProvider, jvmAutoUpload)
            }
            if (uploadSourceMapProvider != null) {
                variant.register(project, uploadSourceMapProvider, reactNativeEnabled)
            }
            addReactNativeMavenRepo(project, bugsnag)
        }
    }

    /**
     * If the project uses react-native, this adds the node_module directory
     * containing the Android AARs as a maven repository. This allows the
     * project to compile without the user explicitly adding the maven repository.
     */
    private fun addReactNativeMavenRepo(project: Project, bugsnag: BugsnagPluginExtension) {
        val props = project.extensions.extraProperties
        val hasReact = props.has("react")
        if (hasReact) {
            project.rootProject.allprojects { subProj ->
                val defaultNodeModulesDir = File("${subProj.rootDir}/../node_modules")
                val nodeModulesDir = bugsnag.nodeModulesDir.getOrElse(defaultNodeModulesDir)

                subProj.repositories.maven { repo ->
                    repo.setUrl("$nodeModulesDir/@bugsnag/react-native/android")
                }
            }
        }
    }

    private fun Project.isJvmMinificationEnabled(variant: ApkVariant) =
        variant.buildType.isMinifyEnabled || hasDexguardPlugin()

    private fun registerManifestUuidTask(
        project: Project,
        variant: ApkVariant,
        output: ApkVariantOutput
    ): Provider<RegularFile> {
        return if (BugsnagManifestUuidTaskV2.isApplicable()) {
            val taskName = taskNameForManifestUuid(variant.name)
            // This task will have already been created!
            val manifestUpdater = project.tasks
                .withType(BugsnagManifestUuidTaskV2::class.java)
                .named(taskName)
            return manifestUpdater.flatMap(BaseBugsnagManifestUuidTask::manifestInfoProvider)
        } else {
            val taskName = taskNameForManifestUuid(output.name)
            val manifestInfoOutputFile = project.computeManifestInfoOutputV1(output)
            val buildUuidProvider = project.newUuidProvider()

            val manifestTask = project.tasks.register(taskName, BugsnagManifestUuidTask::class.java) {
                it.buildUuid.set(buildUuidProvider)
                it.variantOutput = output
                it.variant = variant
                it.manifestInfoProvider.set(manifestInfoOutputFile)
                it.dependsOn(output.processManifestProvider)
                it.mustRunAfter(output.processManifestProvider)
            }

            // Enforces correct task ordering. The manifest can only be edited inbetween
            // when the merged manifest is generated (processManifestProvider) and when
            // the merged manifest is copied for use in packaging the artifact (processResourcesProvider).
            // This ensures BugsnagManifestUuidTask runs at the correct time for both tasks.
            //
            // https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:ordering_tasks
            output.processResourcesProvider.configure {
                it.mustRunAfter(manifestTask)
            }
            output.processManifestProvider.configure {
                // Trigger eager configuration of the manifest task. This creates the task
                // and ensures that it is configured whenever the manifest is processed
                // and avoids mutating the task directly which should be avoided.
                //
                // https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
                // #sec:task_configuration_avoidance_general
                manifestTask.get()
            }
            manifestTask.flatMap(BaseBugsnagManifestUuidTask::manifestInfoProvider)
        }
    }

    /**
     * Creates a bugsnag task to compress JVM mapping files
     */
    @Suppress("LongParameterList")
    private fun registerGenerateProguardTask(
        project: Project,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        manifestInfoFileProvider: Provider<RegularFile>,
        mappingFilesProvider: Provider<FileCollection>
    ): TaskProvider<out BugsnagGenerateProguardTask> {
        val taskName = taskNameForGenerateJvmMapping(output)
        val gzipOutputProvider = intermediateForGenerateJvmMapping(project, output)

        return BugsnagGenerateProguardTask.register(project, taskName) {
            manifestInfoFile.set(manifestInfoFileProvider)
            archiveOutputFile.set(gzipOutputProvider)
            failOnUploadError.set(bugsnag.failOnUploadError)

            mappingFilesProvider.let {
                mappingFileProperty.from(it)
            }
        }
    }

    /**
     * Creates a bugsnag task to upload JVM mapping files
     */
    @Suppress("LongParameterList")
    private fun registerUploadProguardTask(
        project: Project,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        manifestInfoFileProvider: Provider<RegularFile>,
        proguardUploadClientProvider: Provider<out UploadRequestClient>,
        generateProguardTaskProvider: TaskProvider<out BugsnagGenerateProguardTask>?
    ): TaskProvider<out BugsnagUploadProguardTask> {
        val taskName = taskNameForUploadJvmMapping(output)
        val requestOutputFileProvider = intermediateForMappingFileRequest(project, output)
        val gzipOutputProvider = intermediateForGenerateJvmMapping(project, output)

        return BugsnagUploadProguardTask.register(project, taskName) {
            requestOutputFile.set(requestOutputFileProvider)
            httpClientHelper.set(httpClientHelperProvider)
            manifestInfoFile.set(manifestInfoFileProvider)
            uploadRequestClient.set(proguardUploadClientProvider)
            mappingFileProperty.set(gzipOutputProvider)
            configureWith(bugsnag)

            val task = generateProguardTaskProvider?.get()
            mustRunAfter(task)
            dependsOn(task)
        }
    }

    /**
     * Creates a bugsnag task to upload JS source maps
     */
    private fun registerUploadSourceMapTask(
        project: Project,
        variant: ApkVariant,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        manifestInfoFileProvider: Provider<RegularFile>
    ): TaskProvider<out BugsnagUploadJsSourceMapTask>? {
        val taskName = taskNameForUploadSourcemaps(output)
        val path = intermediateForUploadSourcemaps(project, output)

        // lookup the react-native task by its name
        // https://github.com/facebook/react-native/blob/master/react.gradle#L132
        val rnTaskName = "bundle${variant.name.capitalize()}JsAndAssets"
        val rnTask: Task = project.tasks.findByName(rnTaskName) ?: return null
        val rnSourceMap = BugsnagUploadJsSourceMapTask.findReactNativeTaskArg(rnTask, "--sourcemap-output")
        val rnBundle = BugsnagUploadJsSourceMapTask.findReactNativeTaskArg(rnTask, "--bundle-output")
        val dev = BugsnagUploadJsSourceMapTask.findReactNativeTaskArg(rnTask, "--dev")

        if (rnSourceMap == null || rnBundle == null || dev == null) {
            project.logger.error("Bugsnag: unable to upload JS sourcemaps. Please enable sourcemap + bundle output.")
            return null
        }

        return BugsnagUploadJsSourceMapTask.register(project, taskName) {
            requestOutputFile.set(path)
            manifestInfoFile.set(manifestInfoFileProvider)
            bundleJsFileProvider.set(File(rnBundle))
            sourceMapFileProvider.set(File(rnSourceMap))
            overwrite.set(bugsnag.overwrite)
            endpoint.set(bugsnag.endpoint)
            devEnabled.set("true" == dev)
            failOnUploadError.set(bugsnag.failOnUploadError)

            val jsProjectRoot = project.rootProject.rootDir.parentFile
            projectRootFileProvider.from(jsProjectRoot)

            val defaultLocation = File(project.projectDir.parentFile.parentFile, "node_modules")
            val nodeModulesDir = bugsnag.nodeModulesDir.getOrElse(defaultLocation)
            val cliPath = File(nodeModulesDir, "@bugsnag/source-maps/bin/cli")
            bugsnagSourceMaps.set(cliPath)
            mustRunAfter(rnTask)
        }
    }

    @Suppress("LongParameterList")
    private fun registerGenerateSoMappingTask(
        project: Project,
        variant: ApkVariant,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        android: AppExtension,
        manifestInfoFileProvider: Provider<RegularFile>
    ): TaskProvider<out BugsnagGenerateNdkSoMappingTask> {
        // Create a Bugsnag task to upload NDK mapping file(s)
        val taskName = taskNameForGenerateNdkMapping(output)
        return BugsnagGenerateNdkSoMappingTask.register(project, taskName) {
            variantOutput = output
            objDumpPaths.set(bugsnag.objdumpPaths)
            manifestInfoFile.set(manifestInfoFileProvider)

            val searchPaths = getSharedObjectSearchPaths(project, bugsnag, android)
            searchDirectories.from(searchPaths)
            variant.externalNativeBuildProviders.forEach { provider ->
                searchDirectories.from(provider.map(ExternalNativeBuildTask::objFolder))
                searchDirectories.from(provider.map(ExternalNativeBuildTask::soFolder))
            }
        }
    }

    @Suppress("LongParameterList")
    private fun registerGenerateUnityMappingTask(
        project: Project,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        manifestInfoFileProvider: Provider<RegularFile>
    ): TaskProvider<out BugsnagGenerateUnitySoMappingTask> {
        // Create a Bugsnag task to upload Unity mapping file(s)
        val taskName = taskNameForGenerateUnityMapping(output)
        return BugsnagGenerateUnitySoMappingTask.register(project, taskName) {
            variantOutput = output
            objDumpPaths.set(bugsnag.objdumpPaths)
            manifestInfoFile.set(manifestInfoFileProvider)
            rootProjectDir.set(project.rootProject.projectDir)
        }
    }

    @Suppress("LongParameterList")
    private fun registerUploadNdkTask(
        project: Project,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        manifestInfoFileProvider: Provider<RegularFile>,
        ndkUploadClientProvider: Provider<out UploadRequestClient>,
        generateTaskProvider: TaskProvider<out BugsnagGenerateNdkSoMappingTask>
    ): TaskProvider<out BugsnagUploadSharedObjectTask> {
        return registerSharedObjectUploadTask(
            project,
            generateTaskProvider,
            bugsnag,
            httpClientHelperProvider,
            manifestInfoFileProvider,
            ndkUploadClientProvider,
            taskNameForUploadNdkMapping(output),
            intermediateForNdkSoRequest(project, output),
            BugsnagUploadSharedObjectTask.UploadType.NDK,
            NDK_SO_MAPPING_DIR
        )
    }

    @Suppress("LongParameterList")
    private fun registerUploadUnityTask(
        project: Project,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        manifestInfoFileProvider: Provider<RegularFile>,
        ndkUploadClientProvider: Provider<out UploadRequestClient>,
        generateTaskProvider: TaskProvider<out BugsnagGenerateUnitySoMappingTask>
    ): TaskProvider<out BugsnagUploadSharedObjectTask> {
        return registerSharedObjectUploadTask(
            project,
            generateTaskProvider,
            bugsnag,
            httpClientHelperProvider,
            manifestInfoFileProvider,
            ndkUploadClientProvider,
            taskNameForUploadUnityMapping(output),
            intermediateForUnitySoRequest(project, output),
            BugsnagUploadSharedObjectTask.UploadType.UNITY,
            UNITY_SO_MAPPING_DIR
        )
    }

    @Suppress("LongParameterList")
    private fun registerSharedObjectUploadTask(
        project: Project,
        generateTaskProvider: TaskProvider<out Task>,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        manifestInfoFileProvider: Provider<RegularFile>,
        ndkUploadClientProvider: Provider<out UploadRequestClient>,
        taskName: String,
        requestOutputFile: Provider<RegularFile>,
        uploadType: BugsnagUploadSharedObjectTask.UploadType,
        intermediateOutputPath: String
    ): TaskProvider<BugsnagUploadSharedObjectTask> {
        // Create a Bugsnag task to upload NDK mapping file(s)
        return BugsnagUploadSharedObjectTask.register(project, taskName) {
            // upload task requires SO mapping generation to occur first
            this.dependsOn(generateTaskProvider)
            this.requestOutputFile.set(requestOutputFile)
            this.uploadType.set(uploadType)
            projectRoot.set(bugsnag.projectRoot.getOrElse(project.projectDir.toString()))
            httpClientHelper.set(httpClientHelperProvider)
            manifestInfoFile.set(manifestInfoFileProvider)
            uploadRequestClient.set(ndkUploadClientProvider)
            intermediateOutputDir.set(project.layout.buildDirectory.dir(intermediateOutputPath))
            configureWith(bugsnag)
        }
    }

    @Suppress("LongParameterList")
    private fun registerReleasesUploadTask(
        project: Project,
        variant: ApkVariant,
        output: ApkVariantOutput,
        bugsnag: BugsnagPluginExtension,
        manifestInfoFileProvider: Provider<RegularFile>,
        releasesUploadClientProvider: Provider<out UploadRequestClient>,
        mappingFilesProvider: Provider<FileCollection>?,
        checkSearchDirectories: Boolean,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>
    ): TaskProvider<out BugsnagReleasesTask> {
        val taskName = taskNameForUploadRelease(output)
        val requestOutputFile = intermediateForReleaseRequest(project, output)
        return BugsnagReleasesTask.register(project, taskName) {
            this.requestOutputFile.set(requestOutputFile)
            httpClientHelper.set(httpClientHelperProvider)
            retryCount.set(bugsnag.retryCount)
            timeoutMillis.set(bugsnag.requestTimeoutMs)
            failOnUploadError.set(bugsnag.failOnUploadError)
            releasesEndpoint.set(bugsnag.releasesEndpoint)
            sourceControlProvider.set(bugsnag.sourceControl.provider)
            sourceControlRepository.set(bugsnag.sourceControl.repository)
            sourceControlRevision.set(bugsnag.sourceControl.revision)
            metadata.set(bugsnag.metadata)
            builderName.set(bugsnag.builderName)
            gradleVersion.set(project.gradle.gradleVersion)
            manifestInfoFile.set(manifestInfoFileProvider)
            uploadRequestClient.set(releasesUploadClientProvider)

            if (project.isJvmMinificationEnabled(variant)) {
                mappingFilesProvider?.let {
                    jvmMappingFileProperty.from(it)
                }
            }
            if (checkSearchDirectories) {
                variant.externalNativeBuildProviders.forEach { task ->
                    ndkMappingFileProperty.from(task.map { it.objFolder })
                    ndkMappingFileProperty.from(task.map { it.soFolder })
                }
            }
            configureMetadata()
        }
    }

    /**
     * Determines whether SO mapping files should be generated for the
     * libunity.so file in Unity projects.
     */
    @Suppress("SENSELESS_COMPARISON")
    internal fun isUnityLibraryUploadEnabled(
        bugsnag: BugsnagPluginExtension,
        android: AppExtension
    ): Boolean {
        val enabled = bugsnag.uploadNdkUnityLibraryMappings.orNull
        return when {
            enabled != null -> enabled
            else -> {
                // workaround to avoid exception as noCompress was null until AGP 4.1
                runCatching {
                    val clz = android.aaptOptions.javaClass
                    val method = clz.getMethod("getNoCompress")
                    val noCompress = method.invoke(android.aaptOptions)
                    if (noCompress is Collection<*>) {
                        return noCompress.contains(".unity3d")
                    }
                }
                return false
            }
        }
    }

    internal fun isNdkUploadEnabled(
        bugsnag: BugsnagPluginExtension,
        android: AppExtension
    ): Boolean {
        val usesCmake = android.externalNativeBuild.cmake.path != null
        val usesNdkBuild = android.externalNativeBuild.ndkBuild.path != null
        val unityEnabled = isUnityLibraryUploadEnabled(bugsnag, android)
        val default = usesCmake || usesNdkBuild || unityEnabled
        return bugsnag.uploadNdkMappings.getOrElse(default)
    }

    internal fun isReactNativeUploadEnabled(
        bugsnag: BugsnagPluginExtension
    ): Boolean {
        return bugsnag.uploadReactNativeMappings.getOrElse(false)
    }

    /**
     * Gets the directories which should be searched for NDK shared objects.
     * By default this is set to an empty list, and paths set by the user
     * via the Bugsnag plugin extension will be included.
     *
     * If the project is a Unity app then additional search paths are added
     * which cover the default location of NDK SO files in Unity.
     */
    internal fun getSharedObjectSearchPaths(
        project: Project,
        bugsnag: BugsnagPluginExtension,
        android: AppExtension
    ): List<File> {
        val searchPaths = bugsnag.sharedObjectPaths.get().toMutableList()
        val unityEnabled = isUnityLibraryUploadEnabled(bugsnag, android)
        val ndkEnabled = isNdkUploadEnabled(bugsnag, android)

        if (unityEnabled && ndkEnabled) {
            val unity2019ExportedDir = File(project.rootDir, "unityLibrary/src/main/jniLibs")
            // this directory covers both Unity 2018 exported project structure,
            // and projects built internally in Unity using Gradle.
            val unityDefaultDir = File(project.projectDir, "src/main/jniLibs")
            searchPaths.add(unityDefaultDir)
            searchPaths.add(unity2019ExportedDir)
        }
        return searchPaths
    }

    private fun Project.newUuidProvider(): Provider<String> {
        return provider {
            UUID.randomUUID().toString()
        }
    }
}
