package com.bugsnag.android.gradle

import org.gradle.api.tasks.TaskAction

class BugsnagReleasesTask extends BugsnagVariantOutputTask {

    private static final Collection<String> VALID_VCS_PROVIDERS = Arrays.asList("github",
        "github-enterprise", "bitbucket", "bitbucket-server", "gitlab", "gitlab-onpremise")

    private static final String MK_OS_ARCH = "os.arch"
    private static final String MK_OS_NAME = "os.name"
    private static final String MK_OS_VERSION = "os.version"
    private static final String MK_JAVA_VERSION = "java.version"
    private static final String MK_GRADLE_VERSION = "gradle.version"

    String apiKey
    String versionName // "appVersion"
    Integer versionCode // "appVersionCode"
    String releaseStage

    String vcsProvider // in "sourceControl" object
    String vcsRepository
    String vcsRevision

    // TODO 0. get versionName, code, release stage
    // TODO 1. check if git repo exists and get info if so
    // TODO 2. Automatically collect non-sensitive useful metadata and merge with any user-supplied info

    BugsnagReleasesTask() {
        super()
        this.description = "Assembles information about the build that will be sent to the releases API"
    }

    @TaskAction
    def fetchReleaseInfo() { // TODO check project plugin extension values
//        if (!isValidPayload(apiKey, versionName)) {
//            project.logger.warn("Must supply api key and version name for release task")
//            return
//        }

        if (isValidVcsProvider(vcsProvider)) {
           // TODO add to payload
        }
        project.logger.lifecycle("Releases task!")



        // Fetch URL: git config --get remote.origin.url
        // Fetch Hash: git rev-parse HEAD
        // VCS provider: parse URL



        // collect default project metadata

        for (Map.Entry<String, String> entrySet : collectDefaultMetaData()) {
            project.logger.lifecycle("Key: ${entrySet.key}, Value: ${entrySet.value}")
        }
    }

    private Map<String, String> collectDefaultMetaData() {
        Map<String, String> metadata = new HashMap<>()
        metadata.put(MK_OS_ARCH, System.getProperty(MK_OS_ARCH))
        metadata.put(MK_OS_NAME, System.getProperty(MK_OS_NAME))
        metadata.put(MK_OS_VERSION, System.getProperty(MK_OS_VERSION))
        metadata.put(MK_JAVA_VERSION, System.getProperty(MK_JAVA_VERSION))
        metadata.put(MK_GRADLE_VERSION, project.gradle.gradleVersion)
        metadata
    }

    static boolean isValidPayload(String apiKey, String versionName) {
        return apiKey != null && versionName != null
    }

    static boolean isValidVcsProvider(String provider) {
        return provider == null || VALID_VCS_PROVIDERS.contains(provider)
    }

}
