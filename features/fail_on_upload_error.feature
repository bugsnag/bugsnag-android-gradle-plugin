Feature: Build stops when mapping file upload fails

Scenario: Upload successfully with API key, mapping file, and correct endpoint
    When I build "default_app" using the "standard" bugsnag config
    Then I should receive 2 requests
    And the exit code equals 0

Scenario: No uploads or build failures when obfuscation is disabled
    When I build "disabled_obfuscation" using the "standard" bugsnag config
    Then I should receive 0 requests
    And the exit code equals 0

Scenario: Upload failure due to empty API key
    When I build the failing "default_app" using the "empty_api_key" bugsnag config
    Then I should receive 0 requests
    And the exit code equals 1

Scenario: Upload failure due to connectivity failure
    When I build the failing "default_app" using the "wrong_endpoint" bugsnag config
    Then I should receive 0 requests
    And the exit code equals 1
