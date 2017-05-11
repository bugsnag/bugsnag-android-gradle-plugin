package com.bugsnag.android.gradle

import com.android.build.gradle.internal.core.Abi
import com.android.build.gradle.internal.ndk.NdkHandler
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher
import java.util.regex.Pattern

import static groovy.io.FileType.*

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
    def joinPath = { String ...args -> args.join(File.separator) }

    BugsnagUploadNdkTask() {
        super()
        this.description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    @TaskAction
    def upload() {
        super.readManifestFile();
        boolean sharedObjectFound = false
        searchLibraryPaths { String arch, File sharedObject ->
            sharedObjectFound = true

            File outputFile = createSymbolsForSharedObject(sharedObject, arch)
            if (outputFile)
                uploadSymbols(outputFile, arch, sharedObject.name)
        }
        if (!sharedObjectFound) {
            project.logger.error("No shared objects found in ${sharedObjectPath?: intermediatePath}")
        }
    }

    /**
     * Traverse potential library paths, aggregating shared libraries
     *
     * Potential locations:
     * - {project dir}/{defined shared object path}
     * - {project dir}/obj/local
     * - {intermediates}/cmake/{variant}/obj
     * - {intermediates}/binaries/{variant}/obj
     * - {intermediates}/exploded-aar/{*}/jni
     *
     * Each of these locations contain a list of directories indicating which
     * architecture is targeted and any library (*.so) files.
     *
     * @param processor a closure to execute on each parent directory and shared
     *                  object file
     */
    def searchLibraryPaths(Closure processor) {
        if (sharedObjectPath)
            findSharedObjectFiles(joinPath(projectDir.path, sharedObjectPath), processor)

        findSharedObjectFiles(joinPath(projectDir.path, "obj", "local"), processor)

        String intermediateDir = intermediatePath?.path
        if (intermediateDir) {
            findSharedObjectFiles(joinPath(intermediateDir, "cmake", variantName, "obj"), processor)
            findSharedObjectFiles(joinPath(intermediateDir, "binaries", variantName, "obj"), processor)

            File explodedLibs = new File(joinPath(intermediateDir, "exploded-aar"))
            if (explodedLibs.exists())
                searchLibraryJNIPaths(explodedLibs, processor)
        }
    }

    /**
     * Recursively searches subdirectories for shared object files
     */
    def searchLibraryJNIPaths(File dir, Closure processor) {
        if (dir.exists()) {
            dir.eachDir {
                findSharedObjectFiles(joinPath(it.path, "jni"), processor)
                searchLibraryJNIPaths(it, processor)
            }
        }
    }

    /**
     * Searches the subdirectories of a given path and executes a block on
     * any shared object files
     * @param path      The parent path to search. Each subdirectory should
     *                  represent an architecture
     * @param processor a closure to execute on each parent directory and shared
     *                  object file
     */
    def findSharedObjectFiles(String path, Closure processor) {
        File dir = new File(path)
        if (dir.exists()) {
            dir.eachDir { arch ->
                arch.eachFileMatch FILES, ~/.*\.so$/, { processor(arch.name, it) }
            }
        }
    }


    /**
     * Uses objdump to create a symbols file for the given shared object file
     * @param sharedObject the shared object file
     * @param arch the arch of the file
     * @return the output file location, or null on error
     */
    File createSymbolsForSharedObject(File sharedObject, String arch) {
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
    boolean outPutSymbolFile(BufferedReader outReader, File outputFile, String arch) {
        // Output the file from stdout
        try {
            FileOutputStream is = new FileOutputStream(outputFile)
            OutputStreamWriter osw = new OutputStreamWriter(is)
            Writer writer = new BufferedWriter(osw)

            Pattern addressPattern = Pattern.compile("^\\s+([0-9a-f]+):", Pattern.CASE_INSENSITIVE);
            boolean justSeenAddress = false;
            String previousAddress = null;

            // Loop to remove redundant address lines (just keep the first and last addresses of each block)
            String line = outReader.readLine()
            Matcher addressMatcher = null;
            while (line != null) {

                // Check to see if the current line is an address
                addressMatcher = addressPattern.matcher(line);
                if (addressMatcher.find()) {

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
    void uploadSymbols(File mappingFile, String arch, String sharedObjectName) {
        MultipartEntity mpEntity = new MultipartEntity()
        mpEntity.addPart("soMappingFile", new FileBody(mappingFile))
        mpEntity.addPart("arch", new StringBody(arch))
        mpEntity.addPart("sharedObjectName", new StringBody(sharedObjectName))
        mpEntity.addPart("projectRoot", new StringBody(projectDir.toString()))

        super.uploadMultipartEntity(mpEntity)
    }

    /**
     * Gets the path to the objdump executable to use to get symbols from a shared object
     * @param arch The arch of the shared object
     * @return The objdump executable, or null if not found
     */
    File getObjDumpExecutable(String arch) {

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
