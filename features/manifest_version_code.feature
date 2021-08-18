Feature: Manifest versionCode override

Scenario: App versionCode override in AndroidManifest.xml
    When I build "default_app" using the "custom_manifest_version_code" bugsnag config
    And I wait to receive a build
    And I wait to receive an upload

    Then 1 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
        | 9000           | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 uploads are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                       | overwrite |
        | 9000        | 1.0         | com.bugsnag.android.example | null      |

    And 1 uploads have an R8 mapping file with the following symbols:
        | jvmSymbols |
        | com.Bar |
        | void doSomething() |
