Feature: Plugin integrated in project with productFlavors

Scenario: Flavors automatic upload on build
    When I build "flavors" using the "standard" bugsnag config
    Then I should receive 4 requests

    And the request 3 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 3
    And the payload field "apiKey" equals "TEST_API_KEY" for request 3
    And the payload field "appVersionCode" equals "1" for request 3

    And the request 2 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 2
    And the payload field "apiKey" equals "TEST_API_KEY" for request 2
    And the payload field "appVersionCode" equals "1" for request 2

    And the request 1 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 1 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 1 equals "1"
    And the field "versionName" for multipart request 1 equals "1.0"
    And the field "appId" for multipart request 1 equals "com.bugsnag.android.example.bar"

    And the request 0 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 0 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 0 equals "1"
    And the field "versionName" for multipart request 0 equals "1.0"
    And the field "appId" for multipart request 0 equals "com.bugsnag.android.example.foo"

Scenario: Flavors automatic upload disabled
    When I build "flavors" using the "all_disabled" bugsnag config
    Then I should receive no requests

Scenario: Flavors manual upload of build API
    When I build the "Foo-release" variantOutput for "flavors" using the "all_disabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 0 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 0 equals "1"
    And the field "versionName" for multipart request 0 equals "1.0"
    And the field "appId" for multipart request 0 equals "com.bugsnag.android.example.foo"
