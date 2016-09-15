Bugsnag Android Gradle Plugin
=============================

Gradle plugin to automatically upload [ProGuard](https://developer.android.com/tools/help/proguard.html) mapping files to Bugsnag.

[Bugsnag](https://bugsnag.com/platforms/android) for Android automatically detects crashes in your Android apps, collecting diagnostic information and immediately notifying your development team.

[Create a free account](https://bugsnag.com) to start capturing exceptions from your applications.

If you aren't using Gradle or need more manual control, [see the API docs](https://bugsnag.com/docs/notifiers/android/proguard) for alternative ways to use Bugsnag with Proguard.

# Contents

- [Getting Started](#getting-started)
    - [Installation](#installation)
- [Configuration](#configuration)
    - [API Key](#api-key)
    - [Automatic Upload](#automatic-upload)
    - [Automatic Proguard Config](#automatic-proguard-config)
    - [Custom Endpoints](#custom-endpoints)
    - [Disabling Bugsnag](#disabling-bugsnag)
    - [Overwrite mapping file](#overwrite-mapping-file)
- [Build UUIDs](#build-uuids)
- [Support](#support)
- [Contributing](#contributing)
- [License](#license)


# Getting Started

## Installation

### Using the Gradle Plugin Portal (Gradle 2.1+)

Add the plugin to the `plugins` section at the top of your Module-level Gradle
Settings, at `<project dir>/<module name>/build.gradle` (usually
`<project_dir>/app/build.gradle`):

```groovy
plugins {
    id "com.bugsnag.android.gradle" version "2.3.1"
}
```

### Using other source repositories

Add this plugin as a dependency in your main *Project Gradle Settings*, at `<project_dir>/build.gradle`:

```groovy
buildscript {
    dependencies {
        // Add this line to your `dependencies` section
        classpath 'com.bugsnag:bugsnag-android-gradle-plugin:2.3.1'
    }
}
```

You'll then need to "apply" the plugin by adding the following line to the top of your *Module Gradle Settings*, at `<project_dir>/<module_name>/build.gradle` (usually `<project_dir>/app/build.gradle`).

```groovy
apply plugin: com.bugsnag.android.gradle.BugsnagPlugin
```


# Configuration

In most situations, the basic installation steps above will suffice, but you can manually configure further settings as follows:

## API Key

By default this plugin will read your API key [from your manifest](https://github.com/bugsnag/bugsnag-android#configuring-your-androidmanifest). You should ensure that you have the following code in `AndroidManifest.xml`.

```xml
<application ...>
    <meta-data android:name="com.bugsnag.android.API_KEY" android:value="your-api-key-here"/>
</application>
```

Alternatively, you can set your API key in your app's `build.gradle` as follows:

```groovy
bugsnag {
    apiKey 'your-api-key-here'
}
```

## Automatic Upload

By default, this plugin will automatically upload the proguard mapping file generated for every build, if you'd prefer to disable this automatic uploading, set the `autoUpload` property in your app's `build.gradle`:

```groovy
bugsnag {
    autoUpload false
}
```

If you disable automatic uploading, you can still run the upload task manually, with the uploadBugsnag*Variant*Mapping task:

```shell
./gradlew clean package uploadBugsnagReleaseMapping
```

## Automatic Proguard Config

In order for Bugsnag to de-duplicate errors correctly it needs to know the file and line number for every crash. Proguard strips this information by default, so we'll automatically add the following line into your Proguard configuation to avoid this:

```
-keepattributes SourceFile,LineNumberTable
```

If you'd prefer to add this into your Proguard configuration yourself, you can disable our automatic configuration in your app's `build.gradle` as follows:

```groovy
bugsnag {
    autoProguardConfig false
}
```

## Custom Endpoints

By default, we'll upload mapping files to `upload.bugsnag.com`, if you are using Bugsnag Enterprise, you can configure your upload endpoint in your app's `build.gradle` as follows:

```groovy
bugsnag {
    endpoint 'http://upload.bugsnag.example.com'
}
```

## Disabling Bugsnag
To completely disable the Bugsnag plugin, use the extension property `enableBugsnag`, for example:

```groovy
android {
    buildTypes {
        debug {
            ext.enableBugsnag = false
        }
    }
}
```

## Overwrite mapping file
To overwrite a mapping file, you can set the `overwrite` property as follows:

```groovy
bugsnag {
    overwrite false
}
```

You can also declare this inside a product flavor. Conflicting values (between multiple flavors or a flavor and a build-type) are ignored and the plugin will be disabled if `false` was set anywhere.

You may want to do this to speed up build types or flavors where you don't require Bugsnag's functionality. Bugsnag's generation of a UUID for each build into your manifest (see [Build UUIDs](#build-uuids)) causes the resources task to be run for each build, introducing a small delay if your build's changes don't involve resources.

## Retrying the file upload
To enable retrying the file upload, set the `retryCount` property:

```groovy
bugsnag {
    retryCount 5
}
```

# Build UUIDs

This plugin automatically generates a UUID for each build (excluding [Instant
Run builds](https://medium.com/google-developers/instant-run-how-does-it-work-294a1633367f),
which is inserted into your `AndroidManifest.xml` during the build process. This
UUID is used to uniquely identify each build, along with your `appId` and
`versionCode`, to help us determine which proguard mapping file to use with each
crash report.

This UUID is available as `com.bugsnag.BUILD_UUID` in a [meta-data
element](https://developer.android.com/guide/topics/manifest/meta-data-element.html)
in case you'd like to use yourself it in your applications.


# Support

- [Additional Documentation](https://bugsnag.com/docs/notifiers/android/proguard)
- [Search open and closed issues](https://github.com/bugsnag/bugsnag-android-gradle-plugin/issues?utf8=✓&q=is%3Aissue) for similar problems
- [Report a bug or request a feature](https://github.com/bugsnag/bugsnag-android-gradle-plugin/issues/new)


# Contributing

We'd love your contributions! For information on how to build, test and release
this plugin, see our [contributing guide](CONTRIBUTING.md).


# License

This plugin is free software released under the MIT License. See
[LICENSE.txt](LICENSE.txt) for details.
