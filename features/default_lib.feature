Feature: Plugin integrated in default Android Library

Scenario: Project builds successfully
    When I build "default_lib" using the "standard" bugsnag config
    Then I should receive a request
