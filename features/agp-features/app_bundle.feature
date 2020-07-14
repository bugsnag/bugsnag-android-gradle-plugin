Feature: Generating Android app bundles

Scenario: Single-module default app bundles successfully
    When I bundle "default_app" using the "standard" bugsnag config
    Then I should receive 2 requests

    And the request 0 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 0
    And the payload field "apiKey" equals "TEST_API_KEY" for request 0
    And the payload field "builderName" is not null for request 0
    And the payload field "buildTool" equals "gradle-android" for request 0
    And the payload field "appVersionCode" equals "1" for request 0
    And the payload field "sourceControl.provider" equals "github" for request 0
    And the payload field "sourceControl.repository" equals "https://github.com/bugsnag/bugsnag-android-gradle-plugin.git" for request 0
    And the payload field "sourceControl.revision" is not null for request 0

    And the payload field "metadata.os_arch" is not null for request 0
    And the payload field "metadata.os_name" is not null for request 0
    And the payload field "metadata.os_version" is not null for request 0
    And the payload field "metadata.java_version" is not null for request 0
    And the payload field "metadata.gradle_version" is not null for request 0
    And the payload field "metadata.git_version" is not null for request 0

    And the request 1 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 1 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 1 equals "1"
    And the field "versionName" for multipart request 1 equals "1.0"
    And the field "appId" for multipart request 1 equals "com.bugsnag.android.example"
    And the field "overwrite" for multipart request 1 is null

Scenario: Bundling multiple flavors automatically
    When I bundle "flavors" using the "standard" bugsnag config
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

Scenario: Bundling single flavor
    When I bundle the "Foo" variantOutput for "flavors" using the "standard" bugsnag config
    Then I should receive 2 requests

    And the request 1 is valid for the Build API

    And the request 0 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 0 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 0 equals "1"
    And the field "versionName" for multipart request 0 equals "1.0"
    And the field "appId" for multipart request 0 equals "com.bugsnag.android.example.foo"

Scenario: Auto upload disabled
    When I bundle "default_app" using the "all_disabled" bugsnag config
    Then I should receive no requests
