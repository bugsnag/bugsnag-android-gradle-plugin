Feature: Disabling plugin for product flavors

Scenario: Disabled product flavor makes no requests
    When I build "flavors" using the "disabled_product_flavor" bugsnag config
    Then I should receive 2 requests

    And the request 0 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 0 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 0 equals "1"
    And the field "versionName" for multipart request 0 equals "1.0"
    And the field "appId" for multipart request 0 equals "com.bugsnag.android.example.bar"

    And the request 1 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 1
    And the payload field "apiKey" equals "TEST_API_KEY" for request 1
    And the payload field "appVersionCode" equals "1" for request 1
