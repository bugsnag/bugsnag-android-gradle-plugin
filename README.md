Bugsnag Android Gradle Plugin
=============================

Gradle plugin which automatically uploads ProGuard mapping files to Bugsnag.

> This is a pre-release version, interface is subject to change

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

Contributing
------------

We'd love your contributions! For information on how to build, test and release
this plugin, see our [contributing guide](CONTRIBUTING.md).


License
-------

This plugin is free software released under the MIT License. See
[LICENSE.txt](LICENSE.txt) for details.
