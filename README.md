Bugsnag Android Gradle Plugin
=============================

This Gradle plugin automatically uploads [ProGuard](https://developer.android.com/tools/help/proguard.html) and [DexGuard](https://www.guardsquare.com/en/dexguard) mapping files to Bugsnag.

If you aren't using Gradle or need more manual control, [see the API docs](https://bugsnag.com/docs/notifiers/android/proguard) for alternative ways to use Bugsnag with Proguard.

[Bugsnag](https://bugsnag.com/platforms/android) for Android automatically detects crashes in your Android apps, collecting diagnostic information and immediately notifying your development team.

[Create a free account](https://bugsnag.com) to start capturing exceptions from your applications.

## Features

* Automatically upload [ProGuard](https://developer.android.com/tools/help/proguard.html) and [DexGuard](https://www.guardsquare.com/en/dexguard) mapping files to deobfuscate your stacktraces
* Automatically upload [NDK Symbol Maps](https://docs.bugsnag.com/api/ndk-symbol-mapping-upload/) to deobfuscate NDK stacktraces
* Automatically report build information to track the [health of your releases](https://docs.bugsnag.com/product/releases/)

## Getting started

1. [Create a Bugsnag account](https://bugsnag.com)
1. Complete the instructions in the [integration guide](https://docs.bugsnag.com/build-integrations/gradle/) to setup the Gradle Plugin
1. Customize your integration using the [configuration options](http://docs.bugsnag.com/build-integrations/gradle/#additional-configuration/)

## Support

* [Read the integration guide](https://docs.bugsnag.com/build-integrations/gradle/) or [configuration options documentation](http://docs.bugsnag.com/build-integrations/gradle/#additional-configuration/)
* [Search open and closed issues](https://github.com/bugsnag/bugsnag-android-gradle-plugin/issues?utf8=✓&q=is%3Aissue) for similar problems
* [Report a bug or request a feature](https://github.com/bugsnag/bugsnag-android-gradle-plugin/issues/new)
- [Additional Documentation](https://bugsnag.com/docs/notifiers/android/proguard)

## Contributing

All contributors are welcome! For information on how to build, test
and release `bugsnag-android-gradle-plugin`, see our
[contributing guide](https://github.com/bugsnag/bugsnag-android-gradle-plugin/blob/master/CONTRIBUTING.md).


## License

The Bugsnag Android Gradle Plugin is free software released under the MIT License.
See [LICENSE.txt](https://github.com/bugsnag/bugsnag-android/blob/master/LICENSE.txt)
for details.
