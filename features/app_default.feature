Feature: Plugin integrated in default app

Scenario: Project builds successfully
    When I build the "appvanilla" module
    Then I should receive a request
