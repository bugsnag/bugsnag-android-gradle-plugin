Feature: Plugin integrated in project with APK splits and productFlavors

Scenario: Project builds successfully
    When I build "flavor_apk_splits" using the "standard" bugsnag config
    Then I should receive a request
