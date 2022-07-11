Feature: Manifest meta-data overrides

Scenario: Manual versionCode overrides default
    When I build "manual_version" using the "standard" bugsnag config
    And I wait to receive 2 builds

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
        | 101            | 3.5        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |
