package com.bugsnag.android.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.bugsnag.android.gradle.BugsnagInstallJniLibsTask.Companion.resolveBugsnagArtifacts
import com.bugsnag.android.gradle.internal.AgpVersions
import com.bugsnag.android.gradle.internal.BugsnagHttpClientHelper
import com.bugsnag.android.gradle.internal.ExternalNativeBuildTaskUtil
import com.bugsnag.android.gradle.internal.NDK_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.NdkToolchain
import com.bugsnag.android.gradle.internal.TASK_JNI_LIBS
import com.bugsnag.android.gradle.internal.UNITY_SO_COPY_DIR
import com.bugsnag.android.gradle.internal.UNITY_SO_MAPPING_DIR
import com.bugsnag.android.gradle.internal.UploadRequestClient
import com.bugsnag.android.gradle.internal.dependsOn
import com.bugsnag.android.gradle.internal.getDexguardAabTaskName
import com.bugsnag.android.gradle.internal.hasDexguardPlugin
import com.bugsnag.android.gradle.internal.intermediateForGenerateJvmMapping
import com.bugsnag.android.gradle.internal.intermediateForMappingFileRequest
import com.bugsnag.android.gradle.internal.intermediateForReleaseRequest
import com.bugsnag.android.gradle.internal.intermediateForUploadSourcemaps
import com.bugsnag.android.gradle.internal.isDexguardEnabledForVariant
import com.bugsnag.android.gradle.internal.isVariantEnabled
import com.bugsnag.android.gradle.internal.newUploadRequestClientProvider
import com.bugsnag.android.gradle.internal.register
import com.bugsnag.android.gradle.internal.registerV2ManifestUuidTask
import com.bugsnag.android.gradle.internal.taskNameForManifestUuid
import com.bugsnag.android.gradle.internal.taskNameForUploadJvmMapping
import com.bugsnag.android.gradle.internal.taskNameForUploadRelease
import com.bugsnag.android.gradle.internal.taskNameForUploadSourcemaps
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.StopExecutionException
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
        private const val CLEAN_TASK = "Clean"
    }

    @Suppress("LongMethod")
    override fun apply(project: Project) {
        if (AgpVersions.CURRENT < AgpVersions.VERSION_7_0) {
            throw StopExecutionException(
                "Using com.bugsnag.android.gradle:7+ with Android Gradle Plugin < 7 " +
                    "is not supported. Either upgrade the Android Gradle Plugin to 7, or use an " +
                    "earlier version of the BugSnag Gradle Plugin. " +
                    "For more information about this change, see " +
                    "https://docs.bugsnag.com/build-integrations/gradle/"
            )
        } else if (AgpVersions.CURRENT >= AgpVersions.VERSION_8_0) {
            project.logger.warn(
                "Using com.bugsnag.android.gradle:7+ with Android Gradle Plugin 8+ is not " +
                    "formally supported, and may lead to compatibility errors. " +
                    "For more information, please see " +
                    "https://docs.bugsnag.com/build-integrations/gradle/"
            )
        }

        val bugsnag = project.extensions.create("bugsnag", BugsnagPluginExtension::class.java)

        if (!bugsnag.enabled.get()) {
            return
        }

        runCatching {
            val android = project.extensions.getByType(BaseExtension::class.java)

            project.pluginManager.withPlugin("com.android.library") {
                project.logger.warn(
                    "Bugsnag does not support uploading mapping files or build information from" +
                        " library modules. This should be done from the application module which" +
                        " produces your APK instead."
                )
                project.afterEvaluate {
                    registerNdkLibInstallTask(project)
                }
            }
            project.pluginManager.withPlugin("com.android.application") {
                setupBugsnagPlugin(project, bugsnag, android)
            }
        }
    }

    private fun setupBugsnagPlugin(
        project: Project,
        bugsnag: BugsnagPluginExtension,
        android: BaseExtension
    ) {
        val httpClientHelperProvider = BugsnagHttpClientHelper.create(
            project,
            bugsnag
        )

        val releasesUploadClientProvider = newUploadRequestClientProvider(project, "releases")
        val proguardUploadClientProvider = newUploadRequestClientProvider(project, "proguard")
        val ndkUploadClientProvider = newUploadRequestClientProvider(project, "ndk")
        val unityUploadClientProvider = newUploadRequestClientProvider(project, "unity")

        registerV2ManifestUuidTask(bugsnag, project)

        project.afterEvaluate {
            if (!bugsnag.enabled.get()) {
                return@afterEvaluate
            }

            addReactNativeMavenRepo(project, bugsnag)

            val variants = when (android) {
                is AppExtension -> android.applicationVariants
                is LibraryExtension -> android.libraryVariants
                else -> throw IllegalStateException("Unexpected variant type: $android")
            }
            variants.configureEach { variant ->
                val filterImpl = VariantFilterImpl(variant.name)
                if (!isVariantEnabled(bugsnag, filterImpl)) {
                    return@configureEach
                }
                check(variant is ApkVariant)
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
        android: BaseExtension,
        variant: ApkVariant,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        releasesUploadClientProvider: Provider<out UploadRequestClient>,
        proguardUploadClientProvider: Provider<out UploadRequestClient>,
        ndkUploadClientProvider: Provider<out UploadRequestClient>,
        unityUploadClientProvider: Provider<out UploadRequestClient>
    ) {
        val ndkToolchain by lazy(LazyThreadSafetyMode.NONE) {
            NdkToolchain.configureNdkToolkit(project, bugsnag, variant)
        }

        variant.outputs.configureEach { output ->
            check(output is ApkVariantOutput) {
                "Expected variant output to be ApkVariantOutput but found ${output.javaClass}"
            }
            val jvmMinificationEnabled = project.isJvmMinificationEnabled(variant)
            val ndkEnabled = isNdkUploadEnabled(bugsnag, android)
            val unityEnabled = BugsnagGenerateUnitySoMappingTask.isUnityLibraryUploadEnabled(bugsnag, android)
            val reactNativeEnabled = isReactNativeUploadEnabled(bugsnag)

            // register bugsnag tasks
            val mappingFilesProvider = createMappingFileProvider(project, variant, output, bugsnag.dexguardMajorVersion.orNull)
            val manifestTaskProvider = registerManifestUuidTask(project, output)

            val manifestInfoProvider = manifestTaskProvider
                .flatMap { it.manifestInfoProvider }

            // skip tasks for variant if JVM/NDK/Unity minification not enabled
            if (!jvmMinificationEnabled && !ndkEnabled && !unityEnabled && !reactNativeEnabled) {
                return@configureEach
            }

            val generateProguardTaskProvider = when {
                jvmMinificationEnabled -> BugsnagGenerateProguardTask.register(
                    project,
                    output,
                    bugsnag.failOnUploadError,
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
                    manifestInfoProvider,
                    proguardUploadClientProvider,
                    generateProguardTaskProvider
                ).dependsOn(manifestTaskProvider)

                else -> null
            }
            val ndkSoMappingOutput = "$NDK_SO_MAPPING_DIR/${output.name}"
            val generateNdkMappingProvider = when {
                ndkEnabled -> // Create a Bugsnag task to upload NDK mapping file(s)
                    BugsnagGenerateNdkSoMappingTask.register(
                        project,
                        variant,
                        output,
                        ndkToolchain,
                        getSharedObjectSearchPaths(project, bugsnag, android),
                        ndkSoMappingOutput
                    )

                else -> null
            }
            val uploadNdkMappingProvider = when {
                ndkEnabled && generateNdkMappingProvider != null -> BugsnagUploadSoSymTask.register(
                    project,
                    output,
                    ndkToolchain,
                    BugsnagUploadSoSymTask.UploadType.NDK,
                    generateNdkMappingProvider,
                    httpClientHelperProvider,
                    ndkUploadClientProvider
                )

                else -> null
            }

            val unityMappingDir = "$UNITY_SO_MAPPING_DIR/${output.name}"
            val generateUnityMappingProvider = when {
                unityEnabled ->
                    // Create a Bugsnag task to upload Unity mapping file(s)
                    BugsnagGenerateUnitySoMappingTask.register(
                        project,
                        output,
                        ndkToolchain,
                        unityMappingDir,
                        "$UNITY_SO_COPY_DIR/${output.name}"
                    )

                else -> null
            }
            val uploadUnityMappingProvider = when {
                unityEnabled && generateUnityMappingProvider != null -> {
                    BugsnagUploadSoSymTask.register(
                        project,
                        output,
                        ndkToolchain,
                        BugsnagUploadSoSymTask.UploadType.UNITY,
                        generateUnityMappingProvider,
                        httpClientHelperProvider,
                        unityUploadClientProvider
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
                    manifestInfoProvider
                )?.dependsOn(manifestTaskProvider)

                else -> null
            }

            val releaseUploadTask = registerReleasesUploadTask(
                project,
                variant,
                output,
                bugsnag,
                manifestInfoProvider,
                releasesUploadClientProvider,
                mappingFilesProvider,
                ndkEnabled,
                httpClientHelperProvider
            ).dependsOn(manifestTaskProvider)

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

                // DexGuard 9 runs as part of the bundle task supplied by AGP,
                // so need to alter task dependency so that BAGP always runs
                // after DexGuard
                if (project.hasDexguardPlugin() && project.isDexguardEnabledForVariant(variant)) {
                    val taskName = getDexguardAabTaskName(variant)
                    releaseUploadTask.configure {
                        it.dependsOn(generateProguardTaskProvider)
                    }

                    generateProguardTaskProvider.configure {
                        it.dependsOn(project.tasks.named(taskName))
                    }
                }
            }
            if (uploadSourceMapProvider != null) {
                variant.register(project, uploadSourceMapProvider, reactNativeEnabled)
            }

            try {
                project.tasks.named("installRelease").configure {
                    if (reactNativeEnabled) {
                        project.logger.warn(
                            "Bugsnag: JS sourcemaps and JVM/NDK mapping files are not uploaded when " +
                                "using the react-native CLI (e.g. react-native run-android --variant=release). " +
                                "You should generate a release APK using ./gradlew assembleRelease or " +
                                "./gradlew bundleRelease instead. See the React Native docs for further info: " +
                                "https://reactnative.dev/docs/signed-apk-android#generating-the-release-apk"
                        )
                    }
                }
            } catch (ignored: UnknownTaskException) {
            }
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
        // Expo projects are ReactNative projects but *do not* depend on @bugsnag/react-native
        // Expo doesn't define any good indicators within the Gradle project, so we try look for a well known
        // property that it normally defines, and have a slightly slower per-project fallback
        val hasExpo = project.hasProperty("expo.jsEngine")
        if (hasReact && !hasExpo) {
            project.rootProject.allprojects { subProj ->
                val defaultNodeModulesDir = File("${subProj.rootDir}/../node_modules")
                val nodeModulesDir = bugsnag.nodeModulesDir.getOrElse(defaultNodeModulesDir)
                if (!nodeModulesDir.exists()) {
                    throw StopExecutionException(
                        "Cannot find node_modules directory at: ${nodeModulesDir.absolutePath} " +
                            "To set this to the correct path manually, please see: " +
                            "https://docs.bugsnag.com/build-integrations/gradle/#custom-node_modules-directory"
                    )
                }

                val bugsnagModuleDir = File(nodeModulesDir, "@bugsnag/react-native/android")
                // if the @expo modules are installed, we don't require @bugsnag/react-native
                val expoModulesDir = File(nodeModulesDir, "@expo")
                if (!bugsnagModuleDir.exists() && !expoModulesDir.exists()) {
                    throw StopExecutionException(
                        "Cannot find the @bugsnag/react-native module in your node_modules directory. " +
                            "Manual installation instructions can be found here: " +
                            "https://docs.bugsnag.com/platforms/react-native/react-native/manual-setup/#installation"
                    )
                }

                if (bugsnagModuleDir.exists()) {
                    subProj.repositories.maven { repo ->
                        repo.setUrl(bugsnagModuleDir.toString())
                    }
                }
            }
        }
    }

    private fun Project.isJvmMinificationEnabled(variant: BaseVariant) =
        variant.buildType.isMinifyEnabled || hasDexguardPlugin()

    private fun registerManifestUuidTask(
        project: Project,
        variant: BaseVariantOutput
    ): TaskProvider<BugsnagManifestUuidTask> {
        val taskName = taskNameForManifestUuid(variant.name)
        // This task will have already been created!
        val manifestUpdater = project.tasks
            .withType(BugsnagManifestUuidTask::class.java)
            .named(taskName)
        return manifestUpdater
    }

    /**
     * Creates a bugsnag task to upload JVM mapping files
     */
    @Suppress("LongParameterList")
    private fun registerUploadProguardTask(
        project: Project,
        output: BaseVariantOutput,
        bugsnag: BugsnagPluginExtension,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>,
        manifestInfoProvider: Provider<RegularFile>,
        proguardUploadClientProvider: Provider<out UploadRequestClient>,
        generateProguardTaskProvider: TaskProvider<out BugsnagGenerateProguardTask>?
    ): TaskProvider<out BugsnagUploadProguardTask> {
        val taskName = taskNameForUploadJvmMapping(output)
        val requestOutputFileProvider = intermediateForMappingFileRequest(project, output)
        val gzipOutputProvider = intermediateForGenerateJvmMapping(project, output)

        return BugsnagUploadProguardTask.register(project, taskName) {
            requestOutputFile.set(requestOutputFileProvider)
            manifestInfo.set(manifestInfoProvider)
            mappingFileProperty.set(gzipOutputProvider)

            uploadRequestClient.set(proguardUploadClientProvider)
            usesService(proguardUploadClientProvider)

            configureWith(bugsnag, httpClientHelperProvider)

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
        variant: BaseVariant,
        output: BaseVariantOutput,
        bugsnag: BugsnagPluginExtension,
        manifestInfoProvider: Provider<RegularFile>
    ): TaskProvider<out BugsnagUploadJsSourceMapTask>? {
        val taskName = taskNameForUploadSourcemaps(output)
        val path = intermediateForUploadSourcemaps(project, output)

        // lookup the react-native task by its name
        // https://github.com/facebook/react-native/blob/master/react.gradle#L132
        val rnTaskName = "bundle${variant.name.capitalize()}JsAndAssets"
        val rnTask: Task? = project.tasks.findByName(rnTaskName)
        if (rnTask == null) {
            project.logger.error("Bugsnag: unable to find ReactNative bundle task '$rnTaskName'")
            return null
        }

        val rnSourceMap = findReactNativeSourcemapFile(project, variant)
        var rnBundle =
            BugsnagUploadJsSourceMapTask.findReactNativeTaskArg(rnTask, "--bundle-output")
        val dev = BugsnagUploadJsSourceMapTask.findReactNativeTaskArg(rnTask, "--dev")

        if (rnBundle == null || dev == null) {
            project.logger.error("Bugsnag: unable to upload JS sourcemaps. Please enable sourcemap + bundle output.")
            return null
        }

        val enabledHermes = BugsnagUploadJsSourceMapTask.isHermesEnabled(project)
        if (enabledHermes) {
            rnBundle = BugsnagUploadJsSourceMapTask.rescueReactNativeSourceBundle(
                rnTask,
                rnBundle,
                project,
                output
            )
        }

        return BugsnagUploadJsSourceMapTask.register(project, taskName) {
            requestOutputFile.set(path)
            manifestInfo.set(manifestInfoProvider)
            bundleJsFileProvider.set(File(rnBundle))
            sourceMapFileProvider.set(File(rnSourceMap))
            overwrite.set(bugsnag.overwrite)
            endpoint.set(bugsnag.endpoint.get())
            devEnabled.set("true" == dev)
            failOnUploadError.set(bugsnag.failOnUploadError)

            val jsProjectRoot = project.rootProject.rootDir.parentFile
            projectRootFileProvider.from(jsProjectRoot)

            val defaultLocation = File(project.projectDir.parentFile.parentFile, "node_modules")
            val nodeModulesDir = bugsnag.nodeModulesDir.getOrElse(defaultLocation)
            val cliPath = File(nodeModulesDir, "@bugsnag/source-maps/bin/cli")
            bugsnagSourceMaps.set(cliPath)
            dependsOn(rnTask)
        }
    }

    @Suppress("LongParameterList")
    private fun registerReleasesUploadTask(
        project: Project,
        variant: BaseVariant,
        output: BaseVariantOutput,
        bugsnag: BugsnagPluginExtension,
        manifestInfoProvider: Provider<RegularFile>,
        releasesUploadClientProvider: Provider<out UploadRequestClient>,
        mappingFilesProvider: Provider<FileCollection>?,
        checkSearchDirectories: Boolean,
        httpClientHelperProvider: Provider<out BugsnagHttpClientHelper>
    ): TaskProvider<out BugsnagReleasesTask> {
        val taskName = taskNameForUploadRelease(output)
        val requestOutputFile = intermediateForReleaseRequest(project, output)
        return BugsnagReleasesTask.register(project, taskName) {
            usesService(httpClientHelperProvider)
            usesService(releasesUploadClientProvider)

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
            manifestInfo.set(manifestInfoProvider)
            uploadRequestClient.set(releasesUploadClientProvider)

            if (project.isJvmMinificationEnabled(variant)) {
                mappingFilesProvider?.let {
                    jvmMappingFileProperty.from(it)
                }
            }
            if (checkSearchDirectories) {
                val externalNativeBuildTaskUtil = ExternalNativeBuildTaskUtil(project.providers)

                variant.externalNativeBuildProviders.forEach { task ->
                    ndkMappingFileProperty.from(externalNativeBuildTaskUtil.findSearchPaths(task))
                }
            }
            configureMetadata()
        }
    }

    internal fun isNdkUploadEnabled(
        bugsnag: BugsnagPluginExtension,
        android: BaseExtension
    ): Boolean {
        val usesCmake = android.externalNativeBuild.cmake.path != null
        val usesNdkBuild = android.externalNativeBuild.ndkBuild.path != null
        val unityEnabled = BugsnagGenerateUnitySoMappingTask.isUnityLibraryUploadEnabled(bugsnag, android)
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
        android: BaseExtension
    ): List<File> {
        val searchPaths = bugsnag.sharedObjectPaths.get().toMutableList()
        val unityEnabled = BugsnagGenerateUnitySoMappingTask.isUnityLibraryUploadEnabled(bugsnag, android)
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
}
