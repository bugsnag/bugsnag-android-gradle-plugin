Feature: Plugin integrated in project with productFlavors

Scenario: Project builds successfully
    When I build "flavors" using the "standard" bugsnag config
    Then I should receive a request
