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

By default the bugsnag-android-gradle-plugin reads your API key [from your manifest](https://github.com/bugsnag/bugsnag-android#configuring-your-androidmanifest). You should ensure that you have the following code in AndroidManifest.xml.

```xml
<application ...>
    <meta-data android:name="com.bugsnag.android.API_KEY" android:value="your-api-key-here"/>
</application>
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

Proguard Configuration
----------------------

In order for Bugsnag to deduplicate errors correctly it needs to know the file and line at which the error
occurred. Proguard by default strips this information, so the bugsnag-android-gradle-plugin adds an extra
line to your Proguard configuration:

    -keepattributes SourceFile,LineNumberTable

BuildUUID
---------

The bugsnag-android-gradle-plugin adds an additional meta-data property to your AndroidManifest with the
key "com.bugsnag.BUILD_UUID". This is used, along with appId and versionCode to identify which proguard
mapping file to use when receiving a crash.

Contributing
------------

We'd love your contributions! For information on how to build, test and release
this plugin, see our [contributing guide](CONTRIBUTING.md).


License
-------

This plugin is free software released under the MIT License. See
[LICENSE.txt](LICENSE.txt) for details.
