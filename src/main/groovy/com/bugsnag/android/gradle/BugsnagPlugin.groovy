package com.bugsnag.android.gradle

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.scope.VariantOutputScope
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.ApkVariantOutputData
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.MergeManifests
import com.android.build.gradle.tasks.PackageApplication
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.android.builder.model.BuildType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
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
class BugsnagPlugin implements Plugin<Project> {
    static final String API_KEY_TAG = 'com.bugsnag.android.API_KEY'
    static final String BUILD_UUID_TAG = 'com.bugsnag.android.BUILD_UUID'
    static final String GROUP_NAME = 'Bugsnag'

    void apply(Project project) {
        project.extensions.create("bugsnag", BugsnagPluginExtension)

        project.afterEvaluate {

            if (project.plugins.hasPlugin("com.android.application")) {
                // Create tasks for each Build Variant
                // https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide#TOC-Build-Variants
                project.android.applicationVariants.all { ApplicationVariant variant ->
                    configureVariant(project, variant)
                }

            } else if (project.plugins.hasPlugin("com.android.model.application")) {

                project.tasks.whenTaskAdded {t ->

                    if (t.name.startsWith("process") && t.name.endsWith("Manifest")) {
                        if (t instanceof MergeManifests) {
                            MergeManifests task = (MergeManifests) t;
                            configureVariant(project, task)
                        }
                    }
                }

            } else {
                // Make sure the android plugin has been applied first
                throw new IllegalStateException('Must apply \'com.android.application\', or \'com.android.model.application\' first! ')
            }
        }
    }

    private static void configureVariant(Project project, MergeManifests task) {

        project.logger.error("BugsnagGroovy: Task Added " + task.name + " " + task.getClass().toString())

        String manifestPath = task.manifestOutputFile.absolutePath;
        project.logger.error("BugsnagGroovy: manifest file " + manifestPath)

        ApkVariantOutputData data = task.variantOutputData

        VariantOutputScope scope = data.scope

        VariantScope vscope = scope.variantScope;
        project.logger.error("BugsnagGroovy: mapping file " + vscope.mappingFile)
        project.logger.error("BugsnagGroovy: application Id " + vscope.variantData.applicationId)

        String applicationId = vscope.variantData.applicationId
        File mappingFile = vscope.mappingFile
        String variantName = task.variantName.capitalize()

//        Map props = task.getProperties()
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  TaskProperty " + key.toString() + " " + props.get(key).toString())
//        }
//        props = data.getProperties()
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  DataProperty " + key.toString() + " " + props.get(key).toString())
//        }
//        props = scope.getProperties()
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  ScopeProperty " + key.toString() + " " + props.get(key).toString())
//        }
//        props = vscope.getProperties()
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  VScopeProperty " + key.toString() + " " + props.get(key).toString())
//        }
//        Map props = project.properties
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  ProjectProperty " + key.toString() + " " + props.get(key).toString())
//        }
//
//
//        props = project.ext.properties
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  ProjectExtProperty " + key.toString() + " " + props.get(key).toString())
//        }
//
//        DefaultTask debug = project.androidDebug
//        props = debug.properties
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  ProjectAndroidDebugProperty " + key.toString() + " " + props.get(key).toString())
//        }
//
//        props = debug.inputs.properties
//        for (Object key : props.keySet() ) {
//            project.logger.error("BugsnagGroovy:  ProjectAndroidDebugInput " + key.toString() + " " + props.get(key).toString())
//        }

        Task assembleTask = project.assemble //project["processDebugManifest"]
        Task packageTask = project.processDebugResources

        //configureProguardConfigTask(project, variantName, bt, vscope.packageApplicationTask)

        configureManifestTask(project, variantName, manifestPath, task, data.processResourcesTask)

        configureUploadTask(project, variantName, manifestPath, applicationId, mappingFile, packageTask, assembleTask)
    }

