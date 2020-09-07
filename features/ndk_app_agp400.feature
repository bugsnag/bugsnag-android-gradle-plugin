Feature: Plugin integrated in NDK app

@requires_agp4_0_or_higher
Scenario: NDK apps send requests
    When I build the NDK app
    And I wait to receive 6 requests

    Then the request is valid for the Build API
    And the payload field "appVersion" equals "1.0"
    And the payload field "builderName" is not null
    And the payload field "buildTool" equals "gradle-android"
    And the payload field "appVersionCode" equals "1"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" is not null
    And the payload field "arch" equals "arm64-v8a"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" is not null
    And the payload field "arch" equals "armeabi-v7a"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" is not null
    And the payload field "arch" equals "x86"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" is not null
    And the payload field "arch" equals "x86_64"
    And I discard the oldest request

    And the request is valid for the Android Mapping API

@requires_agp4_0_or_higher
Scenario: Custom projectRoot is added to payload
    When I set environment variable "PROJECT_ROOT" to "/repos/custom/my-app"
    And I build the NDK app
    And I wait to receive 6 requests

    Then the request is valid for the Build API
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app"
    And I discard the oldest request

    And the request is valid for the Android Mapping API

# Sets a non-existent objdump location for x86 and arm64-v8a, delivery should proceed as normal for other files
@requires_agp4_0_or_higher
Scenario: Custom objdump location
    When I set environment variable "OBJDUMP_LOCATION" to "/fake/objdump"
    And I build the NDK app
    And I wait to receive 4 requests

    And the request is valid for the Build API
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "arch" equals "armeabi-v7a"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "arch" equals "x86_64"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
