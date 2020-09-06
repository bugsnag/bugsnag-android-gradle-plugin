Feature: Plugin integrated in React Native app

Scenario: React Native sends requests
    When I build the React Native app
    And I wait to receive 2 requests

    Then the request is valid for the Build API
    And the payload field "appVersion" equals "1.0"
    And the payload field "builderName" is not null
    And the payload field "buildTool" equals "gradle-android"
    And the payload field "appVersionCode" equals "1"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "1"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.rnapp"
