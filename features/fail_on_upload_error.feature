Feature: Build stops when mapping file upload fails

Scenario: Upload successfully with API key, mapping file, and correct endpoint
    When I build "default_app" using the "standard" bugsnag config
    And I wait to receive a build
    And I wait to receive an upload

Scenario: No build failures when obfuscation is disabled
    When I build "disabled_obfuscation" using the "standard" bugsnag config
    And I wait for 3 seconds
    Then I wait to receive a build
    Then I should receive no uploads

Scenario: Upload failure due to empty API key
    When I build the failing "default_app" using the "empty_api_key" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds

Scenario: Upload failure due to connectivity failure
    When I build the failing "default_app" using the "wrong_endpoint" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds
