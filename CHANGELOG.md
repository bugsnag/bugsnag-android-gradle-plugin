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
