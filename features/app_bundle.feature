Feature: Generating Android app bundles

Scenario: Single-module default app bundles successfully
    When I bundle "default_app" using the "standard" bugsnag config
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
      | 1              | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       | overwrite |
      | 1           | 1.0         | com.bugsnag.android.example | null      |

Scenario: Bundling multiple flavors automatically
    When I bundle "flavors" using the "standard" bugsnag config
    And I wait to receive 4 requests

    Then 2 requests are valid for the build API and match the following:
      | appVersion | appVersionCode |
      | 1.0        | 1              |
      | 1.0        | 1              |

    And 2 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 1           | 1.0         | com.bugsnag.android.example.foo |
      | 1           | 1.0         | com.bugsnag.android.example.bar |

Scenario: Bundling single flavor
    When I bundle the "Foo" variantOutput for "flavors" using the "standard" bugsnag config
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersion | appVersionCode |
      | 1.0        | 1              |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 1           | 1.0         | com.bugsnag.android.example.foo |

Scenario: Auto upload disabled
    When I bundle "default_app" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no requests
