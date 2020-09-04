Feature: Plugin integrated in project with Density + ABI APK splits

@skip_agp4_1_or_higher
Scenario: Density ABI Splits project builds successfully
    When I build "density_abi_splits" using the "standard" bugsnag config
    And I wait to receive 26 requests

    Then the request is valid for the Build API
    And the payload field "appVersionCode" equals "21"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "31"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "11"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "41"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "51"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "22"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "32"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "42"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "52"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "23"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "33"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "43"
    And I discard the oldest request

    And the request is valid for the Build API
    And the payload field "appVersionCode" equals "53"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "21"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "31"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "11"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "41"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "51"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "22"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "32"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "42"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "52"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "23"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "33"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "43"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "53"

@skip_agp4_1_or_higher
Scenario: Density ABI Splits automatic upload disabled
    When I build "density_abi_splits" using the "all_disabled" bugsnag config
    And I wait for 5 seconds
    Then I should receive no requests

@skip_agp4_1_or_higher
@skip_agp3_5
Scenario: Density ABI Splits manual upload of build API
    When I build the "XxxhdpiArmeabi-release" variantOutput for "density_abi_splits" using the "all_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "33"