    private static void configureVariant(Project project, BaseVariant variant) {
        project.logger.error("BugsnagGroovy: Configuring variant " + variant.name +  " for project " + project.name)

        def hasDisabledBugsnag = {
            it.ext.properties.containsKey("enableBugsnag") && !it.ext.enableBugsnag
        }

        // Ignore any conflicting properties, bail if anything has a disable flag.
        if ((variant.productFlavors + variant.buildType).any(hasDisabledBugsnag)) {
            return
        }

        // The Android build system supports creating multiple APKs
        // per Build Variant (Variant Outputs):
        // https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/user-guide/apk-splits
        //
        // Variant Outputs share most tasks so we only need to attach
        // Bugsnag tasks to the first output.
        BaseVariantOutput variantOutput = variant.outputs.first()
        String variantName = variant.name.capitalize()

        // The location of the "intermediate" AndroidManifest.xml
        String manifestPath = variantOutput.processManifest.manifestOutputFile.absolutePath

        PackageApplication packageTask = variantOutput.packageApplication

        configureProguardConfigTask(project, variantName, variant.getBuildType(), packageTask)

        configureManifestTask(project, variantName, manifestPath, variantOutput.processManifest, variantOutput.processResources)

        configureUploadTask(project, variantName, manifestPath, variant.applicationId, variant.mappingFile, packageTask, variant.getAssemble())
    }

    private static void configureProguardConfigTask(Project project,
                                                    String variantName,
                                                    BuildType buildType,
                                                    Task packageTask) {
        // Create a Bugsnag task to add a "Bugsnag recommended proguard settings" file
        BugsnagProguardConfigTask proguardConfigTask = project.tasks.create("processBugsnag${variantName}Proguard", BugsnagProguardConfigTask)
        proguardConfigTask.group = GROUP_NAME
        proguardConfigTask.buildType = buildType

        // Automatically add the "edit proguard settings" task to the
        // build process.
        //
        // This task must be called before ProGuard is run, but since
        // the name of the ProGuard task changed between 1.0 and 1.5
        // of the Android build tools, we'll hook into the "package"
        // task as a dependency, since this is always run before
        // ProGuard.
        //
        // For reference, in Android Build Tools 1.0, the ProGuard
        // task was named `proguardRelease`, and in 1.5+ the ProGuard
        // task is named `transformClassesAndResourcesWithProguardForRelease`
        // as it is now part of the "transforms" process.
        if(project.bugsnag.autoProguardConfig) {
            packageTask.dependsOn proguardConfigTask
        }
    }

    private static void configureManifestTask(Project project,
                                              String variantName,
                                              String manifestPath,
                                              ManifestProcessorTask manifestProcessor,
                                              ProcessAndroidResources processResources) {
        // Create a Bugsnag task to add a build UUID to AndroidManifest.xml
        // This task must be called after "process${variantName}Manifest", since it
        // requires that an AndroidManifest.xml exists in `build/intermediates`.
        BugsnagManifestTask manifestTask = project.tasks.create("processBugsnag${variantName}Manifest", BugsnagManifestTask)
        manifestTask.group = GROUP_NAME
        manifestTask.manifestPath = manifestPath
        manifestTask.mustRunAfter manifestProcessor
        manifestTask.onlyIf { it.shouldRun() }


        // Automatically add the "add build uuid to manifest" task to
        // the build process.
        //
        // This task must be called after processManifest (see above),
        // but before any source code is compiled, and before
        // Bugsnag's upload task is run, since both require a UUID to
        // be present in AndroidManifest.xml.
        processResources.dependsOn manifestTask
    }

    private static void configureUploadTask(Project project,
                                            String variantName,
                                            String manifestPath,
                                            String applicationId,
                                            File mappingFile,
                                            Task packageTask,
                                            Task assembleTask) {

        // Create a Bugsnag task to upload proguard mapping file
        BugsnagUploadTask uploadTask = project.tasks.create("uploadBugsnag${variantName}Mapping", BugsnagUploadTask)
        uploadTask.group = GROUP_NAME
        uploadTask.manifestPath = manifestPath
        uploadTask.applicationId = applicationId
        uploadTask.mappingFile = mappingFile

        uploadTask.mustRunAfter packageTask


        // Automatically add the "upload proguard mappings" task to
        // the build process.
        //
        // This task must be called after `packageApplication` (see
        // above), but we also want this to run automatically as part
        // of a typical build, so we hook into the `assemble` task.
        if(project.bugsnag.autoUpload) {
            assembleTask.dependsOn uploadTask
        }
    }
}
