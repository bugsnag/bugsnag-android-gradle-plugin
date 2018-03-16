Feature: Plugin integrated in default app

Scenario: Project builds successfully
    When I build "default_app" using the "standard" bugsnag config
    Then I should receive a request
