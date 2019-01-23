Upgrading Guide
===============

Upgrade from 3.X to 4.X
-----------------------

This release removes support for the [Jack compiler](https://android-developers.googleblog.com/2017/03/future-of-java-8-language-feature.html), but is otherwise backwards compatible. Update the version number to 4.+ to upgrade:

```groovy
dependencies {
    classpath 'com.bugsnag:bugsnag-android-gradle-plugin:4.+'
}
```
