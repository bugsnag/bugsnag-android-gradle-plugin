package com.bugsnag.android.gradle;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;
import org.gradle.process.internal.ExecException;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BugsnagReleasesTask extends DefaultTask {

    private static final Collection<String> VALID_VCS_PROVIDERS =
        Arrays.asList("github-enterprise", "bitbucket-server", "gitlab-onpremise", "bitbucket",
            "github", "gitlab");

    private static final String MK_OS_ARCH = "os.arch";
    private static final String MK_OS_NAME = "os.name";
    private static final String MK_OS_VERSION = "os.version";
    private static final String MK_JAVA_VERSION = "java.version";
    private static final String VCS_COMMAND = "git";
    private static final String CHARSET_UTF8 = "UTF-8";

    BaseVariantOutput variantOutput;
    BaseVariant variant;

    @TaskAction
    void fetchReleaseInfo() throws IOException, SAXException, ParserConfigurationException {
        Project project = getProject();
        final AndroidManifestInfo manifestInfo = BugsnagVariantOutputUtils.readManifestFile(project, variant, variantOutput);
        Logger logger = project.getLogger();

        if (!isValidPayload(manifestInfo.getApiKey(), manifestInfo.getVersionName())) {
            logger.warn("Must supply api key and version name for release task");
            return;
        }
        final BugsnagPluginExtension bugsnag = project.getExtensions().getByType(BugsnagPluginExtension.class);
        final JSONObject payload = generateJsonPayload(manifestInfo, bugsnag);
        String json = payload.toString();
        logger.debug("Releases Payload:\n" + json);

        new Call(project) {
            @Override
            public boolean makeApiCall() throws IOException {
                return deliverPayload(payload, manifestInfo, bugsnag);
            }
        }.execute();
    }

    private boolean deliverPayload(JSONObject payload, AndroidManifestInfo manifestInfo, BugsnagPluginExtension bugsnag) throws IOException {
        OutputStream os = null;
        Logger logger = getProject().getLogger();

        try {
            URL url = new URL(bugsnag.getReleasesEndpoint());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Bugsnag-Api-Key", manifestInfo.getApiKey());
            conn.setReadTimeout(bugsnag.getRequestTimeoutMs());
            conn.setConnectTimeout(bugsnag.getRequestTimeoutMs());
            conn.setDoOutput(true);

            os = conn.getOutputStream();
            os.write(payload.toString().getBytes(CHARSET_UTF8));

            int statusCode = conn.getResponseCode();

            if (statusCode == 200) {
                logger.info("Uploaded release info to Bugsnag");
                return true;
            } else {
                BufferedReader reader = null;
                String line;

                try {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        logger.error(line);
                    }
                    logger.warn("Release Request failed with statusCode " + statusCode);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
                return false;
            }
        } catch (IOException e) {
            logger.error(bugsnag.getReleasesEndpoint());
            logger.error("Failed to POST request", e);
            return false;
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    private JSONObject generateJsonPayload(AndroidManifestInfo manifestInfo, BugsnagPluginExtension bugsnag) {
        JSONObject root = new JSONObject();

        root.put("buildTool", "gradle-android");
        root.put("apiKey", manifestInfo.getApiKey());
        root.put("appVersion", manifestInfo.getVersionName());
        root.put("appVersionCode", manifestInfo.getVersionCode());

        String user;
        if (bugsnag.getBuilderName() != null) {
            user = bugsnag.getBuilderName();
        } else {
            user = runCmd("whoami");
        }
        root.put("builderName", user);

        root.put("metadata", generateMetadataJson(bugsnag));
        root.put("sourceControl", generateVcsJson(bugsnag));
        return root;
    }

    private JSONObject generateVcsJson(BugsnagPluginExtension bugsnag) {
        SourceControl sourceControl = AgpCompat.getSourceControl(getProject());
        String vcsUrl = null;
        String commitHash = null;
        String vcsProvider = null;

        if (sourceControl != null) {
            vcsUrl = sourceControl.getRepository();
            commitHash = sourceControl.getRevision();
            vcsProvider = sourceControl.getProvider();
        }

        if (vcsUrl == null) {
            vcsUrl = runCmd(VCS_COMMAND, "config", "--get", "remote.origin.url");
        }
        if (commitHash == null) {
            commitHash = runCmd(VCS_COMMAND, "rev-parse", "HEAD");
        }
        if (vcsProvider == null) {
            vcsProvider = parseProviderUrl(vcsUrl);
        }

        JSONObject sourceControlObj = new JSONObject();
        sourceControlObj.put("repository", vcsUrl);
        sourceControlObj.put("revision", commitHash);

        if (isValidVcsProvider(vcsProvider)) {
            sourceControlObj.put("provider", vcsProvider);
        }
        return sourceControlObj;
    }

    private JSONObject generateMetadataJson(BugsnagPluginExtension bugsnag) {
        Map<String, String> metadata = collectDefaultMetaData();
        Map<String, String> userData = bugsnag.getMetadata();

        if (userData != null) { // user values overwrite any defaults
            for (Map.Entry<String, String> entry : userData.entrySet()) {
                metadata.put(entry.getKey(), entry.getValue());
            }
        }

        JSONObject additionalInfo = new JSONObject();

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            additionalInfo.put(entry.getKey(), entry.getValue());
        }
        return additionalInfo;
    }

    private Map<String, String> collectDefaultMetaData() {
        String gradleVersion = getProject().getGradle().getGradleVersion();
        Map<String, String> metadata = new HashMap();
        metadata.put("os_arch", System.getProperty(MK_OS_ARCH));
        metadata.put("os_name", System.getProperty(MK_OS_NAME));
        metadata.put("os_version", System.getProperty(MK_OS_VERSION));
        metadata.put("java_version", System.getProperty(MK_JAVA_VERSION));
        metadata.put("gradle_version", gradleVersion);
        metadata.put("git_version", runCmd(VCS_COMMAND, "--version"));
        return metadata;
    }

    static boolean isValidPayload(String apiKey, String versionName) {
        return apiKey != null && versionName != null;
    }

    static boolean isValidVcsProvider(String provider) {
        return provider == null || VALID_VCS_PROVIDERS.contains(provider);
    }

    static String parseProviderUrl(String url) {
        if (url != null) {
            for (String provider : VALID_VCS_PROVIDERS) {
                if (url.contains(provider)) {
                    return provider;
                }
            }
        }
        return null;
    }

    /**
     * Runs a command on the shell
     * @param cmd the command (arguments must be separate strings)
     * @return the cmd output
     */
    private String runCmd(final String... cmd) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            getProject().exec(new Action<ExecSpec>() {
                @Override
                public void execute(ExecSpec execSpec) {
                    execSpec.commandLine(cmd);
                    execSpec.setStandardOutput(baos);
                    getLogging().captureStandardError(LogLevel.INFO);
                }
            });
            return new String(baos.toByteArray(), Charset.forName(CHARSET_UTF8)).trim();
        } catch (ExecException ignored) {
            return null;
        }
    }
}
