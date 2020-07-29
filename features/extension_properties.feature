Feature: Extension properties control plugin behaviour

Scenario: Disable autoReportBuilds
    When I build "default_app" using the "auto_report_builds_disabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API

Scenario: Enable debug mapping upload
    When I build "debug_proguard" using the "upload_debug_enabled" bugsnag config
    Then I should receive 4 requests
    And the request 0 is valid for the Android Mapping API
    And the request 1 is valid for the Android Mapping API
    And the request 2 is valid for the Build API
    And the request 3 is valid for the Build API

Scenario: Enable overwrite
    When I build "default_app" using the "overwrite_enabled" bugsnag config
    Then I should receive 1 request
    And the request 0 is valid for the Android Mapping API
    And the field "overwrite" for multipart request 0 equals "true"

Scenario: Alter build API values
    When I build "default_app" using the "custom_build_info" bugsnag config
    Then I should receive 1 request
    And the payload field "builderName" equals "Mark Twain" for request 0
    And the payload field "buildTool" equals "gradle-android" for request 0
    And the payload field "sourceControl.provider" equals "bitbucket" for request 0
    And the payload field "sourceControl.repository" equals "https://example.com/bar/foo.git" for request 0
    And the payload field "sourceControl.revision" equals "fab8721" for request 0
    And the payload field "metadata.MyKey" equals "MyValue"
    And the payload field "metadata.os_version" equals "BeOS"
