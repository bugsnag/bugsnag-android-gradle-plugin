Feature: Plugin integrated in project with productFlavors

Scenario: Project builds successfully
    When I build the "appflavors" module
    Then I should receive a request
