package com.bugsnag.android.gradle;

import java.io.File;

/**
 * Used to return information about a shared object mapping file
 */
public class SOMappingFile {

    String sharedObjectName;
    File mappingFile;
    String arch;

    public SOMappingFile(String sharedObjectName, File mappingFile, String arch) {
        this.sharedObjectName = sharedObjectName;
        this.mappingFile = mappingFile;
        this.arch = arch;
    }

    public String getSharedObjectName() {
        return sharedObjectName;
    }

    public void setSharedObjectName(String sharedObjectName) {
        this.sharedObjectName = sharedObjectName;
    }

    public File getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }
}
