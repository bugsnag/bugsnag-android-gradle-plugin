Feature: Plugin integrated in project with APK splits

Scenario: Project builds successfully
    When I build the "appsplits" module
    Then I should receive a request
