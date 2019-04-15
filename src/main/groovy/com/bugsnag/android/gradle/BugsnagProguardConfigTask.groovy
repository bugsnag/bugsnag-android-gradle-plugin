package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 Task to add an additional ProGuard configuration file (bugsnag.pro)
 which ensures that our required ProGuard settings are applied.

 This task must be called before ProGuard is run.
 */
class BugsnagProguardConfigTask extends DefaultTask {

    static final String PROGUARD_CONFIG_PATH = "build/intermediates/bugsnag/bugsnag.pro"
    static final String PROGUARD_CONFIG_SETTINGS = """\
    -keepattributes LineNumberTable,SourceFile
    -keep class com.bugsnag.android.NativeInterface { *; }
    -keep class com.bugsnag.android.Breadcrumb { *; }
    -keep class com.bugsnag.android.Breadcrumbs { *; }
    -keep class com.bugsnag.android.Breadcrumbs\$Breadcrumb { *; }
    -keep class com.bugsnag.android.BreadcrumbType { *; }
    -keep class com.bugsnag.android.Severity { *; }
    -keep class com.bugsnag.android.ndk.BugsnagObserver { *; }
    """.toString()

    BaseVariant variant

    BugsnagProguardConfigTask() {
        super()
        this.description = "Adds the Bugsnag recommended proguard settings to your project"
    }

    @TaskAction
    void createProguardConfig() {
        // Create a file handle for the Bugsnag proguard config file
        File file = project.file(PROGUARD_CONFIG_PATH)

        // Create the directory if it doesnt exist already
        file.getParentFile().mkdirs()

        // Write our recommended proguard settings to this file
        FileWriter fr = null

        try {
            fr = new FileWriter(file.path)
            fr.write(PROGUARD_CONFIG_SETTINGS)
            fr.write("\n")
        } catch (Exception e) {
            project.logger.warn("Failed to write Bugsnag ProGuard settings", e)
        } finally {
            if (fr != null) {
                fr.close()
            }
        }

        // Add this proguard settings file to the list
        variant.getBuildType().buildType.proguardFiles(file)
    }
}
