Feature: Version code override

Scenario: Setting versionCodeOverride is respected
    When I build "version_code_override" using the "standard" bugsnag config
    And I wait to receive 4 requests

    Then 2 requests are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 9000              | 1.0        |
      | 9000              | 1.0        |

    And 2 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 9000           | 1.0         | com.bugsnag.android.example.bar |
      | 9000           | 1.0         | com.bugsnag.android.example.foo |

@agp_4_1_only
Scenario: Setting onVariants.outputs.versionCode is respected
    When I build "on_variants_version_code" using the "standard" bugsnag config
    And I wait to receive 4 requests

    Then 2 requests are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 9000              | 1.0        |
      | 9000              | 1.0        |

    And 2 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 9000           | 1.0         | com.bugsnag.android.example.bar |
      | 9000           | 1.0         | com.bugsnag.android.example.foo |
