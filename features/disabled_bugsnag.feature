Feature: Bugsnag disabled

Scenario: No requests are received when bugsnag is disabled
    When I build "default_app" using the "disabled_bugsnag" bugsnag config
    Then I should receive 0 requests
