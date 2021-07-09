Feature: Plugin integrated in project with Density APK splits and productFlavors

Scenario: Flavor Density Split project builds successfully
    When I build "flavor_apk_splits" using the "standard" bugsnag config
    And I wait to receive 6 builds
    And I wait to receive 6 uploads

    Then 6 builds are valid for the build API and match the following:
      | appVersionCode |
      | 1              |
      | 2              |
      | 3              |
      | 1              |
      | 2              |
      | 3              |

    And 6 uploads are valid for the android mapping API and match the following:
      | versionCode | appId                           |
      | 1           | com.bugsnag.android.example.bar |
      | 2           | com.bugsnag.android.example.bar |
      | 3           | com.bugsnag.android.example.bar |
      | 1           | com.bugsnag.android.example.foo |
      | 2           | com.bugsnag.android.example.foo |
      | 3           | com.bugsnag.android.example.foo |

Scenario: Flavor Density Split automatic upload disabled
    When I build "flavor_apk_splits" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds

Scenario: Flavor Density Split manual upload of build API
    When I build the "Bar-xxhdpi-release" variantOutput for "flavor_apk_splits" using the "all_disabled" bugsnag config
    And I wait to receive an upload
    Then the upload is valid for the Android Mapping API
    And the upload payload field "apiKey" equals "TEST_API_KEY"
    And the upload payload field "versionCode" equals "3"
    And the upload payload field "versionName" equals "1.0"
    And the upload payload field "appId" equals "com.bugsnag.android.example.bar"
