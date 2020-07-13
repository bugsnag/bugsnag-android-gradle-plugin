package com.bugsnag.android.gradle

import com.android.build.gradle.tasks.ManifestProcessorTask
import org.gradle.api.Project
import org.gradle.api.file.Directory

class AgpCompat {
    static File getManifestOutputDir(ManifestProcessorTask processManifest) {
        Object outputDir = processManifest.manifestOutputDirectory

        if (outputDir instanceof File) {
            return outputDir
        } else {
            // gradle 4.7 introduced a provider API for lazy evaluation of properties,
            // AGP subsequently changed the API from File to Provider<File>
            // see https://docs.gradle.org/4.7/userguide/lazy_configuration.html
            Directory dir = outputDir.getOrNull()

            if (dir != null) {
                return dir.asFile
            }
        }
        return null
    }

    static SourceControl getSourceControl(Project project) {
        return project.bugsnag.sourceControl
    }
}
