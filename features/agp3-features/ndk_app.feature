Feature: Plugin integrated in NDK app

Scenario: NDK apps send requests
    When I build the NDK app
    Then I should receive 6 requests

    And the request 5 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 5
    And the payload field "apiKey" equals "your-api-key-here" for request 5
    And the payload field "builderName" is not null for request 5
    And the payload field "buildTool" equals "gradle-android" for request 5
    And the payload field "appVersionCode" equals "1" for request 5

    And the request 1 is valid for the Android NDK Mapping API
    And the payload field "apiKey" equals "your-api-key-here" for request 1
    And the payload field "projectRoot" is not null for request 1
    And the payload field "arch" equals "arm64-v8a" for request 1

    And the request 2 is valid for the Android NDK Mapping API
    And the payload field "apiKey" equals "your-api-key-here" for request 2
    And the payload field "projectRoot" is not null for request 2
    And the payload field "arch" equals "armeabi-v7a" for request 2

    And the request 3 is valid for the Android NDK Mapping API
    And the payload field "apiKey" equals "your-api-key-here" for request 3
    And the payload field "projectRoot" is not null for request 3
    And the payload field "arch" equals "x86" for request 3

    And the request 4 is valid for the Android NDK Mapping API
    And the payload field "apiKey" equals "your-api-key-here" for request 4
    And the payload field "projectRoot" is not null for request 4
    And the payload field "arch" equals "x86_64" for request 4

    And the request 0 is valid for the Android Mapping API
    And the payload field "apiKey" equals "your-api-key-here" for request 0

Scenario: Custom projectRoot is added to payload
    When I set environment variable "PROJECT_ROOT" to "/repos/custom/my-app"
    When I build the NDK app
    Then I should receive 6 requests

    And the request 0 is valid for the Android Mapping API

    And the request 1 is valid for the Android NDK Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app" for request 1

    And the request 2 is valid for the Android NDK Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app" for request 2

    And the request 3 is valid for the Android NDK Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app" for request 3

    And the request 4 is valid for the Android NDK Mapping API
    And the payload field "projectRoot" equals "/repos/custom/my-app" for request 4

    And the request 5 is valid for the Build API

# Sets a non-existent objdump location for x86 and arm64-v8a, delivery should proceed as normal for other files
Scenario: Custom objdump location
    When I set environment variable "OBJDUMP_LOCATION" to "/fake/objdump"
    When I build the NDK app
    Then I should receive 4 requests

    And the request 0 is valid for the Android Mapping API

    And the request 1 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "armeabi-v7a" for request 1

    And the request 2 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "x86_64" for request 2

    And the request 3 is valid for the Build API

Scenario: NDK app only uploads SO file matching architecture for ABI splits
    When I set environment variable "ABI_SPLITS" to "enabled"
    When I build the NDK app
    Then I should receive 18 requests

    And the request 0 is valid for the Android Mapping API
    And the payload field "versionCode" equals "5" for request 0

    And the request 1 is valid for the Android Mapping API
    And the payload field "versionCode" equals "4" for request 1

    And the request 2 is valid for the Android Mapping API
    And the payload field "versionCode" equals "1" for request 2

    And the request 3 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "x86_64" for request 3

    And the request 4 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "x86" for request 4

    And the request 5 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "arm64-v8a" for request 5

    And the request 6 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "armeabi-v7a" for request 6

    And the request 7 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "x86" for request 7

    And the request 8 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "x86_64" for request 8

    And the request 9 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "armeabi-v7a" for request 9

    And the request 10 is valid for the Android NDK Mapping API
    And the payload field "arch" equals "arm64-v8a" for request 10

    And the request 11 is valid for the Android Mapping API
    And the payload field "versionCode" equals "3" for request 11

    And the request 12 is valid for the Android Mapping API
    And the payload field "versionCode" equals "2" for request 12

    And the request 13 is valid for the Build API
    And the payload field "appVersionCode" equals "5" for request 13

    And the request 14 is valid for the Build API
    And the payload field "appVersionCode" equals "4" for request 14

    And the request 15 is valid for the Build API
    And the payload field "appVersionCode" equals "1" for request 15

    And the request 16 is valid for the Build API
    And the payload field "appVersionCode" equals "3" for request 16

    And the request 17 is valid for the Build API
    And the payload field "appVersionCode" equals "2" for request 17
