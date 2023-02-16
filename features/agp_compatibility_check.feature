Feature: AGP7 Emits Compatibility Error

Scenario: Compatibility Error Emitted when AGP 7.4.1 is used
    When I build the failing "default_app" on AGP "7.4.1" using the "standard" bugsnag config
    And I wait for 3 seconds
    Then I should receive no requests
