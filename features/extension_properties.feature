Feature: Extension properties control plugin behaviour

Scenario: Disable autoReportBuilds
    When I build "default_app" using the "auto_report_builds_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API

Scenario: Enable debug mapping upload
    When I build "debug_proguard" using the "upload_debug_enabled" bugsnag config
    And I wait to receive 4 requests
    Then the request is valid for the Build API
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And I discard the oldest request

    And the request is valid for the Build API
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And I discard the oldest request

Scenario: Enable overwrite
    When I build "default_app" using the "overwrite_enabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "overwrite" for multipart request equals "true"

Scenario: Alter build API values
    When I build "default_app" using the "custom_build_info" bugsnag config
    And I wait to receive a request
    Then the payload field "builderName" equals "Mark Twain"
    And the payload field "buildTool" equals "gradle-android"
    And the payload field "sourceControl.provider" equals "bitbucket"
    And the payload field "sourceControl.repository" equals "https://example.com/bar/foo.git"
    And the payload field "sourceControl.revision" equals "fab8721"
    And the payload field "metadata.MyKey" equals "MyValue"
    And the payload field "metadata.os_version" equals "BeOS"
