Feature: Generating Android app bundles

Scenario: Single-module default app bundles successfully
    When I bundle "default_app" using the "standard" bugsnag config
    And I wait to receive a build
    And I wait to receive an upload

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
      | 1              | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       | overwrite |
      | 1           | 1.0         | com.bugsnag.android.example | null      |

    And 1 uploads have an R8 mapping file with the following symbols:
      | jvmSymbols |
      | com.Bar |
      | void doSomething() |

Scenario: Bundling multiple flavors automatically
    When I bundle "flavors" using the "standard" bugsnag config
    And I wait to receive 2 builds
    And I wait to receive 2 uploads

    Then 2 builds are valid for the build API and match the following:
      | appVersion | appVersionCode |
      | 1.0        | 1              |
      | 1.0        | 1              |

    And 2 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 1           | 1.0         | com.bugsnag.android.example.foo |
      | 1           | 1.0         | com.bugsnag.android.example.bar |

Scenario: Bundling single flavor
    When I bundle the "Foo" variantOutput for "flavors" using the "standard" bugsnag config
    And I wait to receive a build
    And I wait to receive an upload

    Then 1 builds are valid for the build API and match the following:
      | appVersion | appVersionCode |
      | 1.0        | 1              |

    And 1 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 1           | 1.0         | com.bugsnag.android.example.foo |

Scenario: Auto upload disabled
    When I bundle "default_app" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no builds
    And I should receive no uploads
