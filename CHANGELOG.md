## 2.0.0 (2016-03-11)

This update added a few enhancements making usage easier and more customizable.
There are more details in the
[README](https://github.com/bugsnag/bugsnag-android-gradle-plugin#configuration)
about the improvements to configuration. While gradle compatibility is
preserved, this is a breaking change for users of previous versions of the
plugin.

### Bug Fixes

* Fix support for Android plugin 1.5+
  [James Smith](https://github.com/loopj]
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
