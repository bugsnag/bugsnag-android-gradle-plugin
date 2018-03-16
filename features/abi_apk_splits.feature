Feature: Plugin integrated in project with APK ABI splits

Scenario: Project builds successfully
    When I build "abi_apk_splits" using the "standard" bugsnag config
    Then I should receive a request
