package com.bugsnag.android.gradle;

import java.util.Map;

/**
 * Defines configuration options (Gradle plugin extensions) for the BugsnagPlugin
 */
public class BugsnagPluginExtension {

    String endpoint = "https://upload.bugsnag.com";
    String releasesEndpoint = "https://build.bugsnag.com";

    boolean autoUpload = true;
    boolean autoReportBuilds = true;
    boolean uploadDebugBuildMappings = false;
    boolean overwrite = false;
    int retryCount = 0;
    Boolean ndk = null;
    String sharedObjectPath = null;
    String projectRoot = null;
    boolean failOnUploadError = true;
    int requestTimeoutMs = 60000;

    // release API values
    String builderName = null;
    Map<String, String> metadata = null;
    Map<String, String> objdumpPaths = null;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getReleasesEndpoint() {
        return releasesEndpoint;
    }

    public void setReleasesEndpoint(String releasesEndpoint) {
        this.releasesEndpoint = releasesEndpoint;
    }

    public boolean isAutoUpload() {
        return autoUpload;
    }

    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    public boolean isAutoReportBuilds() {
        return autoReportBuilds;
    }

    public void setAutoReportBuilds(boolean autoReportBuilds) {
        this.autoReportBuilds = autoReportBuilds;
    }

    public boolean isUploadDebugBuildMappings() {
        return uploadDebugBuildMappings;
    }

    public void setUploadDebugBuildMappings(boolean uploadDebugBuildMappings) {
        this.uploadDebugBuildMappings = uploadDebugBuildMappings;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Boolean getNdk() {
        return ndk;
    }

    public void setNdk(Boolean ndk) {
        this.ndk = ndk;
    }

    public String getSharedObjectPath() {
        return sharedObjectPath;
    }

    public void setSharedObjectPath(String sharedObjectPath) {
        this.sharedObjectPath = sharedObjectPath;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public boolean isFailOnUploadError() {
        return failOnUploadError;
    }

    public void setFailOnUploadError(boolean failOnUploadError) {
        this.failOnUploadError = failOnUploadError;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public String getBuilderName() {
        return builderName;
    }

    public void setBuilderName(String builderName) {
        this.builderName = builderName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getObjdumpPaths() {
        return objdumpPaths;
    }

    public void setObjdumpPaths(Map<String, String> objdumpPaths) {
        this.objdumpPaths = objdumpPaths;
    }
}
