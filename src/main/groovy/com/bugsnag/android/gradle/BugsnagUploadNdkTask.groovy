package com.bugsnag.android.gradle

import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.core.Abi
import com.android.build.gradle.internal.ndk.NdkHandler
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import com.android.build.gradle.tasks.ProcessAndroidResources
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.GZIPOutputStream

import static groovy.io.FileType.FILES

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
class BugsnagUploadNdkTask extends BugsnagMultiPartUploadTask {

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
        super.readManifestFile()
        symbolPath = findSymbolPath(variantOutput)
        project.logger.lifecycle("Symbolpath: ${symbolPath}")

        boolean sharedObjectFound = false
        Closure processor = { String arch, File sharedObject ->
            project.logger.lifecycle("Found shared object file (${arch}) ${sharedObject}")
            sharedObjectFound = true

            File outputFile = createSymbolsForSharedObject(sharedObject, arch)
            if (outputFile) {
                uploadSymbols(outputFile, arch, sharedObject.name)
            }
        }

        Collection<ExternalNativeBuildTask> tasks = variant.externalNativeBuildTasks
        for (ExternalNativeBuildTask task : tasks) {
            File objFolder = task.objFolder
            File soFolder = task.soFolder
            findSharedObjectFiles(objFolder, processor)
            findSharedObjectFiles(soFolder, processor)
        }

        if (sharedObjectPath) {
            File file = new File(projectDir.path, sharedObjectPath)
            findSharedObjectFiles(file, processor)
        }
        if (!sharedObjectFound) {
            project.logger.error("No shared objects found")
        }
    }

    private static File findSymbolPath(BaseVariantOutput variantOutput) {
        ProcessAndroidResources resources = variantOutput.processResources

        def symbolPath = resources.textSymbolOutputFile

        if (symbolPath == null) {
            throw new IllegalStateException("Could not find symbol path")
        }
        symbolPath
    }
    /**
     * Searches the subdirectories of a given path and executes a block on
     * any shared object files
     * @param path The parent path to search. Each subdirectory should
     *                  represent an architecture
     * @param processor a closure to execute on each parent directory and shared
     *                  object file
     */
    void findSharedObjectFiles(File dir, Closure processor) {
        project.logger.lifecycle("Checking dir: ${dir}")

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
                File outputDir = new File(project.buildDir, "bugsnag")

                if (!outputDir.exists()) {
                    outputDir.mkdir()
                }

                File outputFile = new File(outputDir, arch + ".gz")
                File errorOutputFile = new File(outputDir, arch + ".error.txt")
                project.logger.lifecycle("Creating symbol file at ${outputFile}")

                // Call objdump, redirecting output to the output file
                ProcessBuilder builder = new ProcessBuilder(objDumpPath.toString(), "--disassemble", "--demangle", "--line-numbers", "--section=.text", sharedObject.toString())
                builder.redirectError(errorOutputFile)
                Process process = builder.start()

                // Output the file to a zip
                InputStream stdout = process.getInputStream()
                outputZipFile(stdout, outputFile)

                if (process.waitFor() == 0) {
                    return outputFile
                } else {
                    project.logger.error("failed to generate symbols for " + arch + ", see " + errorOutputFile.toString() + " for more details")
                    return null
                }
            } catch (Exception e) {
                project.logger.error("failed to generate symbols for " + arch + ": " + e.getMessage(), e)
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
        GZIPOutputStream zipStream

        try {
            zipStream = new GZIPOutputStream(new FileOutputStream(outputFile));

            byte[] buffer = new byte[8192];
            int len;
            while((len=stdout.read(buffer)) != -1){
                zipStream.write(buffer, 0, len);
            }

        } finally {
            zipStream.close()
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
        mpEntity.addPart("soMappingFile", new FileBody(mappingFile))
        mpEntity.addPart("soMappingFileFormat", new StringBody("zip"))
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
            NdkHandler handler = new NdkHandler(rootDir, null, toolchain, "", true)
            File objDumpPath = new File(handler.getDefaultGccToolchainPath(abi), "bin/" + abi.getGccExecutablePrefix() + "-objdump")
            return objDumpPath
        } catch (Throwable ex) {
            project.logger.error("Error attempting to calculate objdump location: " + ex.message)
        }

        return null
    }
}
