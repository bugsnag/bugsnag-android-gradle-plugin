Feature: Plugin integrated in default app

Scenario: Single-module default app builds successfully
    When I build "default_app" using the "standard" bugsnag config
    Then I should receive 2 requests

    And the request 1 is valid for the Build API
    And the payload field "appVersion" equals "1.0" for request 1
    And the payload field "apiKey" equals "TEST_API_KEY" for request 1
    And the payload field "builderName" is not null for request 1
    And the payload field "buildTool" equals "gradle-android" for request 1
    And the payload field "appVersionCode" equals "1" for request 1
    And the payload field "sourceControl.provider" equals "github" for request 1
    And the payload field "sourceControl.repository" equals "https://github.com/bugsnag/bugsnag-android-gradle-plugin.git" for request 1
    And the payload field "sourceControl.revision" is not null for request 1

    And the payload field "metadata.os_arch" is not null for request 1
    And the payload field "metadata.os_name" is not null for request 1
    And the payload field "metadata.os_version" is not null for request 1
    And the payload field "metadata.java_version" is not null for request 1
    And the payload field "metadata.gradle_version" is not null for request 1
    And the payload field "metadata.git_version" is not null for request 1

    And the request 0 is valid for the Android Mapping API
    And the field "apiKey" for multipart request 0 equals "TEST_API_KEY"
    And the field "versionCode" for multipart request 0 equals "1"
    And the field "versionName" for multipart request 0 equals "1.0"
    And the field "appId" for multipart request 0 equals "com.bugsnag.android.example"
    And the field "overwrite" for multipart request 0 is null
