Feature: Manual invocation of upload task

Scenario: Manually invoking upload task sends requests
    When I build "default_app" using the "all_disabled" bugsnag config
    When I run the script "features/scripts/manual_upload.sh" synchronously
    Then I should receive 2 requests
    And the request 0 is valid for the Android Mapping API
    And the request 1 is valid for the Build API
