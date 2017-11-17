package com.bugsnag.android.gradle

import org.gradle.api.tasks.TaskAction

class BugsnagReleasesTask extends BugsnagVariantOutputTask {

    private static final Collection<String> VALID_VCS_PROVIDERS = Arrays.asList("github",
        "github-enterprise", "bitbucket", "bitbucket-server", "gitlab", "gitlab-onpremise")

    String apiKey
    String versionName // "appVersion"
    Integer versionCode // "appVersionCode"
    String releaseStage

    String vcsProvider // in "sourceControl" object
    String vcsRepository
    String vcsRevision
    Map<String, String> metadata

    // TODO 0. get versionName, code, release stage
    // TODO 1. check if git repo exists and get info if so
    // TODO 2. Automatically collect non-sensitive useful metadata and merge with any user-supplied info

    BugsnagReleasesTask() {
        super()
        this.description = "Assembles information about the build that will be sent to the releases API"
    }

    @TaskAction
    def fetchReleaseInfo() { // TODO check project plugin extension values
        if (!isValidPayload(apiKey, versionName)) {
            project.logger.warn("Must supply api key and version name for release task")
            return
        }

        if (isValidVcsProvider(vcsProvider)) {
           // TODO add to payload
        }
        project.logger.lifecycle("Releases task!")
    }

    static boolean isValidPayload(String apiKey, String versionName) {
        return apiKey != null && versionName != null
    }

    static boolean isValidVcsProvider(String provider) {
        return provider == null || VALID_VCS_PROVIDERS.contains(provider)
    }

}
