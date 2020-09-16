Feature: Plugin integrated in project with Density + ABI APK splits

@skip_agp4_1_or_higher
Scenario: Density ABI Splits project builds successfully
    When I build "density_abi_splits" using the "standard" bugsnag config
    And I wait to receive 26 requests

    Then 13 requests are valid for the build API and match the following:
      | appVersionCode |
      | 11             |
      | 21             |
      | 31             |
      | 41             |
      | 51             |
      | 22             |
      | 32             |
      | 42             |
      | 52             |
      | 23             |
      | 33             |
      | 43             |
      | 53             |

    And 13 requests are valid for the android mapping API and match the following:
      | versionCode |
      | 11          |
      | 21          |
      | 31          |
      | 41          |
      | 51          |
      | 22          |
      | 32          |
      | 42          |
      | 52          |
      | 23          |
      | 33          |
      | 43          |
      | 53          |

@skip_agp4_1_or_higher
Scenario: Density ABI Splits automatic upload disabled
    When I build "density_abi_splits" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no requests

@skip_agp4_1_or_higher
Scenario: Density ABI Splits manual upload of build API
    When I build the "XxxhdpiArmeabi-release" variantOutput for "density_abi_splits" using the "all_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "versionCode" for multipart request equals "33"
