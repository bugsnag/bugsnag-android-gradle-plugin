Upgrading Guide
===============

Upgrade from 5.x to 7.X
-----------------------

This release contains **breaking changes**.

```groovy
dependencies {
    classpath 'com.bugsnag:bugsnag-android-gradle-plugin:7.x.x'
}
```

### Minimum requirements

The plugin now requires a Gradle wrapper >= 7.0.0, Android Gradle Plugin >= 7.0.0, and JDK >= 11.

## Upgrade from 4.x to 7.x

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

#### Changed properties on `bugsnag` plugin extension
The following properties have been renamed on the bugsnag plugin extension
to better reflect their purpose:

```groovy
// old API
bugsnag {
    autoUpload false
    ndk false
    autoReportBuilds false
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

The `sharedObjectPath` property has been renamed to `sharedObjectPaths` and changed to a `List`:

```groovy
// old API
bugsnag {
    sharedObjectPath = "app/build/jni/libs"
}
```

```groovy
// new API
bugsnag {
    sharedObjectPaths = [
        "app/build/jni/libs"
    ]
}
```

The `requestTimeoutMs` property has been changed to a `Long`:

```groovy
// old API
bugsnag {
    requestTimeoutMs 5000
}
```

```groovy
// new API
bugsnag {
    requestTimeoutMs = 5000L
}
```

#### `bugsnag` plugin extension uses Property API

Previously the `bugsnag` plugin extension did not use Gradle's 
[Property API](https://docs.gradle.org/current/javadoc/org/gradle/api/provider/Property.html),
which meant the plugin could be configured like this:

```groovy
// old API
bugsnag {
    uploadJvmMappings false
}
```

All fields on the `bugsnag` plugin extension are now declared as properties. To migrate, you should
make the following change on any affected fields:

```groovy
// new API
bugsnag {
    uploadJvmMappings = false
}
```

#### Change how bugsnag plugin is disabled for build variants

You should disable the bugsnag plugin for any build variants which do not use obfuscation.
The old API for disabling the plugin on individual build variants has been removed:

```groovy
// old API
android {
    buildTypes {
        debug {
            ext.enableBugsnag = false
        }
    }
}
```

You should use the new API instead. This allows for multiple build variants to be ignored at once
and behaves similarly to AGP's [variant filtering API](https://developer.android.com/studio/build/build-variants#filter-variants):

```groovy
// new API
bugsnag {
    variantFilter { variant ->
        // disables plugin for all variants with debug buildType
        def name = variant.name.toLowerCase()
        if (name.contains("debug")) {
            enabled = false
        }
    }
}
```

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
    new File("app/build/jni/libs"),
    new File("app/build/someOtherFolder")
]
bugsnag {
    sharedObjectPaths = paths
}
```

#### `BugsnagProguardConfigTask` removed

The `BugsnagProguardConfigTask` has been removed from the plugin, along with the `autoProguardConfig` property. 
This task is no longer required because consumer proguard rules are shipped with the bugsnag-android AARs.

Upgrade from 3.X to 4.X
-----------------------

This release removes support for the [Jack compiler](https://android-developers.googleblog.com/2017/03/future-of-java-8-language-feature.html), but is otherwise backwards compatible. Update the version number to 4.+ to upgrade:

```groovy
dependencies {
    classpath 'com.bugsnag:bugsnag-android-gradle-plugin:4.x.x'
}
```
