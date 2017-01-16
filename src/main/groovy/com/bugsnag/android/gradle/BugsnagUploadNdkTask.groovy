package com.bugsnag.android.gradle

import com.android.build.gradle.internal.core.Abi
import com.android.build.gradle.internal.ndk.NdkHandler
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.tasks.TaskAction
/**
    Task to upload shared object mapping files to Bugsnag.

    Reads meta-data tags from the project's AndroidManifest.xml to extract a
    build UUID (injected by BugsnagManifestTask) and a Bugsnag API Key:

    https://developer.android.com/guide/topics/manifest/manifest-intro.html
    https://developer.android.com/guide/topics/manifest/meta-data-element.html

    This task must be called after shared object files are generated, so
    it is usually safe to have this be the absolute last task executed during
    a build.
*/
class BugsnagUploadNdkTask extends BugsnagUploadAbstractTask {
    File intermediatePath
    File symbolPath
    String variantName
    File projectDir
    File rootDir
    String toolchain

    BugsnagUploadNdkTask() {
        super()
        this.description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    @TaskAction
    def upload() {

        if (intermediatePath == null) {
            return
        }

        // Look for the shared objects in likely folders
        File binariesFile = null;
        for (File intDir : intermediatePath.listFiles()) {
            if (intDir.name.equals("cmake")) { // CMake
                binariesFile = intDir;
            } else if (intDir.name.equals("binaries")) { // Experimental
                binariesFile = intDir;
            } // TODO: where do the build-ndk files get output?
        }

        if (binariesFile == null) {
            project.logger.error("Unable to locate correct path to intermediate binaries under " + intermediatePath.absolutePath)
            return
        }

        // Read the API key and Build ID etc..
        super.readManifestFile();

        // Create the files to upload
        File intermediateBinaries = new File(binariesFile.absolutePath + File.separator + variantName + File.separator + "obj")

        for (File archDir : intermediateBinaries.listFiles()) {
            if (archDir.isDirectory()) {
                String arch = archDir.getName();

                for (File sharedObject : archDir.listFiles(new SharedObjectFilter())) {
                    File outputFile = createSymbolsForSharedObject(sharedObject, arch)

                    if (outputFile != null) {
                        uploadSymbols(outputFile, arch, sharedObject.getName())
                    }
                }
            }
        }
    }

    /**
     * Class to filter files to just contain shared objects
     */
    private static class SharedObjectFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".so");
        }
    }

    /**
     * Uses objdump to create a symbols file for the given shared object file
     * @param sharedObject the shared object file
     * @param arch the arch of the file
     * @return the output file location, or null on error
     */
    private File createSymbolsForSharedObject(File sharedObject, String arch) {
        // Get the path the version of objdump to use to get symbols
        File objDumpPath = getObjDumpExecutable(arch)
        if (objDumpPath != null) {

            try {
                File outputFile = new File(symbolPath.getAbsolutePath() + File.separator + arch + ".txt");

                // Call objdump, redirecting output to the output file
                ProcessBuilder builder = new ProcessBuilder(objDumpPath.toString(), "--disassemble", "--demangle", "--line-numbers", "--section=.text", sharedObject.toString())
                builder.redirectOutput(outputFile)
                Process process = builder.start()
                while (process.alive) {
                    Thread.sleep(100)
                }

                return outputFile
            } catch (Exception e) {
                project.logger.error("arch = " + arch + "  failed to generate symbols = " + e.getMessage());
            }
        } else {
            project.logger.error("Unable to upload NDK symbols: Could not find objdump location for " + arch)
        }

        return null
    }

    /**
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     * @param arch the arch that is being uploaded
     * @param sharedObjectName the original shared object name
     */
    private void uploadSymbols(File mappingFile, String arch, String sharedObjectName) {
        MultipartEntity mpEntity = new MultipartEntity()
        mpEntity.addPart("soMappingFile", new FileBody(mappingFile))
        mpEntity.addPart("arch", new StringBody(arch))
        mpEntity.addPart("sharedObjectName", new StringBody(sharedObjectName))
        mpEntity.addPart("relativePath", new StringBody(projectDir.toString()))

        super.uploadMultipartEntity(mpEntity)
    }

    /**
     * Gets the path to the objdump executable to use to get symbols from a shared object
     * @param arch The arch of the shared object
     * @return The objdump executable, or null if not found
     */
    private File getObjDumpExecutable(String arch) {

        try {
            Abi abi = Abi.getByName(arch)
            NdkHandler handler = new NdkHandler(rootDir, null, toolchain, "")
            File objDumpPath = new File(handler.getDefaultGccToolchainPath(abi), "bin/" + abi.getGccExecutablePrefix() + "-objdump")
            return objDumpPath
        } catch (Throwable ex) {
            project.logger.error("Error attempting to calculate objdump location: " + ex.message)
        }

        return null;
    }
}
