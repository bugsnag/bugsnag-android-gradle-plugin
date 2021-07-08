Feature: Disabling plugin for product flavors

Scenario: Disabled product flavor makes no requests
    When I build "flavors" using the "disabled_product_flavor" bugsnag config
    And I wait to receive a build
    And I wait to receive an upload

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 1              | 1.0        |

    And 1 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 1           | 1.0         | com.bugsnag.android.example.bar |
