Feature: Plugin integrated in default Android library

Scenario: Project builds successfully
    When I build the "libvanilla" module
    Then I should receive a request
