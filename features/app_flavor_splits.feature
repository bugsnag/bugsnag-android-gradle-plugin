Feature: Plugin integrated in project with APK splits and productFlavors

Scenario: Project builds successfully
    When I build the "appflavorsplits" module
    Then I should receive a request
