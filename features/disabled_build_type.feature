Feature: Disabling plugin for build type

Scenario: Disabled build type makes no requests
    When I build "default_app" using the "disabled_build_type" bugsnag config
    And I wait for 3 seconds
    Then I wait to receive 1 builds
