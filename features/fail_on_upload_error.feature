Feature: Build stops when mapping file upload fails

Scenario: Upload successfully with API key, mapping file, and correct endpoint
    When I build "default_app" using the "standard" bugsnag config
    And I wait to receive 2 builds
    Then the exit code equals 0

Scenario: No uploads or build failures when obfuscation is disabled
    When I build "disabled_obfuscation" using the "standard" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds
    Then the exit code equals 0

Scenario: Upload failure due to empty API key
    When I build the failing "default_app" using the "empty_api_key" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds

Scenario: Upload failure due to connectivity failure
    When I build the failing "default_app" using the "wrong_endpoint" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds
