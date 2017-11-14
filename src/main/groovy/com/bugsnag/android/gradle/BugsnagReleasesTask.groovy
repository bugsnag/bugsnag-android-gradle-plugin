package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BugsnagReleasesTask extends DefaultTask {

    private static final Collection<String> VALID_VCS_PROVIDERS = Arrays.asList("github",
        "github-enterprise", "bitbucket", "bitbucket-server", "gitlab", "gitlab-onpremise")

    String apiKey
    String versionName // "appVersion"
    int versionCode // "appVersionCode"
    String builderName
    String releaseStage
    boolean autoAssignRelease

    String vcsProvider // in "sourceControl" object
    String vcsUrl
    String vcsHash
    Map<String, String> metadata

    BugsnagReleasesTask() {
        super()
        this.description = "" // TODO
    }

    @TaskAction
    void fetchReleaseInfo() { // TODO check project plugin extension values
        if (apiKey == null || versionName == null) {
            project.logger.warn("Must supply api key and version name for release task")
            return
        }

        isValidVcsProvider(vcsProvider)
    }

    static boolean isValidPayload(String apiKey, String versionName) {
        return apiKey != null && versionName != null
    }

    static boolean isValidVcsProvider(String provider) {
        return provider == null || VALID_VCS_PROVIDERS.contains(provider)
    }

}
