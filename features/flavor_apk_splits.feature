Feature: Plugin integrated in project with Density APK splits and productFlavors

@skip_agp4_1_or_higher
Scenario: Flavor Density Split project builds successfully
    When I build "flavor_apk_splits" using the "standard" bugsnag config
    Then I should receive 12 requests

    And the request 0 is valid for the Build API
    And the payload field "appVersionCode" equals "2" for request 0

    And the request 1 is valid for the Build API
    And the payload field "appVersionCode" equals "1" for request 1

    And the request 2 is valid for the Build API
    And the payload field "appVersionCode" equals "3" for request 2

    And the request 6 is valid for the Build API
    And the payload field "appVersionCode" equals "2" for request 6

    And the request 7 is valid for the Build API
    And the payload field "appVersionCode" equals "1" for request 7

    And the request 8 is valid for the Build API
    And the payload field "appVersionCode" equals "3" for request 8

    And the request 3 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 3 equals "2"
    And the field "appId" for multipart request 3 equals "com.bugsnag.android.example.bar"

    And the request 4 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 4 equals "1"
    And the field "appId" for multipart request 4 equals "com.bugsnag.android.example.bar"

    And the request 5 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 5 equals "3"
    And the field "appId" for multipart request 5 equals "com.bugsnag.android.example.bar"

    And the request 9 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 9 equals "2"
    And the field "appId" for multipart request 9 equals "com.bugsnag.android.example.foo"

    And the request 10 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 10 equals "1"
    And the field "appId" for multipart request 10 equals "com.bugsnag.android.example.foo"

    And the request 11 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 11 equals "3"
    And the field "appId" for multipart request 11 equals "com.bugsnag.android.example.foo"

@skip_agp4_1_or_higher
Scenario: Flavor Density Split automatic upload disabled
    When I build "flavor_apk_splits" using the "all_disabled" bugsnag config
    Then I should receive no requests

@skip_agp4_1_or_higher
Scenario: Flavor Density Split manual upload of build API
    When I build the "Bar-xxhdpi-release" variantOutput for "flavor_apk_splits" using the "all_disabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 0 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 0 equals "3"
    And the field "versionName" for multipart request 0 equals "1.0"
    And the field "appId" for multipart request 0 equals "com.bugsnag.android.example.bar"
