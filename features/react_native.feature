Feature: Plugin integrated in React Native app

Scenario: React Native sends requests
    When I build the React Native app
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 1           | 1.0         | com.bugsnag.android.rnapp |

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "1"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.rnapp"
