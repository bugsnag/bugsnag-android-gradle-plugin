Feature: Reporting Unhandled Exceptions

Scenario: Test Unhandled Kotlin Exception without Session
    When I build the "appvanilla" module
    Then I should receive a request
