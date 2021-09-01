Feature: Manifest meta-data overrides

# ApkVariantOutput level manifest processing for AGP < 4.1.0 means we can't do these tests
@requires_agp4_1_or_higher
Scenario: Invalid Split Overrides
    When I build the failing "splits_and_manual_version" using the "standard" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds

Scenario: Manual versionCode overrides default
    When I build "manual_version" using the "standard" bugsnag config
    And I wait to receive a build
    And I wait to receive an upload

    Then 1 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
        | 101            | 3.5        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |
