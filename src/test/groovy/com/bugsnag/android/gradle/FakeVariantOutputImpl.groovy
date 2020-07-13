package com.bugsnag.android.gradle

import com.android.build.FilterData
import com.android.build.OutputFile
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.PackageAndroidArtifact
import com.android.build.gradle.tasks.ProcessAndroidResources
import groovy.transform.CompileStatic
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

@CompileStatic
class FakeVariantOutputImpl implements ApkVariantOutput {

    private final String name

    protected FakeVariantOutputImpl(String name) {
        this.name = name
    }

    @Override
    String getOutputType() {
        return null
    }

    @Override
    Collection<String> getFilterTypes() {
        return null
    }

    @Override
    Collection<FilterData> getFilters() {
        return null
    }

    @Override
    OutputFile getMainOutputFile() {
        return null
    }

    @Override
    Collection<? extends OutputFile> getOutputs() {
        return null
    }

    @Override
    int getVersionCode() {
        return 1
    }

    @Override
    ProcessAndroidResources getProcessResources() {
        return null
    }

    @Override
    TaskProvider<ProcessAndroidResources> getProcessResourcesProvider() {
        return null
    }

    @Override
    ManifestProcessorTask getProcessManifest() {
        return null
    }

    @Override
    TaskProvider<ManifestProcessorTask> getProcessManifestProvider() {
        return null
    }

    @Override
    Task getAssemble() {
        return null
    }

    @Override
    String getName() {
        return name
    }

    @Override
    String getBaseName() {
        return null
    }

    @Override
    String getDirName() {
        return null
    }

    @Override
    File getOutputFile() {
        return null
    }

    @Override
    PackageAndroidArtifact getPackageApplication() {
        return null
    }

    @Override
    Task getZipAlign() {
        return null
    }

    @Override
    void setVersionCodeOverride(int versionCodeOverride) {

    }

    @Override
    int getVersionCodeOverride() {
        return 0
    }

    @Override
    void setVersionNameOverride(String versionNameOverride) {

    }

    @Override
    String getVersionNameOverride() {
        return null
    }

    @Override
    String getFilter(FilterType filterType) {
        return null
    }

    @Override
    String getOutputFileName() {
        return null
    }

    @Override
    void setOutputFileName(String outputFileName) {

    }
}
