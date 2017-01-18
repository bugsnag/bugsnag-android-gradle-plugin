package com.bugsnag.android.gradle

import com.android.build.gradle.internal.core.Abi
import com.android.build.gradle.internal.ndk.NdkHandler
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher
import java.util.regex.Pattern

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
    String sharedObjectPath

    BugsnagUploadNdkTask() {
        super()
        this.description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    @TaskAction
    def upload() {

        if (intermediatePath == null) {
            return
        }

        File binariesFile = null;

        // See if the sharedObjectPath setting has been provided
        if (sharedObjectPath != null) {
            File objDir = new File(projectDir.toString() + File.separator + sharedObjectPath);
            if (objDir.exists()) {
                binariesFile = objDir;
            } else {
                project.logger.error("Path " + objDir.absolutePath + " not found when looking for shared objects")
                return
            }
        } else {
            // Look for the shared objects in likely folders
            for (File intDir : intermediatePath.listFiles()) {
                if (intDir.name.equals("cmake")) { // CMake
                    binariesFile = new File(intDir.absolutePath + File.separator + variantName + File.separator + "obj")
                } else if (intDir.name.equals("binaries")) { // Experimental
                    binariesFile = new File(intDir.absolutePath + File.separator + variantName + File.separator + "obj")
                }
            }

            if (binariesFile == null) {
                // Check to see if there an 'obj/local' folder in the project for ndk-build setup
                File objDir = new File(projectDir.toString() + File.separator + "obj" + File.separator + "local");
                if (objDir.exists()) {
                    binariesFile = objDir;
                }
            }
        }

        if (binariesFile == null) {
            project.logger.error("Unable to locate correct path to intermediate binaries")
            return
        }

        // Read the API key and Build ID etc..
        super.readManifestFile();

        boolean sharedObjectFound = false
        for (File archDir : binariesFile.listFiles()) {
            if (archDir.isDirectory()) {
                String arch = archDir.getName();

                for (File sharedObject : archDir.listFiles(new SharedObjectFilter())) {
                    sharedObjectFound = true
                    File outputFile = createSymbolsForSharedObject(sharedObject, arch)

                    if (outputFile != null) {
                        uploadSymbols(outputFile, arch, sharedObject.getName())
                    }
                }
            }
        }

        if (!sharedObjectFound) {
            project.logger.error("No shared objects found in " + binariesFile.absolutePath)
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
                File errorOutputFile = new File(symbolPath.getAbsolutePath() + File.separator + arch + ".error.txt");

                // Call objdump, redirecting output to the output file
                ProcessBuilder builder = new ProcessBuilder(objDumpPath.toString(), "--disassemble", "--demangle", "--line-numbers", "--section=.text", sharedObject.toString())
                builder.redirectError(errorOutputFile)
                Process process = builder.start()

                InputStream stdout = process.getInputStream();
                BufferedReader outReader = new BufferedReader (new InputStreamReader(stdout));

                if (!outPutSymbolFile(outReader, outputFile, arch)) {
                    return null;
                }

                if (process.exitValue() == 0) {
                    return outputFile
                } else {
                    project.logger.error("failed to generate symbols for " + arch + ", see " + errorOutputFile.toString() + " for more details");
                    return null
                }
            } catch (Exception e) {
                project.logger.error("failed to generate symbols for " + arch + ": "+ e.getMessage());
            }
        } else {
            project.logger.error("Unable to upload NDK symbols: Could not find objdump location for " + arch)
        }

        return null
    }

    /**
     * Outputs the contents of the outReader from the objdump command to the outputFile
     * Removes redundant address lines to minimize file size
     *
     * @param outReader The objdump output
     * @param outputFile The file to write to
     * @param arch The arch of the shared object being analysed
     * @return true if the file was written successfully, else false
     * @return true if the file was written successfully, else false
     */
    private boolean outPutSymbolFile(BufferedReader outReader, File outputFile, String arch) {
        // Output the file from stdout
        try {
            FileOutputStream is = new FileOutputStream(outputFile)
            OutputStreamWriter osw = new OutputStreamWriter(is)
            Writer writer = new BufferedWriter(osw)

            Pattern addressPattern = Pattern.compile("\\s+([0-9a-f]+):.*", Pattern.CASE_INSENSITIVE);
            boolean justSeenAddress = false;
            String previousAddress = null;

            // Loop to remove redundant address lines (just keep the first and last addresses of each block)
            String line = outReader.readLine()
            while (line != null) {

                // Check to see if the current line is an address
                Matcher addressMatcher = addressPattern.matcher(line);
                if (addressMatcher.matches()) {

                    // Only output the line if this is the start of a block of addresses
                    if (!justSeenAddress) {
                        writer.writeLine(line)
                        previousAddress = null;
                    } else {
                        previousAddress = line;
                    }

                    justSeenAddress = true;
                } else {

                    // If this is the end of a block of addresses then output the last address
                    if (justSeenAddress && previousAddress != null) {
                        writer.writeLine(previousAddress)
                    }

                    writer.writeLine(line)

                    previousAddress = null;
                    justSeenAddress = false;
                }

                line = outReader.readLine()
            }

            writer.close();
        } catch (IOException e) {
            project.logger.error("failed to write symbols for " + arch + ": " + e.getMessage());
            return false
        }

        return true
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
