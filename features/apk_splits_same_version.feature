Feature: Plugin integrated in project with APK splits

@skip_agp4_1_or_higher
Scenario: APK splits avoid uploading duplicate requests for same version information
    When I build "apk_splits" using the "standard" bugsnag config
    Then I should receive 2 requests
    And the request 1 is valid for the Android Mapping API
    And the payload field "buildUUID" equals "same-build-uuid" for request 1
    And the request 0 is valid for the Build API
