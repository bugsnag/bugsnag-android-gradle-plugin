package com.bugsnag.android.gradle

import com.android.build.gradle.internal.core.Abi
import com.android.build.gradle.internal.ndk.NdkHandler
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.tasks.TaskAction

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.GZIPOutputStream

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

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS");

    String getLogPrefix() {
        return "[${dateFormat.format(new Date())}] Bugsnag:"
    }

    BugsnagUploadNdkTask() {
        super()
        this.description = "Generates and uploads the NDK mapping file(s) to Bugsnag"
    }

    @TaskAction
    def upload() {
        project.logger.info("${getLogPrefix()} Started reading manifest file")
        super.readManifestFile();

        boolean sharedObjectFound = false
        searchLibraryPaths { String arch, File sharedObject ->
            sharedObjectFound = true

            project.logger.info("${getLogPrefix()} Started creating symbols for shared object")
            File outputFile = createSymbolsForSharedObject(sharedObject, arch)

            if (outputFile) {
                project.logger.info("${getLogPrefix()} Started uploading symbols")
                uploadSymbols(outputFile, arch, sharedObject.name)
            }
        }
        project.logger.info("${getLogPrefix()} Completed NDK upload task")
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
                File outputFile = new File(symbolPath.getAbsolutePath() + File.separator + arch + ".gz");
                File errorOutputFile = new File(symbolPath.getAbsolutePath() + File.separator + arch + ".error.txt");

                // Call objdump, redirecting output to the output file
                project.logger.info("${getLogPrefix()} Calling objdump process")

                ProcessBuilder builder = new ProcessBuilder(objDumpPath.toString(), "-W", "-x", "--section=.debug_line", sharedObject.toString())
                builder.redirectError(errorOutputFile)
                Process process = builder.start()

                // Output the file to a zip
                InputStream stdout = process.getInputStream();
                outputZipFile(stdout, outputFile)

                if (process.waitFor() == 0) {
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
     * Outputs the contents of stdout into the gzip file output file
     *
     * @param stdout The input stream
     * @param outputFile The output file
     */
    static void outputZipFile(InputStream stdout, File outputFile) {
        GZIPOutputStream zipStream = null

        try {
            zipStream = new GZIPOutputStream(new FileOutputStream(outputFile));

            byte[] buffer = new byte[8192];
            int len;
            while((len=stdout.read(buffer)) != -1){
                zipStream.write(buffer, 0, len);
            }

        } finally {
            if (zipStream != null) {
                zipStream.close()
            }
            stdout.close()
        }
    }

    /**
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     * @param arch the arch that is being uploaded
     * @param sharedObjectName the original shared object name
     */
    void uploadSymbols(File mappingFile, String arch, String sharedObjectName) {
        MultipartEntity mpEntity = new MultipartEntity()
        mpEntity.addPart("soSymbolFile", new FileBody(mappingFile))
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
