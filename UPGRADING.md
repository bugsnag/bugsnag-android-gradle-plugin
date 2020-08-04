Upgrading Guide
===============

Upgrade from 4.X to 5.X
-----------------------

This release contains **breaking changes**.

### Minimum requirements

The plugin now requires a Gradle wrapper >= 5.1.1, Android Gradle Plugin >= 3.4.0, and JDK >= 8.

### API changes

#### `apiKey` property removed

The `apiKey` property has been removed from the `bugsnag` plugin extension.
You should set this value in your `AndroidManifest.xml` instead as documented in
the [Android Integration Instructions](https://docs.bugsnag.com/platforms/android/#basic-configuration):

```groovy
// old API
bugsnag {
    apiKey "your-api-key-here"
}
```

```xml
<!-- new API -->
<application ...>
  <meta-data android:name="com.bugsnag.android.API_KEY"
             android:value="your-api-key-here"/>
</application>
```

#### Renamed properties on `bugsnag` plugin extension
The following properties have been renamed on the bugsnag plugin extension
to better reflect their purpose:

```groovy
// old API
bugsnag {
    autoUpload = false
    ndk = false
    autoReportBuilds = false
}
```

```groovy
// new API
bugsnag {
    uploadJvmMappings = false
    uploadNdkMappings = false
    reportBuilds = false
}
```

#### `bugsnag` plugin extension uses Property API

Previously the `bugsnag` plugin extension did not use Gradle's 
[Property API](https://docs.gradle.org/current/javadoc/org/gradle/api/provider/Property.html),
which meant the plugin could be configured like thus:

```groovy
// old API
bugsnag {
    enabled false
}
```

All fields on the `bugsnag` plugin extension are now declared as properties. To migrate, you should
make the following change on any affected fields:

```groovy
// new API
bugsnag {
    enabled = false
}
```

#### Added `autoUpdateBuildUuid` flag to prevent manifest UUID generation

A new flag has been added to disable the generation of UUIDs in the manifest. When this flag
is set to false the `versionCode/versionName/applicationId` are used to uniquely identify
mapping files instead.

UUID generation can be disabled like thus:

```groovy
bugsnag {
    autoUpdateBuildUuid = false
}
```

Disabling UUID generation can result in performance improvements but requires a versioning scheme
where each build's version is unique.

#### Multiple Shared object search paths

It is now possible to specify multiple file paths which the plugin will search for shared object files.
A shared object mapping file will be generated and uploaded for any SO files found.

```groovy
// old API
bugsnag {
    sharedObjectPath "app/build/jni/libs"
}
```

```groovy
// new API
def paths = [
    File("app/build/jni/libs"),
    File("app/build/someOtherFolder")
]
bugsnag {
    sharedObjectPaths = paths
}
```

#### `BugsnagProguardConfigTask` removed

The `BugsnagProguardConfigTask` has been removed from the plugin. This task is no longer
required because consumer proguard rules are shipped with the bugsnag-android AARs.

Upgrade from 3.X to 4.X
-----------------------

This release removes support for the [Jack compiler](https://android-developers.googleblog.com/2017/03/future-of-java-8-language-feature.html), but is otherwise backwards compatible. Update the version number to 4.+ to upgrade:

```groovy
dependencies {
    classpath 'com.bugsnag:bugsnag-android-gradle-plugin:4.+'
}
```
