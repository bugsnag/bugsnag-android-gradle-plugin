Bugsnag Android Gradle Plugin
=============================

Gradle plugin which automatically uploads ProGuard mapping files to Bugsnag. If you
need more manual control, [see the API docs](https://bugsnag.com/docs/notifiers/android/proguard).

Basic Usage
-----------

Add the following to your `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.bugsnag:bugsnag-android-gradle-plugin:+'
    }
}

apply plugin: 'bugsnag'
```


Advanced Usage
--------------

In most cases, the basic usage above will suffice, but you can manually set
your Bugsnag API key and upload endpoint as follows:

```groovy
bugsnag {
    // We automatically fetch your Bugsnag API key from your AndroidManifest
    // where possible, but you can override it here
    apiKey 'your-api-key-here'

    // By default, we'll upload mapping files to upload.bugsnag.com, if you
    // are using Bugsnag Enterprise, you can configure your upload endpoint
    endpoint 'http://upload.bugsnag.example.com'
}
```

By default Bugsnag refuses to let you upload multiple proguard files for a given
versionCode of an app. If this happens you'll see an error:

    Bugsnag upload failed: duplicate proguard file appId=com.bugsnag.android.example.debug versionCode=7

The easiest way to fix this is to bump your versionCode and re-build, but if you cannot do that for some
reason then you can set the `bugsnag.overwrite` system property to true. This will delete the previous
version of the proguard file from Bugsnag's servers.

    ./gradlew uploadBugsnagReleaseMapping -Dbugsnag.overwrite=true

Proguard Configuration
----------------------

In order for Bugsnag to deduplicate errors correctly it needs to know the file and line at which the error
occurred. Proguard by default strips this information, so the bugsnag-android-gradle-plugin adds an extra
line to your Proguard configuration:

    -keepattributes SourceFile,LineNumberTable

Contributing
------------

We'd love your contributions! For information on how to build, test and release
this plugin, see our [contributing guide](CONTRIBUTING.md).


License
-------

This plugin is free software released under the MIT License. See
[LICENSE.txt](LICENSE.txt) for details.
