Feature: Plugin integrated in project with APK splits

Scenario: APK splits avoid uploading duplicate requests for same version information
    When I build "apk_splits" using the "standard" bugsnag config
    And I wait to receive 2 builds

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 1              | 1.0        |

    And 1 requests are valid for the android mapping API and match the following:
      | buildUUID                 |
      | same-build-uuid           |
