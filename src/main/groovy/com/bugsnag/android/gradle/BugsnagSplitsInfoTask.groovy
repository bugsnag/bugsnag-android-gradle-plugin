package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class BugsnagSplitsInfoTask extends DefaultTask {

    BaseVariant variant
    BugsnagPlugin.SplitsInfo splitsInfo

    BugsnagSplitsInfoTask() {
        super()
        this.description = "Discovers information about APK splits"
    }

    @TaskAction
    def discoverSplitsInfo() {
        def taskName = "splitsDiscoveryTask${BugsnagPlugin.taskNameForVariant(variant)}"
        def task = project.tasks.findByName(taskName)

        if (task == null) {
            throw new IllegalStateException("Could not find task: ${taskName}")
        }

        this.splitsInfo = new BugsnagPlugin.SplitsInfo()
        this.splitsInfo.densityFilters = task.densityFilters
        this.splitsInfo.languageFilters = task.languageFilters
        this.splitsInfo.abiFilters = task.abiFilters
        println("Density: ${this.splitsInfo.densityFilters}")
    }

}
