Feature: Plugin integrated in project with Density APK splits

@skip_agp4_1_or_higher
Scenario: Density Splits project builds successfully
    When I build "density_splits" using the "standard" bugsnag config
    And I wait to receive 14 requests

    Then 7 requests are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 1              | 1.0        |
      | 2              | 1.0        |
      | 3              | 1.0        |
      | 4              | 1.0        |
      | 5              | 1.0        |
      | 6              | 1.0        |
      | 7              | 1.0        |

    And 7 requests are valid for the android mapping API and match the following:
      | versionCode | versionName |
      | 1           | 1.0         |
      | 2           | 1.0         |
      | 3           | 1.0         |
      | 4           | 1.0         |
      | 5           | 1.0         |
      | 6           | 1.0         |
      | 7           | 1.0         |

@skip_agp4_1_or_higher
Scenario: Density Splits automatic upload disabled
    When I build "density_splits" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no requests

@skip_agp4_1_or_higher
@skip_agp3_5
Scenario: Density Splits manual upload of build API
    When I build the "Hdpi-release" variantOutput for "density_splits" using the "all_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "apiKey" for multipart request equals "TEST_API_KEY"
    And the field "versionCode" for multipart request equals "4"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.example"
