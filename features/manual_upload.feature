Feature: Manual invocation of upload task

Scenario: Manually invoking upload task sends requests
    When I build "default_app" using the "all_disabled" bugsnag config
    And I run the script "features/scripts/manual_upload.sh" synchronously
    And I wait to receive 2 requests
    And the request is valid for the Android Mapping API
    And I discard the oldest request
    And the request is valid for the Build API
