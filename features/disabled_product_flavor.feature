Feature: Disabling plugin for product flavors

Scenario: Disabled product flavor makes no requests
    When I build "flavors" using the "disabled_product_flavor" bugsnag config
    And I wait to receive 2 requests

    Then the request is valid for the Build API
    And the payload field "appVersion" equals "1.0"
    And the payload field "apiKey" equals "TEST_API_KEY"
    And the payload field "appVersionCode" equals "1"
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the field "apiKey" for multipart request equals "TEST_API_KEY"
    And the field "versionCode" for multipart request equals "1"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.example.bar"
