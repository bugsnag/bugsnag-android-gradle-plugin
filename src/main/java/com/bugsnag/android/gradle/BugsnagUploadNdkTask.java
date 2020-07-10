package com.bugsnag.android.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.ProcessAndroidResources;

import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.tasks.ExternalNativeBuildTask;
import kotlin.Pair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

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
public class BugsnagUploadNdkTask extends DefaultTask {

    private static final int VALID_SO_FILE_THRESHOLD = 1024;

    File symbolPath;
    String variantName;
    File projectDir;
    File rootDir;
    String sharedObjectPath;

    String applicationId;
    BaseVariantOutput variantOutput;
    BaseVariant variant;

    @TaskAction
    void upload() throws ParserConfigurationException, SAXException, IOException {
        symbolPath = findSymbolPath(variantOutput);

        if (symbolPath == null) {
            return;
        }

        Logger logger = getProject().getLogger();
        logger.lifecycle("Symbolpath: " + symbolPath);
        Set<Pair<File, String>> soFiles = new HashSet();

        for (ExternalNativeBuildTask task : resolveExternalNativeBuildTasks()) {
            File objFolder = task.getObjFolder();
            File soFolder = task.getSoFolder();
            soFiles.addAll(findSharedObjectFiles(objFolder));
            soFiles.addAll(findSharedObjectFiles(soFolder));
        }

        if (sharedObjectPath != null) {
            File file = new File(projectDir.getPath(), sharedObjectPath);
            soFiles.addAll(findSharedObjectFiles(file));
        }

        // sort SO files alphabetically by architecture for consistent request order
        List<Pair<File, String>> files = new ArrayList(soFiles);
        Collections.sort(files, new Comparator<Pair<File, String>>() {
            @Override
            public int compare(Pair<File, String> lhs, Pair<File, String> rhs) {
                return lhs.getSecond().compareTo(rhs.getSecond());
            }
        });
        processFiles(files);
    }

    private void processFiles(Collection<Pair<File, String>> files) throws IOException, ParserConfigurationException, SAXException {
        for (Pair<File, String> pair : files) {
            processFile(pair.getSecond(), pair.getFirst());
        }
    }

    private void processFile(String arch, File sharedObject) throws IOException, ParserConfigurationException, SAXException {
        getProject().getLogger().lifecycle("Found shared object file " + arch + " " + sharedObject);

        File outputFile = generateSymbolsForSharedObject(sharedObject, arch);
        if (outputFile != null) {
            uploadSymbols(outputFile, arch, sharedObject.getName());
        }
    };

    private Collection<ExternalNativeBuildTask> resolveExternalNativeBuildTasks() {
        Collection<ExternalNativeBuildTask> tasks = new ArrayList();
        for (TaskProvider<ExternalNativeBuildTask> provider : variant.getExternalNativeBuildProviders()) {
            ExternalNativeBuildTask obj = provider.getOrNull();

            if (obj != null) {
                tasks.add(obj);
            }
            tasks.add(obj);
        }
        return tasks;
    }

    private static File findSymbolPath(BaseVariantOutput variantOutput) {
        ProcessAndroidResources resources = variantOutput.getProcessResourcesProvider().getOrNull();

        if (resources == null) {
            return null;
        }

        File symbolPath = (File) resources.property("textSymbolOutputFile");

        if (symbolPath == null) {
            throw new IllegalStateException("Could not find symbol path");
        }
        return symbolPath;
    }

    /**
     * Searches the subdirectories of a given path and executes a block on
     * any shared object files
     * @param dir The parent path to search. Each subdirectory should
     *                  represent an architecture
     */
    Collection<Pair<File, String>> findSharedObjectFiles(File dir) {
        getProject().getLogger().lifecycle("Checking dir: " + dir);
        Collection<Pair<File, String>> sharedObjectFiles = new ArrayList();

        if (dir.exists()) {
            for (File arch : dir.listFiles()) {
                for (File file : arch.listFiles()) {
                    if (file.getName().endsWith(".so")) {
                        sharedObjectFiles.add(new Pair(file, arch.getName()));
                    }
                }
            }
        }
        return sharedObjectFiles;
    }

    /**
     * Uses objdump to create a symbols file for the given shared object file
     * @param sharedObject the shared object file
     * @param arch the arch of the file
     * @return the output file location, or null on error
     */
    File generateSymbolsForSharedObject(File sharedObject, String arch) throws IOException {
        // Get the path the version of objdump to use to get symbols
        File objDumpPath = getObjDumpExecutable(arch);
        Logger logger = getProject().getLogger();

        if (objDumpPath != null) {

            Reader outReader = null;

            try {
                File outputDir = new File(getProject().getBuildDir(), "bugsnag");

                if (!outputDir.exists()) {
                    outputDir.mkdir();
                }

                File outputFile = new File(outputDir, arch + ".gz");
                File errorOutputFile = new File(outputDir, arch + ".error.txt");
                logger.lifecycle("Creating symbol file at ${outputFile}");

                // Call objdump, redirecting output to the output file
                ProcessBuilder builder = new ProcessBuilder(objDumpPath.toString(),
                    "--dwarf=info", "--dwarf=rawline", sharedObject.toString());
                builder.redirectError(errorOutputFile);
                Process process = builder.start();

                // Output the file to a zip
                InputStream stdout = process.getInputStream();
                outputZipFile(stdout, outputFile);

                if (process.waitFor() == 0) {
                    return outputFile;
                } else {
                    logger.error("failed to generate symbols for " + arch + " see "
                        + errorOutputFile.toString() + " for more details");
                    return null;
                }
            } catch (Exception e) {
                logger.error("failed to generate symbols for " + arch + " " + e.getMessage(), e);
            } finally {
                if (outReader != null) {
                    outReader.close();
                }
            }
        } else {
            logger.error("Unable to upload NDK symbols: Could not find objdump location for " + arch);
        }
        return null;
    }

