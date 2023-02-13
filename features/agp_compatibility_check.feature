Feature: AGP7 Emits Compatibility Error

Scenario: Compatibility Error Emitted when AGP 4.2.0 is used
    When I build the failing "default_app" on AGP "4.2.0" using the "standard" bugsnag config
    And I wait for 3 seconds
    Then I should receive no requests
