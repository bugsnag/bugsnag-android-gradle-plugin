Feature: Bugsnag disabled

Scenario: No requests are received when bugsnag is disabled
    When I build "default_app" using the "disabled_bugsnag" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds
