# Broken in AGP 3.3.0, tracked by PLAT-3854
#
#Feature: Plugin integrated in default Android Library
#
#Scenario: Single-module default library builds successfully
#    When I build "default_lib" using the "standard" bugsnag config
#    And I wait to receive 2 requests
#
#    And the request is valid for the Build API
#    And the payload field "appVersion" equals "1.0"
#    And the payload field "apiKey" equals "TEST_API_KEY"
#    And the payload field "builderName" is not null
#    And the payload field "buildTool" equals "gradle-android"
#    And the payload field "appVersionCode" equals "1"
#    And the payload field "sourceControl.provider" equals "github"
#    And the payload field "sourceControl.repository" equals "https://github.com/bugsnag/bugsnag-android-gradle-plugin.git"
#    And the payload field "sourceControl.revision" is not null
#
#    And the payload field "metadata.os_arch" is not null
#    And the payload field "metadata.os_name" is not null
#    And the payload field "metadata.os_version" is not null
#    And the payload field "metadata.java_version" is not null
#    And the payload field "metadata.gradle_version" is not null
#    And the payload field "metadata.git_version" is not null
#    And I discard the oldest request
#
#    And the request is valid for the Android Mapping API
#    And the field "apiKey" for multipart request equals "TEST_API_KEY"
#    And the field "versionCode" for multipart request equals "1"
#    And the field "versionName" for multipart request equals "1.0"
#    And the field "appId" for multipart request equals "com.bugsnag.libvanilla"
