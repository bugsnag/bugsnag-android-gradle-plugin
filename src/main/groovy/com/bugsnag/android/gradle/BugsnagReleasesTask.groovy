package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecException
import org.json.simple.JSONObject

import java.nio.charset.Charset

class BugsnagReleasesTask extends DefaultTask {

    private static final Collection<String> VALID_VCS_PROVIDERS =
        Arrays.asList("github-enterprise", "bitbucket-server", "gitlab-onpremise", "bitbucket",
            "github", "gitlab")

    private static final String MK_OS_ARCH = "os.arch"
    private static final String MK_OS_NAME = "os.name"
    private static final String MK_OS_VERSION = "os.version"
    private static final String MK_JAVA_VERSION = "java.version"
    private static final String VCS_COMMAND = "git"
    private static final String CHARSET_UTF8 = "UTF-8"

    BaseVariantOutput variantOutput;
    BaseVariant variant;

    BugsnagReleasesTask() {
        super()
        this.description = "Assembles information about the build that will be sent to the releases API"
    }

    @TaskAction
    void fetchReleaseInfo() {
        AndroidManifestInfo manifestInfo = BugsnagVariantOutputUtils.readManifestFile(project, variant, variantOutput)

        if (!isValidPayload(manifestInfo.apiKey, manifestInfo.versionName)) {
            project.logger.warn("Must supply api key and version name for release task")
            return
        }

        JSONObject payload = generateJsonPayload(manifestInfo)
        String json = payload.toString()

        project.logger.debug("Releases Payload:\n${json}")

        new Call(project) {
            @Override
            boolean makeApiCall() {
                deliverPayload(payload, manifestInfo)
            }
        }.execute()
    }

    private boolean deliverPayload(JSONObject payload, AndroidManifestInfo manifestInfo) {
        OutputStream os = null

        try {
            URL url = new URL(project.bugsnag.releasesEndpoint)
            HttpURLConnection conn = url.openConnection()

            conn.with {
                setRequestMethod("POST")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Bugsnag-Api-Key", manifestInfo.apiKey)
                setReadTimeout(project.bugsnag.requestTimeoutMs)
                setConnectTimeout(project.bugsnag.requestTimeoutMs)
                setDoOutput(true)

                os = outputStream
                os.write(payload.toString().getBytes(CHARSET_UTF8))

                int statusCode = responseCode

                if (statusCode == 200) {
                    project.logger.info("Uploaded release info to Bugsnag")
                    return true
                } else {
                    BufferedReader reader
                    String line

                    try {
                        reader = new BufferedReader(new InputStreamReader(errorStream))
                        while ((line = reader.readLine()) != null) {
                            project.logger.error(line)
                        }
                        project.logger.warn("Release Request failed with statusCode " + statusCode)
                    } finally {
                        if (reader != null) {
                            reader.close()
                        }
                    }
                    return false
                }
            }
        } catch (IOException e) {
            project.logger.error(project.bugsnag.releasesEndpoint)
            project.logger.error("Failed to POST request", e)
            return false
        } finally {
            if (os != null) {
                os.close()
            }
        }
    }

    private JSONObject generateJsonPayload(AndroidManifestInfo manifestInfo) {
        JSONObject root = new JSONObject()

        root.put("buildTool", "gradle-android")
        root.put("apiKey", manifestInfo.apiKey)
        root.put("appVersion", manifestInfo.versionName)
        root.put("appVersionCode", manifestInfo.versionCode)

        String user
        if (project.bugsnag.builderName != null) {
            user = project.bugsnag.builderName
        } else {
            user = runCmd("whoami")
        }
        root.put("builderName", user)

        root.put("metadata", generateMetadataJson())
        root.put("sourceControl", generateVcsJson())
        root
    }

    private JSONObject generateVcsJson() {
        String vcsUrl = project.bugsnag.sourceControl.repository
        String commitHash = project.bugsnag.sourceControl.revision
        String vcsProvider = project.bugsnag.sourceControl.provider

        if (vcsUrl == null) {
            vcsUrl = runCmd(VCS_COMMAND, "config", "--get", "remote.origin.url")
        }
        if (commitHash == null) {
            commitHash = runCmd(VCS_COMMAND, "rev-parse", "HEAD")
        }
        if (vcsProvider == null) {
            vcsProvider = parseProviderUrl(vcsUrl)
        }

        JSONObject sourceControl = new JSONObject()
        sourceControl.put("repository", vcsUrl)
        sourceControl.put("revision", commitHash)

        if (isValidVcsProvider(vcsProvider)) {
            sourceControl.put("provider", vcsProvider)
        }
        sourceControl
    }

    private JSONObject generateMetadataJson() {
        Map<String, String> metadata = collectDefaultMetaData()
        Map<String, String> userData = project.bugsnag.metadata

        if (userData != null) { // user values overwrite any defaults
            for (Map.Entry<String, String> entry : userData.entrySet()) {
                metadata.put(entry.key, entry.value)
            }
        }

        JSONObject additionalInfo = new JSONObject()

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            additionalInfo.put(entry.key, entry.value)
        }
        additionalInfo
    }

    private Map<String, String> collectDefaultMetaData() {
        Map<String, String> metadata = new HashMap<>()
        metadata.put("os_arch", System.getProperty(MK_OS_ARCH))
        metadata.put("os_name", System.getProperty(MK_OS_NAME))
        metadata.put("os_version", System.getProperty(MK_OS_VERSION))
        metadata.put("java_version", System.getProperty(MK_JAVA_VERSION))
        metadata.put("gradle_version", project.gradle.gradleVersion)
        metadata.put("git_version", runCmd(VCS_COMMAND, "--version"))
        metadata
    }

    static boolean isValidPayload(String apiKey, String versionName) {
        apiKey != null && versionName != null
    }

    static boolean isValidVcsProvider(String provider) {
        provider == null || VALID_VCS_PROVIDERS.contains(provider)
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
    private String runCmd(String... cmd) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream()

            project.exec {
                commandLine cmd
                standardOutput = baos
                logging.captureStandardError LogLevel.INFO
            }
            new String(baos.toByteArray(), Charset.forName(CHARSET_UTF8)).trim()
        } catch (ExecException ignored) {
            null
        }
    }
}
