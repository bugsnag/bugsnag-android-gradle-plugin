package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class BugsnagSplitsInfoTask extends DefaultTask {

    BaseVariant variant
    def densityFilters
    def languageFilters
    def abiFilters

    BugsnagSplitsInfoTask() {
        super()
        this.description = "Discovers information about APK splits"
    }

    @TaskAction
    def discoverSplitsInfo() {
        def taskName = "splitsDiscoveryTask${BugsnagPlugin.taskNameForVariant(variant)}"
        def task = project.tasks.findByName(taskName)

        if (task != null) {
            this.densityFilters = task.densityFilters
            this.languageFilters = task.languageFilters
            this.abiFilters = task.abiFilters
        } else {
            project.logger.error("Failed to find splits task ${taskName}")
        }

    }

}
