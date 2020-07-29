package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecException
import org.json.simple.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import javax.inject.Inject

open class BugsnagReleasesTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Assembles information about the build that will be sent to the releases API"
    }

    @get:PathSensitive(NONE)
    @get:InputFile
    override val manifestInfoFile: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val requestOutputFile: RegularFileProperty = objects.fileProperty()

    // should take the JVM + NDK mapping files as inputs because the manifestInfo will
    // not necessarily vary between different builds. it is not guaranteed that
    // either of these properties will be set so they are marked as optional.
    @get:PathSensitive(NONE)
    @get:InputFile
    @get:Optional
    val jvmMappingFileProperty: RegularFileProperty = objects.fileProperty()

    @get:PathSensitive(NONE)
    @get:InputFiles
    @get:Optional
    val ndkMappingFileProperty: Property<FileCollection> = objects.property(FileCollection::class.java)

    @get:Input
    val retryCount: Property<Int> = objects.property(Int::class.javaObjectType)

    @get:Input
    val timeoutMillis: Property<Long> = objects.property(Long::class.javaObjectType)

    @get:Input
    val releasesEndpoint: Property<String> = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val builderName: Property<String> = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val metadata: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

    @get:Optional
    @get:Input
    val sourceControlProvider: Property<String> = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val sourceControlRepository: Property<String> = objects.property(String::class.java)

    @get:Optional
    @get:Input
    val sourceControlRevision: Property<String> = objects.property(String::class.java)

    @TaskAction
    fun fetchReleaseInfo() {
        val manifestInfo = parseManifestInfo()
        val payload = generateJsonPayload(manifestInfo)
        logger.lifecycle("Bugsnag: Attempting upload to Releases API")

        object : Call(retryCount, logger) {
            override fun makeApiCall(): Boolean {
                val response = deliverPayload(payload, manifestInfo)
                requestOutputFile.asFile.get().writeText(response)
                logger.lifecycle("Bugsnag: Upload succeeded")
                return true
            }
        }.execute()
    }

    private fun deliverPayload(
        payload: JSONObject,
        manifestInfo: AndroidManifestInfo
    ): String {
        var os: OutputStream? = null
        try {
            val url = URL(releasesEndpoint.get())
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Bugsnag-Api-Key", manifestInfo.apiKey)
            val timeoutMs = timeoutMillis.get()
            conn.readTimeout = timeoutMs.toInt()
            conn.connectTimeout = timeoutMs.toInt()
            conn.doOutput = true
            os = conn.outputStream
            os.write(payload.toString().toByteArray(charset(CHARSET_UTF8)))
            return readRequestResponse(conn)
        } catch (exc: IOException) {
            throw IllegalStateException("Request to Bugsnag Releases API failed, aborting build.", exc)
        } finally {
            os?.close()
        }
    }

    private fun readRequestResponse(conn: HttpURLConnection): String {
        val statusCode = conn.responseCode
        val success = statusCode == 200
        val responseBody = when {
            success -> conn.inputStream.bufferedReader().readText()
            else -> conn.errorStream.bufferedReader().readText()
        }
        return when {
            success -> responseBody
            else -> {
                project.logger.error(responseBody)
                throw IllegalStateException("Request to Bugsnag Releases API failed, aborting build.")
            }
        }
    }

    private fun generateJsonPayload(manifestInfo: AndroidManifestInfo): JSONObject {
        val root = JSONObject()
        root["buildTool"] = "gradle-android"
        root["apiKey"] = manifestInfo.apiKey
        root["appVersion"] = manifestInfo.versionName
        root["appVersionCode"] = manifestInfo.versionCode
        root["metadata"] = generateMetadataJson()
        root["sourceControl"] = generateVcsJson()
        root["builderName"] = if (builderName.isPresent) {
            builderName.get()
        } else {
            runCmd("whoami")
        }
        return root
    }

    private fun generateVcsJson(): JSONObject {
        var vcsUrl = sourceControlRepository.orNull
        var commitHash = sourceControlRevision.orNull
        var vcsProvider = sourceControlProvider.orNull
        if (vcsUrl == null) {
            vcsUrl = runCmd(VCS_COMMAND, "config", "--get", "remote.origin.url")
        }
        if (commitHash == null) {
            commitHash = runCmd(VCS_COMMAND, "rev-parse", "HEAD")
        }
        if (vcsProvider == null) {
            vcsProvider = parseProviderUrl(vcsUrl)
        }
        val sourceControlObj = JSONObject()
        sourceControlObj["repository"] = vcsUrl
        sourceControlObj["revision"] = commitHash
        if (isValidVcsProvider(vcsProvider)) {
            sourceControlObj["provider"] = vcsProvider
        }
        return sourceControlObj
    }

    private fun generateMetadataJson(): JSONObject {
        val defaultMetaData = collectDefaultMetaData()
        metadata.orNull?.entries?.forEach { entry: Map.Entry<String, String> ->
            defaultMetaData[entry.key] = entry.value
        }
        val additionalInfo = JSONObject()
        defaultMetaData.entries.forEach { entry: Map.Entry<String?, String?> ->
            additionalInfo[entry.key] = entry.value
        }
        return additionalInfo
    }

    private fun collectDefaultMetaData(): MutableMap<String, String?> {
        val gradleVersion = project.gradle.gradleVersion
        // TODO these should eventually use Gradle's newer env gradle property APIs
        return hashMapOf(
            "os_arch" to System.getProperty(MK_OS_ARCH),
            "os_name" to System.getProperty(MK_OS_NAME),
            "os_version" to System.getProperty(MK_OS_VERSION),
            "java_version" to System.getProperty(MK_JAVA_VERSION),
            "gradle_version" to gradleVersion,
            "git_version" to runCmd(VCS_COMMAND, "--version")
        )
    }

    /**
     * Runs a command on the shell
     * @param cmd the command (arguments must be separate strings)
     * @return the cmd output
     */
    private fun runCmd(vararg cmd: String): String? {
        return try {
            val baos = ByteArrayOutputStream()
            project.exec { execSpec ->
                execSpec.commandLine(*cmd)
                execSpec.standardOutput = baos
                logging.captureStandardError(LogLevel.INFO)
            }
            String(baos.toByteArray(), Charset.forName(CHARSET_UTF8)).trim { it <= ' ' }
        } catch (ignored: ExecException) {
            null
        }
    }

    companion object {
        private val VALID_VCS_PROVIDERS: Collection<String> = listOf("github-enterprise",
            "bitbucket-server", "gitlab-onpremise", "bitbucket", "github", "gitlab")
        private const val MK_OS_ARCH = "os.arch"
        private const val MK_OS_NAME = "os.name"
        private const val MK_OS_VERSION = "os.version"
        private const val MK_JAVA_VERSION = "java.version"
        private const val VCS_COMMAND = "git"
        private const val CHARSET_UTF8 = "UTF-8"

        @JvmStatic
        fun isValidVcsProvider(provider: String?): Boolean {
            return provider == null || VALID_VCS_PROVIDERS.contains(provider)
        }

        @JvmStatic
        fun parseProviderUrl(url: String?): String? {
            if (url != null) {
                for (provider: String in VALID_VCS_PROVIDERS) {
                    if (url.contains((provider))) {
                        return provider
                    }
                }
            }
            return null
        }
    }
}
