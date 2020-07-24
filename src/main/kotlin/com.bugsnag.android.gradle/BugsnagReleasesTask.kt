package com.bugsnag.android.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
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

    @TaskAction
    fun fetchReleaseInfo() {
        val manifestInfo = parseManifestInfo()
        val bugsnag = project.extensions.getByType(BugsnagPluginExtension::class.java)
        val payload = generateJsonPayload(manifestInfo, bugsnag)
        val json = payload.toString()
        project.logger.lifecycle("Bugsnag: Attempting upload to Releases API")

        object : Call(project) {
            @Throws(IOException::class)
            override fun makeApiCall(): Boolean {
                val response = deliverPayload(payload, manifestInfo, bugsnag)
                requestOutputFile.asFile.get().writeText(response)
                project.logger.lifecycle("Bugsnag: Upload succeeded")
                return true
            }
        }.execute()
    }

    private fun deliverPayload(payload: JSONObject,
                               manifestInfo: AndroidManifestInfo,
                               bugsnag: BugsnagPluginExtension): String {
        var os: OutputStream? = null
        try {
            val url = URL(bugsnag.releasesEndpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Bugsnag-Api-Key", manifestInfo.apiKey)
            conn.readTimeout = bugsnag.requestTimeoutMs
            conn.connectTimeout = bugsnag.requestTimeoutMs
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

    private fun generateJsonPayload(manifestInfo: AndroidManifestInfo, bugsnag: BugsnagPluginExtension): JSONObject {
        val root = JSONObject()
        root["buildTool"] = "gradle-android"
        root["apiKey"] = manifestInfo.apiKey
        root["appVersion"] = manifestInfo.versionName
        root["appVersionCode"] = manifestInfo.versionCode
        root["metadata"] = generateMetadataJson(bugsnag)
        root["sourceControl"] = generateVcsJson(bugsnag)
        root["builderName"] = if (bugsnag.builderName != null) {
            bugsnag.builderName
        } else {
            runCmd("whoami")
        }
        return root
    }

    private fun generateVcsJson(bugsnag: BugsnagPluginExtension): JSONObject {
        val sourceControl = bugsnag.sourceControl
        var vcsUrl = sourceControl.repository
        var commitHash = sourceControl.revision
        var vcsProvider = sourceControl.provider
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

    private fun generateMetadataJson(bugsnag: BugsnagPluginExtension): JSONObject {
        val metadata = collectDefaultMetaData()
        bugsnag.metadata?.entries?.forEach { entry: Map.Entry<String, String> ->
            metadata[entry.key] = entry.value
        }
        val additionalInfo = JSONObject()
        metadata.entries.forEach { entry: Map.Entry<String?, String?> ->
            additionalInfo[entry.key] = entry.value
        }
        return additionalInfo
    }

    private fun collectDefaultMetaData(): MutableMap<String, String?> {
        val gradleVersion = project.gradle.gradleVersion
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