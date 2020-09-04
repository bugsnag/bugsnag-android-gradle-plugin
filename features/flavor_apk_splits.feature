Feature: Plugin integrated in project with Density APK splits and productFlavors

@skip_agp4_1_or_higher
Scenario: Flavor Density Split project builds successfully
    When I build "flavor_apk_splits" using the "standard" bugsnag config
    And I wait to receive 12 requests

    Then the request is valid for the Build API
    And the payload field "appVersionCode" equals "2"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "1"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "3"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "2"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.bar"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "1"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.bar"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "3"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.bar"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "2"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "1"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "3"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "2"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.foo"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "1"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.foo"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "3"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.foo"

@skip_agp4_1_or_higher
Scenario: Flavor Density Split automatic upload disabled
    When I build "flavor_apk_splits" using the "all_disabled" bugsnag config
    And I wait for 5 seconds
    Then I should receive no requests

@skip_agp4_1_or_higher
@skip_agp3_5
Scenario: Flavor Density Split manual upload of build API
    When I build the "Bar-xxhdpi-release" variantOutput for "flavor_apk_splits" using the "all_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "apiKey" for multipart request equals "TEST_API_KEY"
    And the field "versionCode" for multipart request equals "3"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.bar"
