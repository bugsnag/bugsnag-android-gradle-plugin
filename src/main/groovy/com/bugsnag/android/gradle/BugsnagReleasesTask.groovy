package com.bugsnag.android.gradle

import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

class BugsnagReleasesTask extends BugsnagVariantOutputTask {


    private static final Collection<String> VALID_VCS_PROVIDERS =
        Arrays.asList("github-enterprise", "bitbucket-server", "gitlab-onpremise", "bitbucket",
            "github", "gitlab")

    private static final String MK_OS_ARCH = "os.arch"
    private static final String MK_OS_NAME = "os.name"
    private static final String MK_OS_VERSION = "os.version"
    private static final String MK_JAVA_VERSION = "java.version"
    private static final String MK_GRADLE_VERSION = "gradle.version"
    private static final String MK_GIT_VERSION = "git.version"

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
    void fetchReleaseInfo() { // TODO check project plugin extension values
//        if (!isValidPayload(apiKey, versionName)) {
//            project.logger.warn("Must supply api key and version name for release task")
//            return
//        }


        String vcsUrl = runCmd("git", "config", "--get", "remote.origin.url")
        String commitHash = runCmd("git", "rev-parse", "HEAD")
        String vcsProvider = parseProviderUrl(vcsUrl)

        project.logger.lifecycle("VCS URL ${vcsUrl}")
        project.logger.lifecycle("Commit hash ${commitHash}")
        project.logger.lifecycle("VCS Provider ${vcsProvider}")

        if (isValidVcsProvider(vcsProvider)) {
            // TODO add to payload
        }
        project.logger.lifecycle("Releases task!")

        // Fetch URL: git config --get remote.origin.url
        // Fetch Hash: git rev-parse HEAD
        // VCS provider: parse URL

//        commandLine "git config --get remote.origin.url"

        // collect default project metadata
        Map<String, String> defaultMetaData = collectDefaultMetaData()
    }

    private Map<String, String> collectDefaultMetaData() {
        Map<String, String> metadata = new HashMap<>()
        metadata.put(MK_OS_ARCH, System.getProperty(MK_OS_ARCH))
        metadata.put(MK_OS_NAME, System.getProperty(MK_OS_NAME))
        metadata.put(MK_OS_VERSION, System.getProperty(MK_OS_VERSION))
        metadata.put(MK_JAVA_VERSION, System.getProperty(MK_JAVA_VERSION))
        metadata.put(MK_GRADLE_VERSION, project.gradle.gradleVersion)
        metadata.put(MK_GIT_VERSION, runCmd("git", "--version"))
        metadata
    }

    static boolean isValidPayload(String apiKey, String versionName) {
        return apiKey != null && versionName != null
    }

    static boolean isValidVcsProvider(String provider) {
        return provider == null || VALID_VCS_PROVIDERS.contains(provider)
    }

    static String parseProviderUrl(String url) {
        if (url != null) {
            for (String provider : VALID_VCS_PROVIDERS) {
                if (url.contains(provider)) {
                    return provider
                }
            }
        }
        null
    }


    /**
     * Runs a command on the shell
     * @param cmd the command (arguments must be separate strings)
     * @return the cmd output
     */
    String runCmd(String... cmd) { // TODO handle non-zero process codes
        ByteArrayOutputStream baos = new ByteArrayOutputStream()

        project.exec {
            commandLine cmd
            standardOutput = baos
        }
        new String(baos.toByteArray(), Charset.forName("UTF-8"))
    }
}


