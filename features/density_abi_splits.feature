Feature: Plugin integrated in project with Density + ABI APK splits

Scenario: Density ABI Splits project builds successfully
    When I build "density_abi_splits" using the "standard" bugsnag config
    And I wait to receive 13 builds
    And I wait to receive 13 uploads

    Then 13 builds are valid for the build API and match the following:
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

    And 13 uploads are valid for the android mapping API and match the following:
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

Scenario: Density ABI Splits automatic upload disabled
    When I build "density_abi_splits" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds

Scenario: Density ABI Splits manual upload of build API
    When I build the "XxxhdpiArmeabi-release" variantOutput for "density_abi_splits" using the "all_disabled" bugsnag config
    And I wait to receive an upload
    Then the upload is valid for the Android Mapping API
    And the upload payload field "versionCode" equals "33"
