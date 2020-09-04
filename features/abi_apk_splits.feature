Feature: Plugin integrated in project with ABI APK splits

@skip_agp4_1_or_higher
Scenario: ABI Splits project builds successfully
    When I build "abi_splits" using the "standard" bugsnag config
    And I wait to receive 16 requests

    Then the request is valid for the Build API
    And the payload field "appVersionCode" equals "2"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "3"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "4"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "5"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "6"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "1"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "7"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "8"
    And the payload field "appVersion" equals "1.0"
    And the payload field "apiKey" equals "TEST_API_KEY"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "2"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "3"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "4"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "5"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "6"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "1"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "7"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "8"
    And the field "apiKey" for multipart request equals "TEST_API_KEY"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.example"

@skip_agp4_1_or_higher
Scenario: ABI Splits automatic upload disabled
    When I build "abi_splits" using the "all_disabled" bugsnag config
    And I wait for 5 seconds
    Then I should receive no requests

@skip_agp4_1_or_higher
@skip_agp3_5
Scenario: ABI Splits manual upload of build API
    When I build the "Armeabi-release" variantOutput for "abi_splits" using the "all_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "apiKey" for multipart request equals "TEST_API_KEY"
    And the field "versionCode" for multipart request equals "3"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.example"
