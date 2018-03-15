Feature: Plugin integrated in project with APK ABI splits

Scenario: Project builds successfully
    When I build the "appabisplits" module
    Then I should receive a request
