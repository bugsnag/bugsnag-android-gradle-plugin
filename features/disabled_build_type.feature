Feature: Disabling plugin for build type

Scenario: Disabled build type makes no requests
    When I build "default_app" using the "disabled_build_type" bugsnag config
    Then I should receive 0 requests
