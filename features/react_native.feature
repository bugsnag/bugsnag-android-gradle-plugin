Feature: Plugin integrated in React Native app

Scenario: React Native sends requests
    When I build the React Native app
    Then I should receive 2 requests

    And the request 0 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 0
    And the payload field "apiKey" equals "YOUR-API-KEY-HERE" for request 0
    And the payload field "builderName" is not null for request 0
    And the payload field "buildTool" equals "gradle-android" for request 0
    And the payload field "appVersionCode" equals "1" for request 0

    And the request 1 is valid for the Android Mapping API
    And the part "apiKey" for request 1 equals "YOUR-API-KEY-HERE"
    And the part "versionCode" for request 1 equals "1"
    And the part "versionName" for request 1 equals "1.0"
    And the part "appId" for request 1 equals "com.bugsnag.android.rnapp"
