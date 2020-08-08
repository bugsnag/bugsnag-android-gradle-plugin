package com.bugsnag.android.gradle

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecException
import org.gradle.util.VersionNumber
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.time.Duration
import javax.inject.Inject

open class BugsnagReleasesTask @Inject constructor(
    objects: ObjectFactory,
    private val providerFactory: ProviderFactory
) : DefaultTask(), AndroidManifestInfoReceiver {

    init {
        group = BugsnagPlugin.GROUP_NAME
        description = "Assembles information about the build that will be sent to the releases API"
    }

    @get:Internal
    internal val uploadRequestClient: Property<UploadRequestClient> = objects.property(UploadRequestClient::class.java)

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

    @get:Input
    @get:Optional
    val osArch: Property<String> = objects.property(String::class.java)

    @get:Input
    @get:Optional
    val osName: Property<String> = objects.property(String::class.java)

    @get:Input
    @get:Optional
    val osVersion: Property<String> = objects.property(String::class.java)

    @get:Input
    @get:Optional
    val javaVersion: Property<String> = objects.property(String::class.java)

    @get:Input
    @get:Optional
    val gradleVersion: Property<String> = objects.property(String::class.java)

    @get:Input
    @get:Optional
    val gitVersion: Property<String> = objects.property(String::class.java)

    @TaskAction
    fun fetchReleaseInfo() {
        val manifestInfo = parseManifestInfo()
        val payload = generateJsonPayload(manifestInfo)

        val response = uploadRequestClient.get().makeRequestIfNeeded(manifestInfo, payload.toString()) {
            logger.lifecycle("Bugsnag: Attempting upload to Releases API")
            lateinit var response: String
            object : Call(retryCount, logger) {
                override fun makeApiCall(): Boolean {
                    response = deliverPayload(payload, manifestInfo)
                    requestOutputFile.asFile.get().writeText(response)
                    logger.lifecycle("Bugsnag: Upload succeeded")
                    return true
                }
            }.execute()
            response
        }
        requestOutputFile.asFile.get().writeText(response)
        logger.lifecycle("Bugsnag: Releases request complete")
    }

    private fun deliverPayload(
        payload: ReleasePayload,
        manifestInfo: AndroidManifestInfo
    ): String {
        val timeoutDuration = Duration.ofMillis(timeoutMillis.get())
        val bugsnagService = createService(timeoutDuration)

        val response = try {
            bugsnagService.upload(
                releasesEndpoint.get(),
                apiKey = manifestInfo.apiKey,
                payload = payload
            ).execute()
        } catch (e: IOException) {
            throw IllegalStateException("Request to Bugsnag Releases API failed, aborting build.", e)
        }
        return readRequestResponse(response)
    }

    private fun readRequestResponse(response: Response<String>): String {
        val statusCode = response.code()
        val success = statusCode == 200
        val responseData = when {
            success -> response.body().orEmpty()
            else -> response.errorBody()?.string().orEmpty()
        }
        return when {
            success -> responseData
            else -> {
                logger.error(responseData)
                throw IllegalStateException("Request to Bugsnag Releases API failed, aborting build.")
            }
        }
    }

    private fun generateJsonPayload(manifestInfo: AndroidManifestInfo): ReleasePayload {
        return ReleasePayload(
            buildTool = "gradle-android",
            apiKey = manifestInfo.apiKey,
            appVersion = manifestInfo.versionName,
            appVersionCode = manifestInfo.versionCode,
            metadata = generateMetadataJson(),
            sourceControl = generateVcsJson(),
            builderName = if (builderName.isPresent) {
                builderName.get()
            } else {
                runCmd("whoami")
            }
        )
    }

    private fun generateVcsJson(): Map<String, String?> {
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
        val sourceControlObj = mutableMapOf<String, String?>()
        sourceControlObj["repository"] = vcsUrl
        sourceControlObj["revision"] = commitHash
        if (isValidVcsProvider(vcsProvider)) {
            sourceControlObj["provider"] = vcsProvider
        }
        return sourceControlObj
    }

    private fun generateMetadataJson(): Map<String, String?> {
        val metadataMap = mutableMapOf<String, String?>()
        collectDefaultMetaData(metadataMap)
        metadataMap.putAll(metadata.orNull.orEmpty())
        return metadataMap.toMap()
    }

    private fun collectDefaultMetaData(map: MutableMap<String, String?>) {
        map["os_arch"] = osArch.orNull
        map["os_name"] = osName.orNull
        map["os_version"] = osVersion.orNull
        map["java_version"] = javaVersion.orNull
        map["gradle_version"] = gradleVersion.orNull
        map["git_version"] = gitVersion.orNull
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

    internal fun configureMetadata() {
        val gradleVersionNumber = gradleVersion.orNull?.let {
            gradleVersion.set(it)
            VersionNumber.parse(it)
        }
        gitVersion.set(providerFactory.provider { runCmd(VCS_COMMAND, "--version") } )
        if (gradleVersionNumber != null && gradleVersionNumber >= SYS_PROPERTIES_VERSION)  {
            osArch.set(providerFactory.systemProperty(MK_OS_ARCH) )
            osName.set(providerFactory.systemProperty(MK_OS_NAME) )
            osVersion.set(providerFactory.systemProperty(MK_OS_VERSION) )
            javaVersion.set(providerFactory.systemProperty(MK_JAVA_VERSION))
        } else {
            osArch.set(providerFactory.provider { System.getProperty(MK_OS_ARCH) } )
            osName.set(providerFactory.provider { System.getProperty(MK_OS_NAME) } )
            osVersion.set(providerFactory.provider { System.getProperty(MK_OS_VERSION) } )
            javaVersion.set(providerFactory.provider { System.getProperty(MK_JAVA_VERSION) })
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
        private val SYS_PROPERTIES_VERSION = VersionNumber.parse("6.1")

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

        private fun createService(
            timeoutDuration: Duration
        ): BugsnagReleasesService {
            return createService(OkHttpClient.Builder()
                .connectTimeout(timeoutDuration)
                .callTimeout(timeoutDuration)
                .build())
        }

        internal fun createService(
            okHttpClient: OkHttpClient
        ): BugsnagReleasesService {
            return Retrofit.Builder()
                .baseUrl("https://upload.bugsnag.com") // Not actually used
                .validateEagerly(true)
                .callFactory(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create()
        }
    }
}

@JsonClass(generateAdapter = true)
internal data class ReleasePayload(
    val buildTool: String,
    val apiKey: String,
    val appVersion: String,
    val appVersionCode: String,
    val metadata: Map<String, String?>,
    val sourceControl: Map<String, String?>,
    val builderName: String?
)

internal interface BugsnagReleasesService {
    @POST
    fun upload(
        @Url endpoint: String,
        @Header("Bugsnag-Api-Key") apiKey: String,
        @Body payload: ReleasePayload
    ): retrofit2.Call<String>
}
