package com.bugsnag.android.gradle

import static groovy.io.FileType.FILES

import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ExternalNativeBuildTask
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.util.zip.GZIPOutputStream

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

        for (ExternalNativeBuildTask task : resolveExternalNativeBuildTasks()) {
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

    private Collection<ExternalNativeBuildTask> resolveExternalNativeBuildTasks() {
        try {
            return variant.externalNativeBuildProviders
                .stream()
                .map({ it.get() })
                .collect()
        } catch (Throwable ignored) {
            return variant.externalNativeBuildTasks
        }
    }

    private static File findSymbolPath(BaseVariantOutput variantOutput) {
        def resources = resolveProcessAndroidResources(variantOutput)
        def symbolPath = resources.textSymbolOutputFile

        if (symbolPath == null) {
            throw new IllegalStateException("Could not find symbol path")
        }
        symbolPath
    }

    private static def resolveProcessAndroidResources(BaseVariantOutput variantOutput) {
        try {
            return variantOutput.processResourcesProvider.get()
        } catch (Throwable ignored) {
            return variantOutput.processResources
        }
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

            Reader outReader = null

            try {
                File outputDir = new File(project.buildDir, "bugsnag")

                if (!outputDir.exists()) {
                    outputDir.mkdir()
                }

                File outputFile = new File(outputDir, arch + ".gz")
                File errorOutputFile = new File(outputDir, arch + ".error.txt")
                project.logger.lifecycle("Creating symbol file at ${outputFile}")

                // Call objdump, redirecting output to the output file
                ProcessBuilder builder = new ProcessBuilder(objDumpPath.toString(),
                    "--dwarf=info", "--dwarf=rawline", sharedObject.toString())
                builder.redirectError(errorOutputFile)
                Process process = builder.start()

                // Output the file to a zip
                InputStream stdout = process.getInputStream()
                outputZipFile(stdout, outputFile)

                if (process.waitFor() == 0) {
                    return outputFile
                } else {
                    project.logger.error("failed to generate symbols for " + arch + ", see "
                        + errorOutputFile.toString() + " for more details")
                    return null
                }
            } catch (Exception e) {
                project.logger.error("failed to generate symbols for " + arch + ": " + e.getMessage(), e)
            } finally {
                if (outReader != null) {
                    outReader.close()
                }
            }
        } else {
            project.logger.error("Unable to upload NDK symbols: Could not find objdump location for " + arch)
        }
        null
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
            zipStream = new GZIPOutputStream(new FileOutputStream(outputFile))

            byte[] buffer = new byte[8192]
            int len
            while((len=stdout.read(buffer)) != -1){
                zipStream.write(buffer, 0, len)
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

        String projectRoot = project.bugsnag.projectRoot ?: projectDir.toString()
        mpEntity.addPart("projectRoot", new StringBody(projectRoot))

        super.uploadMultipartEntity(mpEntity)
    }

    /**
     * Gets the path to the objdump executable to use to get symbols from a shared object
     * @param arch The arch of the shared object
     * @return The objdump executable, or null if not found
     */
    File getObjDumpExecutable(String arch) {
        try {
            String override = getObjDumpOverride(arch)
            File objDumpFile

            if (override != null) {
                objDumpFile = new File(override)
            } else {
                objDumpFile = findObjDump(project, arch)
            }

            if (!objDumpFile.exists() || !objDumpFile.canExecute()) {
                throw new RuntimeException("Failed to find executable objdump at $objDumpFile")
            }
            return objDumpFile
        } catch (Throwable ex) {
            project.logger.error("Error attempting to calculate objdump location: " + ex.message)
        }
        null
    }

    private Object getObjDumpOverride(String arch) {
        Map<String, String> paths = project.bugsnag.objdumpPaths
        paths != null ? paths[arch] : null
    }

    static File findObjDump(Project project, String arch) {
        Abi abi = Abi.findByName(arch)
        String ndkDir = project.android.ndkDirectory
        String osName = calculateOsName()

        if (abi == null) {
            throw new IllegalStateException("Failed to find ABI for $arch")
        }
        if (osName == null) {
            throw new IllegalStateException("Failed to calculate OS name")
        }
        calculateObjDumpLocation(ndkDir, abi, osName)
    }

    static File calculateObjDumpLocation(String ndkDir, Abi abi, String osName) {
        new File("$ndkDir/toolchains/$abi.toolchainPrefix-4.9/prebuilt/" +
            "$osName/bin/$abi.objdumpPrefix-objdump")
    }

    static String calculateOsName() {
        if (Os.isFamily(Os.FAMILY_MAC)) {
            return "darwin-x86_64"
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            return "linux-x86_64"
        } else if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            if ("x86" == System.getProperty("os.arch")) { // 32-bit
                return "windows"
            } else {
                return "windows-x86_64"
            }
        } else {
            return null
        }
    }

}
