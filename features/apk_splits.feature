Feature: Plugin integrated in project with APK splits

Scenario: Project builds successfully
    When I build "apk_splits" using the "standard" bugsnag config
    Then I should receive a request
