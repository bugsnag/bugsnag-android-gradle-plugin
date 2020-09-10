Feature: Plugin integrated in project with Density APK splits and productFlavors

@skip_agp4_1_or_higher
Scenario: Flavor Density Split project builds successfully
    When I build "flavor_apk_splits" using the "standard" bugsnag config
    And I wait to receive 12 requests

    Then 6 requests are valid for the build API and match the following:
      | appVersionCode |
      | 1              |
      | 2              |
      | 3              |
      | 1              |
      | 2              |
      | 3              |

    And 6 requests are valid for the android mapping API and match the following:
      | versionCode | appId                           |
      | 1           | com.bugsnag.android.example.bar |
      | 2           | com.bugsnag.android.example.bar |
      | 3           | com.bugsnag.android.example.bar |
      | 1           | com.bugsnag.android.example.foo |
      | 2           | com.bugsnag.android.example.foo |
      | 3           | com.bugsnag.android.example.foo |

@skip_agp4_1_or_higher
Scenario: Flavor Density Split automatic upload disabled
    When I build "flavor_apk_splits" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
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
