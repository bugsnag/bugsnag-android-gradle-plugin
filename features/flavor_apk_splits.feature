Feature: Plugin integrated in project with Density APK splits and productFlavors

Scenario: Flavor Density Split project builds successfully
    When I build "flavor_apk_splits" using the "standard" bugsnag config
    Then I should receive 12 requests

    And the request 0 is valid for the Build API
    And the payload field "appVersionCode" equals "2" for request 0

    And the request 1 is valid for the Build API
    And the payload field "appVersionCode" equals "1" for request 1

    And the request 2 is valid for the Build API
    And the payload field "appVersionCode" equals "3" for request 2

    And the request 3 is valid for the Build API
    And the payload field "appVersionCode" equals "2" for request 3

    And the request 4 is valid for the Build API
    And the payload field "appVersionCode" equals "1" for request 4

    And the request 5 is valid for the Build API
    And the payload field "appVersionCode" equals "3" for request 5

    And the request 6 is valid for the Android Mapping API
    And the part "versionCode" for request 6 equals "2"
    And the part "appId" for request 6 equals "com.bugsnag.android.example.bar"

    And the request 7 is valid for the Android Mapping API
    And the part "versionCode" for request 7 equals "1"
    And the part "appId" for request 7 equals "com.bugsnag.android.example.bar"

    And the request 8 is valid for the Android Mapping API
    And the part "versionCode" for request 8 equals "3"
    And the part "appId" for request 8 equals "com.bugsnag.android.example.bar"

    And the request 9 is valid for the Android Mapping API
    And the part "versionCode" for request 9 equals "2"
    And the part "appId" for request 9 equals "com.bugsnag.android.example.foo"

    And the request 10 is valid for the Android Mapping API
    And the part "versionCode" for request 10 equals "1"
    And the part "appId" for request 10 equals "com.bugsnag.android.example.foo"

    And the request 11 is valid for the Android Mapping API
    And the part "versionCode" for request 11 equals "3"
    And the part "appId" for request 11 equals "com.bugsnag.android.example.foo"

Scenario: Flavor Density Split automatic upload disabled
    When I build "flavor_apk_splits" using the "all_disabled" bugsnag config
    Then I should receive no requests

Scenario: Flavor Density Split manual upload of build API
    When I build the "Bar-xxhdpi-release" variantOutput for "flavor_apk_splits" using the "all_disabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API
    And the part "apiKey" for request 0 equals "TEST_API_KEY"
    And the part "versionCode" for request 0 equals "3"
    And the part "versionName" for request 0 equals "1.0"
    And the part "appId" for request 0 equals "com.bugsnag.android.example.bar"
