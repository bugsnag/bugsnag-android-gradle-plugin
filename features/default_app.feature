Feature: Plugin integrated in default app

Scenario: Single-module default app builds successfully
    When I build "default_app" using the "standard" bugsnag config
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
      | 1              | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       | overwrite |
      | 1           | 1.0         | com.bugsnag.android.example | null      |
