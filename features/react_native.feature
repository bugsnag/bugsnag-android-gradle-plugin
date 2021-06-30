Feature: Plugin integrated in React Native app

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Source maps are uploaded when assembling an app with the default project structure
    When I build the React Native app
    And I wait to receive 3 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 5              | 2.45.beta  | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Source maps are uploaded when bundling an app with the default project structure
    And I run the script "features/scripts/bundle_react_native_app.sh" synchronously
    And I wait to receive 3 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                     |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Source maps are uploaded when assembling an app which uses productFlavors
    When I set environment variable "USE_RN_FLAVORS" to "true"
    When I build the React Native app
    And I wait to receive 6 requests

    Then 2 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |
        | 5              | 2.45.beta  | gradle-android |

    And 2 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                         |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp.foo |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp.bar |

    And 2 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Source maps are uploaded when assembling an app within a monorepo
    When I run the script "features/scripts/build_react_native_monorepo_app.sh" synchronously
    And I wait to receive 3 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 5              | 2.45.beta  | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 5           | 2.45.beta   | com.bugsnag.android.rnapp |

    And 1 requests are valid for the JS source map API and match the following:
      | appVersionCode | appVersion | overwrite | dev   |
      | 5              | 2.45.beta  | true      | false |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Setting uploadReactNativeMappings to false will prevent any source map upload
    When I set environment variable "UPLOAD_RN_MAPPINGS" to "false"
    When I build the React Native app
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                     |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Manually invoking source map upload task
    And I run the script "features/scripts/manual_upload_react_native.sh" synchronously
    And I wait to receive 1 requests
    And 1 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
# Hermes driver cannot build rn_60 fixture
@skip_rn60_fixture
Scenario: Source maps are uploaded in an app using Hermes
    When I set environment variable "RN_ENABLE_HERMES" to "true"
    When I build the React Native app
    And I wait to receive 3 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                     |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Plugin handles server failure gracefully
    When I set the HTTP status code to 500
    And I run the script "features/scripts/manual_upload_react_native.sh" synchronously
    And I wait to receive 5 requests
    And 5 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
    And the exit code equals 0

# blocked by PLAT-6305
@skip_gradle_7_or_higher
Scenario: Source maps are uploaded when assembling an app with a custom nodeModulesDir
    When I set environment variable "CUSTOM_NODE_MODULES_DIR" to "true"
    When I build the React Native app
    And I wait to receive 3 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 5              | 2.45.beta  | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 5              | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 requests are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |
