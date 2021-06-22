Feature: Auto update build UUID flag

Scenario: Build UUID excluded from request when set to false
    When I build "manual_build_uuid" using the "standard" bugsnag config
    And I wait to receive 2 builds

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
      | 1              | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       | overwrite | buildUUID       |
      | 1           | 1.0         | com.bugsnag.android.example | null      | same-build-uuid |
