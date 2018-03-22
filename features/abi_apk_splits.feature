Feature: Plugin integrated in project with Density APK splits

Scenario: ABI Splits project builds successfully
    When I build "abi_splits" using the "standard" bugsnag config
    Then I should receive 16 requests

    And the request 0 is valid for the Build API
    And the payload field "appVersionCode" equals "2" for request 0

    And the request 1 is valid for the Build API
    And the payload field "appVersionCode" equals "3" for request 1

    And the request 2 is valid for the Build API
    And the payload field "appVersionCode" equals "4" for request 2

    And the request 3 is valid for the Build API
    And the payload field "appVersionCode" equals "5" for request 3

    And the request 4 is valid for the Build API
    And the payload field "appVersionCode" equals "6" for request 4

    And the request 5 is valid for the Build API
    And the payload field "appVersionCode" equals "1" for request 5

    And the request 6 is valid for the Build API
    And the payload field "appVersionCode" equals "7" for request 6

    And the request 7 is valid for the Build API
    And the payload field "appVersionCode" equals "8" for request 7
    And the payload field "appVersion" equals "1.0" for request 6
    And the payload field "apiKey" equals "TEST_API_KEY" for request 6

    And the request 8 is valid for the Android Mapping API
    And the part "versionCode" for request 8 equals "2"

    And the request 9 is valid for the Android Mapping API
    And the part "versionCode" for request 9 equals "3"

    And the request 10 is valid for the Android Mapping API
    And the part "versionCode" for request 10 equals "4"

    And the request 11 is valid for the Android Mapping API
    And the part "versionCode" for request 11 equals "5"

    And the request 12 is valid for the Android Mapping API
    And the part "versionCode" for request 12 equals "6"

    And the request 13 is valid for the Android Mapping API
    And the part "versionCode" for request 13 equals "1"

    And the request 14 is valid for the Android Mapping API
    And the part "versionCode" for request 14 equals "7"

    And the request 15 is valid for the Android Mapping API
    And the part "versionCode" for request 15 equals "8"
    And the part "apiKey" for request 15 equals "TEST_API_KEY"
    And the part "versionName" for request 15 equals "1.0"
    And the part "appId" for request 15 equals "com.bugsnag.android.example"

Scenario: ABI Splits automatic upload disabled
    When I build "abi_splits" using the "all_disabled" bugsnag config
    Then I should receive no requests

Scenario: ABI Splits manual upload of build API
    When I build the "Armeabi-release" variantOutput for "abi_splits" using the "all_disabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API
    And the part "apiKey" for request 0 equals "TEST_API_KEY"
    And the part "versionCode" for request 0 equals "3"
    And the part "versionName" for request 0 equals "1.0"
    And the part "appId" for request 0 equals "com.bugsnag.android.example"
