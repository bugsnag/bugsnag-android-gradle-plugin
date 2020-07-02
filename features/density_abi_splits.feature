Feature: Plugin integrated in project with Density + ABI APK splits

Scenario: Density ABI Splits project builds successfully
    When I build "density_abi_splits" using the "standard" bugsnag config
    Then I should receive 26 requests

    And the request 25 is valid for the Build API
    And the payload field "appVersionCode" equals "21" for request 25

    And the request 24 is valid for the Build API
    And the payload field "appVersionCode" equals "31" for request 24

    And the request 23 is valid for the Build API
    And the payload field "appVersionCode" equals "11" for request 23

    And the request 22 is valid for the Build API
    And the payload field "appVersionCode" equals "41" for request 22

    And the request 21 is valid for the Build API
    And the payload field "appVersionCode" equals "51" for request 21

    And the request 20 is valid for the Build API
    And the payload field "appVersionCode" equals "22" for request 20

    And the request 19 is valid for the Build API
    And the payload field "appVersionCode" equals "32" for request 19

    And the request 18 is valid for the Build API
    And the payload field "appVersionCode" equals "42" for request 18

    And the request 17 is valid for the Build API
    And the payload field "appVersionCode" equals "52" for request 17

    And the request 16 is valid for the Build API
    And the payload field "appVersionCode" equals "23" for request 16

    And the request 15 is valid for the Build API
    And the payload field "appVersionCode" equals "33" for request 15

    And the request 14 is valid for the Build API
    And the payload field "appVersionCode" equals "43" for request 14

    And the request 13 is valid for the Build API
    And the payload field "appVersionCode" equals "53" for request 13

    And the request 12 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 12 equals "21"

    And the request 11 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 11 equals "31"

    And the request 10 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 10 equals "11"

    And the request 9 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 9 equals "41"

    And the request 8 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 8 equals "51"

    And the request 7 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 7 equals "22"

    And the request 6 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 6 equals "32"

    And the request 5 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 5 equals "42"

    And the request 4 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 4 equals "52"

    And the request 3 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 3 equals "23"

    And the request 2 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 2 equals "33"

    And the request 1 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 1 equals "43"

    And the request 0 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 0 equals "53"

Scenario: Density ABI Splits automatic upload disabled
    When I build "density_abi_splits" using the "all_disabled" bugsnag config
    Then I should receive no requests

Scenario: Density ABI Splits manual upload of build API
    When I build the "XxxhdpiArmeabi-release" variantOutput for "density_abi_splits" using the "all_disabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API
    And the field "versionCode" for multipart request 0 equals "33"