    /**
     * Outputs the contents of stdout into the gzip file output file
     *
     * @param stdout The input stream
     * @param outputFile The output file
     */
    static void outputZipFile(InputStream stdout, File outputFile) throws IOException {
        GZIPOutputStream zipStream = null;

        try {
            zipStream = new GZIPOutputStream(new FileOutputStream(outputFile));

            byte[] buffer = new byte[8192];
            int len;
            while ((len = stdout.read(buffer)) != -1) {
                zipStream.write(buffer, 0, len);
            }

        } finally {
            if (zipStream != null) {
                zipStream.close();
            }

            stdout.close();
        }
    }

    /**
     * Uploads the given shared object mapping information
     * @param mappingFile the file to upload
     * @param arch the arch that is being uploaded
     * @param sharedObjectName the original shared object name
     */
    void uploadSymbols(File mappingFile, String arch, String sharedObjectName) throws IOException, ParserConfigurationException, SAXException {

        // a SO file may not contain debug info. if that's the case then the mapping file should be very small,
        // so we try and reject it here as otherwise the event-worker will reject it with a 400 status code.
        if (!mappingFile.exists() || mappingFile.length() < VALID_SO_FILE_THRESHOLD) {
            getProject().getLogger().warn("Skipping upload of empty/invalid mapping file: " + mappingFile);
            return;
        }

        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("soSymbolFile", new FileBody(mappingFile));
        mpEntity.addPart("arch", new StringBody(arch));
        mpEntity.addPart("sharedObjectName", new StringBody(sharedObjectName));

        BugsnagPluginExtension bugsnag = getProject().getExtensions().getByType(BugsnagPluginExtension.class);
        String projectRoot = bugsnag.getProjectRoot();
        if (projectRoot == null) {
            projectRoot = projectDir.toString();
        }
        mpEntity.addPart("projectRoot", new StringBody(projectRoot));

        BugsnagMultiPartUploadRequest request = new BugsnagMultiPartUploadRequest();
        request.applicationId = applicationId;
        request.variant = variant;
        request.variantOutput = variantOutput;
        request.uploadMultipartEntity(mpEntity, getProject());
    }

    /**
     * Gets the path to the objdump executable to use to get symbols from a shared object
     * @param arch The arch of the shared object
     * @return The objdump executable, or null if not found
     */
    File getObjDumpExecutable(String arch) {
        try {
            String override = getObjDumpOverride(arch);
            File objDumpFile;

            if (override != null) {
                objDumpFile = new File(override);
            } else {
                objDumpFile = findObjDump(getProject(), arch);
            }

            if (!objDumpFile.exists() || !objDumpFile.canExecute()) {
                throw new IllegalStateException("Failed to find executable objdump at " + objDumpFile);
            }
            return objDumpFile;
        } catch (Throwable ex) {
            getProject().getLogger().error("Error attempting to calculate objdump location: " + ex.getMessage());
        }
        return null;
    }

    private String getObjDumpOverride(String arch) {
        BugsnagPluginExtension bugsnag = getProject().getExtensions().getByType(BugsnagPluginExtension.class);
        Map<String, String> paths = bugsnag.getObjdumpPaths();
        return paths != null ? paths.get(arch) : null;
    }

    static File findObjDump(Project project, String arch) {
        Abi abi = Abi.Companion.findByName(arch);
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        String ndkDir = android.getNdkDirectory().getAbsolutePath();
        String osName = calculateOsName();

        if (abi == null) {
            throw new IllegalStateException("Failed to find ABI for " + arch);
        }
        if (osName == null) {
            throw new IllegalStateException("Failed to calculate OS name");
        }
        return calculateObjDumpLocation(ndkDir, abi, osName);
    }

    static File calculateObjDumpLocation(String ndkDir, Abi abi, String osName) {
        String executable = osName.startsWith("windows") ? "objdump.exe" : "objdump";
        return new File(String.format("%s/toolchains/%s-4.9/prebuilt/%s/bin/%s-%s",
            ndkDir, abi.getToolchainPrefix(), osName, abi.getObjdumpPrefix(), executable));
    }

    static String calculateOsName() {
        if (Os.isFamily(Os.FAMILY_MAC)) {
            return "darwin-x86_64";
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            return "linux-x86_64";
        } else if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return "x86".equals(System.getProperty("os.arch")) ? "windows" : "windows-x86_64";
        } else {
            return null;
        }
    }

}
