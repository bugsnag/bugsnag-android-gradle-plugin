## 5.2.0 (2020-09-22)

* Support mapping upload from Android Gradle Plugin through a proxy
  [#298](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/298)

## 5.1.0 (2020-09-17)

* Remove uploadDebugBuildMappings flag in favour of variantFilter
  [#301](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/301)

* Fix task ordering for upload task on AGP 3.5
  [#300](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/300)

* Fix bugsnag.sharedObjectPaths not uploading SO files
  [#295](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/295)

## 5.0.2 (2020-09-10)

* Reword log messages that implied duplicate uploads
  [#296](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/296)

* Only set mappingFilesProvider on release task if obfuscation enabled
  [#292](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/292)

* Fix task dependencies and order for BugsnagManifestUuidTask in AGP <4.1.0
  [#294](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/294)

## 5.0.1 (2020-08-26)

* Retry request by constructing new OkHttp request
  [#278](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/278)

* Apply `requestTimeoutMs` as an idle timeout
  [#282](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/282)

* Fix Bintray/jcenter publishing
  [#283](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/283)

## 5.0.0 (2020-08-20)

This release contains **breaking changes**. It contains numerous performance
improvements compared to previous versions of the plugin, such as:

- Supporting [up-to-date checks](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks)
- Supporting [task configuration avoidance](https://docs.gradle.org/current/userguide/task_configuration_avoidance.html)
- Supporting the [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html)
- Avoidance of unnecessary work

See the [Upgrade Guide](./UPGRADING.md) for migration instructions.

### Changes

Support configuration caching
[#257](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/257)

Change how bugsnag plugin is disabled for build variants
[#255](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/255)

Convert plugin extension to use property syntax
[#251](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/251)

Deduplicate unnecessary upload requests for APK splits
[#248](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/248)

Switch to OkHttp for networking
[#247](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/247)
[#268](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/268)
[#269](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/266)

Specify shared object files as input for ndk task
[#243](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/243)

Make upload tasks support up-to-date checks
[#239](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/239)

Avoid unnecessary SO mapping file uploads for ABI splits
[#238](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/238)

Alter bugsnag extension to allow multiple shared object paths
[#237](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/237)

Support for manifest processing in AGP 4.1.0-alpha04 and above
[#234](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/234)
[#236](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/236)
[#240](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/240)

Alter bugsnag tasks to support the Incremental Build API
[#230](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/230)

Rename properties on bugsnag extension that control request autoUpload
[#231](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/231)

Bump supported JDK version to 8
[#224](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/224)

Use register() rather than create() for adding tasks
[#221](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/221)
[#227](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/227)

Avoid eager task initialization
[#261](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/261)

Remove unused BugsnagProguardConfigTask
[#209](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/209)

Make AGP dependency compile only
[#211](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/211)

Remove apiKey property from bugsnag plugin extension
[#210](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/210)

## 4.7.5 (2020-05-18)

Add compatibility with AGP 4.0 by using the `mappingFileProvider` API when possible
[#202](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/202)

## 4.7.4 (2020-03-11)

Prevent existing buildUuid being overridden by gradle plugin
[#192](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/192)

## 4.7.3 (2020-01-14)

Use getOrNull() rather than Provider#get()
[#188](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/188)

## 4.7.2 (2019-11-06)

Allow overriding the 'appVersion' field from the AndroidManifest
[#181](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/181)

## 4.7.1 (2019-10-24)

Fix AGP 3.6.0 breaking project builds due to changed return type of `getBundleManifestOutputDirectory()`
[#179](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/179)

Avoid uploading zero-length proguard mapping files
[#180](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/180)

## 4.7.0 (2019-10-14)

Add ability to override reported value for versionCode
[#175](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/175)

## 4.6.0 (2019-09-24)

Add configurable timeout for all http requests
[#173](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/173)

## 4.5.2 (2019-07-18)

Update plugin to extract SO files from transitive dependencies
[#171](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/171)

## 4.5.1 (2019-07-17)

Lookup shared object files correctly in new artefact IDs
[#170](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/170)

## 4.5.0 (2019-07-15)

Search for shared object files in the new artefact locations introduced by modularisation of `bugsnag-android`. This affects versions v4.17.0 and above of `bugsnag-android` and `bugsnag-android-ndk`.
[#168](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/168)

## 4.4.1 (2019-06-19)

Skip uploading mapping files for shared objects which have no debug info
[#166](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/166)

## 4.4.0 (2019-06-10)

This release is companion update for bugsnag-android v4.15.0, which supports detecting and reporting C/C++ crashes without a separate library. 

Since `bugsnag-android` now contains native code, update shared object extraction to include libraries from there as well as `bugsnag-android-ndk`
[#164](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/164)

## 4.3.1 (2019-05-31)

* Calculate correct location for objdump on windows, fixing NDK symbol upload
[#163](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/163)

## 4.3.0 (2019-05-30)

* Resolve pre-existing CodeNarc style violations
[#160](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/160)

## 4.2.0 (2019-04-16)

* Alter NDK project detection to check for the cmake/ndkbuild path
[#156](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/156)

* Disable proguard mapping task setup when variant does not obfuscate
[#157](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/157)

## 4.1.2 (2019-04-02)

* Fix task ordering of build UUID generation when shrinkResources enabled
[#155](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/155)

## 4.1.1 (2019-03-21)

* Add BUILD_UUID to app bundle manifests
[#153](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/153)

## 4.1.0 (2019-03-13)

Note: this version of the plugin will fail the build if a mapping file is not uploaded successfully.
Previously if this occurred the failure would have been logged as an error and the build would have continued.

This behaviour can be disabled by setting `failOnUploadError` to `false`.

* Fail on upload error
[#151](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/151)

## 4.0.0 (2019-01-23)

* Remove support for Jack compiler
[#142](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/142)

* Address provider api warnings
[#146](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/146)

## 3.6.0 (2018-12-12)

* Support automatic upload when building App Bundles
[#137](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/137)

* Manually calculate location of objdump in gradle plugin
[#136](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/136)

### Bug fixes

* Create tasks for debug variant which are not run by default
[#139](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/139)

## 3.5.0 (2018-10-18)

### Bug fixes

* Add option for setting projectRoot on bugsnag plugin extension
[#133](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/133)

## 3.4.2 (2018-09-27)

### Bug fixes

* Fix native artifact resolution for NDK projects manually installing
  bugsnag-android-ndk
  [#130](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/130)

## 3.4.1 (2018-09-11)

### Bug fixes

* Reduce processing requirements to improve upload speed for NDK mapping files
  [Dave Perryman](https://github.com/Pezzah)
  [#129](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/129)

* Close IO streams after the plugin has finished reading and writing
  [Jamie Lynch](https://github.com/fractalwrench)
  [#126](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/126)

## 3.4.0 (2018-08-30)

* Compress Android NDK mapping files to decrease upload times
[Dave Perryman](https://github.com/Pezzah) [#127](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/127)

## 3.3.1 (2018-08-10)

### Bug fixes

* Support Provider API introduced in gradle 4.7, which the manifestOutputDirectory in AGP 3.3.0-alpha04 now uses
[Jamie Lynch](https://github.com/fractalwrench) [#122](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/122)

## 3.3.0 (2018-07-20)

* Improve support for Dexguard APK splits by manually calculating location of mapping file
[Jamie Lynch](https://github.com/fractalwrench) [#117](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/117)

### Bug fixes

* Add a version check for bugsnag-android dependency, disabling redundant ProGuard config if >= v4.6.0
[Jamie Lynch](https://github.com/fractalwrench) [#116](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/116)

## 3.2.8 (2018-06-21)

### Bug fixes

* Update ProGuard keep rules for Breadcrumb class, fixing NDK compatibility
[Jamie Lynch](https://github.com/fractalwrench) [#114](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/114)

## 3.2.7 (2018-06-05)

### Bug fixes

* Add compatibility with Gradle 4.8 - use def rather than TreeSet when finding the buildType
[Jamie Lynch](https://github.com/fractalwrench) [#110](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/110)

## 3.2.6 (2018-04-24)

* Update Android Plugin for Gradle version to 3.1.0

### Bug fixes

* Fix possible failed NDK symbol map uploads which could occur if the mapping
  file was still being written when the process status was checked
  [Jamie Lynch](https://github.com/fractalwrench)
  [#104](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/104)

* Fix malformed metadata uploaded by release task
  [Jamie Lynch](https://github.com/fractalwrench)
  [#91](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/91)

* Reduce log level of failed shell commands in the release task. Shell commands
  are used to automatically detect source control information and are not a
  critical error if one fails to run.
  [Jamie Lynch](https://github.com/fractalwrench)
  [#88](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/88)

## 3.2.5 (2018-02-20)

### Bug fixes

* Support applying the gradle plugin to a module separate from the base
  application by changing the task execution order when generating and attaching
  build UUIDs
  [Jamie Lynch](https://github.com/fractalwrench)
  [#87](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/87)

## 3.2.4 (2018-01-19)

* Remove reference to unused task resulting in build failure

## 3.2.3 (2018-01-18)

* Add example projects with gradle plugin
* Fix manifest lookup in project with only APK splits enabled

## 3.2.2 (2018-01-15)

* Fixes versionCode cast exception in upload task

## 3.2.1 (2018-01-10)

* Automatically add copy task for shared object files to NDK projects

## 3.2.0 (2018-01-09)

* Automatically record release/build information to Bugsnag Build API
* NDK projects are now automatically detected

## 3.1.1 (2017-12-07)

* Fix location of shared object files for NDK

## 3.1.0 (2017-11-17)

* Finalise upload task on assemble rather than build
* Support Library variant upload

## 3.0.0 (2017-10-26)

* Support Android Studio 3 stable release

## 3.0.0-beta5 (2017-10-25)

* Add separate task for holding Splits Info

## 3.0.0-beta4 (2017-10-16)

* Fix upload of files for ARM ABI splits

## 3.0.0-beta3 (2017-10-10)

* Support upload of APK splits

#### Breaking changes
If you run the upload task manually, the task name has now changed to use the VariantOutput name, rather than the Variant name.

```shell
./gradlew clean build uploadBugsnag${variantOutputName}Mapping
```

For example, the following would upload the release mapping file for the `x86` split of the `javaExample` productFlavor in the `example` module:
```shell
./gradlew clean build :example:uploadBugsnagJavaExample-x86-releaseMapping
```

## 3.0.0-beta2 (2017-09-22)

* Expose manual upload gradle task

## 3.0.0-beta (2017-09-06)

* Support Android Plugin for Gradle 3.0.0

## 2.4.2 (2017-05-10)

## Bug fixes

* Improve performance of NDK object file generation

## 2.4.1 (2017-01-30)

## Enhancements

* Support NDK symbols in versioned dependent libraries

## Bug fixes

* Fix typo in internal Proguard configuration

## 2.4.0 (2017-01-27)

## Enhancements

Add support for uploading NDK symbols

## 2.3.2 (2016-09-26)

### Bug Fixes

* Reduce dependent version of HttpClient to 4.2.1

## 2.3.1 (2016-09-15)

### Bug Fixes

* Fix error log on upload failures unrelated to server/network outage

## 2.3.0 (2016-09-13)

### Enhancements

* Support retrying the mapping file upload via `retryCount` config property
  [Felipe Duarte](https://github.com/fcduarte)
  [#27](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/27)

### Bug Fixes

* Fix build failure if the Bugsnag server could not be reached

## 2.2.1 (2016-09-05)

### Bug fixes

* Restore support for running alongside older versions of HttpClient

## 2.2.0 (2016-09-05)

### Enhancements

* Add option to overwrite a mapping during upload
  [Felipe Duarte](https://github.com/fcduarte)
  [#25](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/25)

## 2.1.3 (2016-08-31)

* Fix build error during task execution

## 2.1.2 (2016-08-30)

* Avoid creating new build UUID for Instant Run builds

## 2.1.1 (2016-05-18)

* Update `enableBugsnag` feature to be configurable on a per-variant basis
| [Tim Mellor](https://github.com/tmelz)
| [#22](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/22)

## 2.1.0 (2016-05-13)

### Enhancements

* Add configuration option `enableBugsnag` allowing you to completely disable Bugsnag in certain situations
  | [Tim Mellor](https://github.com/tmelz)
  | [#21](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/21)
* Plugin id changed to `com.bugsnag.android.gradle` from `bugsnag`
* Now published on https://plugins.gradle.org

## 2.0.2 (2016-03-16)

* Depend on Android Gradle plugin 1.5 to avoid lint warnings

## 2.0.1 (2016-03-14)

### Bug Fixes

* Target Java 6 when building, instead of Java 8

## 2.0.0 (2016-03-11)

This update added a few enhancements making usage easier and more customizable.
There are more details in the
[README](https://github.com/bugsnag/bugsnag-android-gradle-plugin#configuration)
about the improvements to configuration. While gradle compatibility is
preserved, this is a breaking change for users of previous versions of the
plugin.

### Bug Fixes

* Fix support for Android plugin 1.5+
  [James Smith](https://github.com/loopj)
  [#10](https://github.com/bugsnag/bugsnag-android-gradle-plugin/pull/10)

1.2.2 (2016-03-02)
-----

-   Fix bug causing obfuscated variants to not be detected with newer
    Android toolchains

1.2.1 (2016-01-18)
-----

-   Configure Bugsnag plugin after `project` has been evaluated

1.2.0
-----

- Add build UUID support

1.1.1
-----

- Fix bug when multiple variants have proguard configured

1.1.0
-----

- Configure proguard correctly automatically

1.0.0
-----

-   Send versionCode & applicationId
-   Fix when -dontobfuscate is configured in proguard
-   Fix when multiple variants have proguard enabled

0.1.0
-----

-   Initial release
