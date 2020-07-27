Feature: Plugin integrated in project with APK splits

@skip_agp4_1_or_higher
Scenario: APK splits avoid uploading duplicate requests for same version information
    When I build "apk_splits" using the "auto_update_build_uuid" bugsnag config
    Then I should receive 2 requests

    And the request 0 is valid for the Android Mapping API
    And the request 1 is valid for the Build API
