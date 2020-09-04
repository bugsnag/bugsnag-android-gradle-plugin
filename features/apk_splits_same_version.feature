Feature: Plugin integrated in project with APK splits

@skip_agp4_1_or_higher
Scenario: APK splits avoid uploading duplicate requests for same version information
    When I build "apk_splits" using the "standard" bugsnag config
    And I wait to receive 2 requests
    Then the request is valid for the Build API
    And I discard the oldest request

    And the request is valid for the Android Mapping API
    And the payload field "buildUUID" equals "same-build-uuid"
