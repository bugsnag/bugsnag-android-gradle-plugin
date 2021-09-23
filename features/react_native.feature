Feature: Plugin integrated in React Native app

Scenario: Source maps are uploaded when assembling an app with the default project structure
    When I build the React Native app
    And I wait to receive a build
    And I wait to receive 2 uploads

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 5              | 2.45.beta  | gradle-android |

    And 1 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

Scenario: Source maps are uploaded when bundling an app with the default project structure
    And I run the script "features/scripts/bundle_react_native_app.sh" synchronously
    And I wait to receive a build
    And I wait to receive 2 uploads

    Then 1 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |

    And 1 uploads are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                     |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

Scenario: Source maps are uploaded when assembling an app which uses productFlavors
    When I set environment variable "USE_RN_FLAVORS" to "true"
    When I build the React Native app
    And I wait to receive 2 builds
    And I wait to receive 4 uploads

    Then 2 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |
        | 5              | 2.45.beta  | gradle-android |

    And 2 uploads are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                         |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp.foo |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp.bar |

    And 2 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |

Scenario: Source maps are uploaded when assembling an app within a monorepo
    When I run the script "features/scripts/build_react_native_monorepo_app.sh" synchronously
    And I wait to receive a build
    And I wait to receive 2 uploads

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 5              | 2.45.beta  | gradle-android |

    And 1 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 5           | 2.45.beta   | com.bugsnag.android.rnapp |

    And 1 uploads are valid for the JS source map API and match the following:
      | appVersionCode | appVersion | overwrite | dev   |
      | 5              | 2.45.beta  | true      | false |

Scenario: Setting uploadReactNativeMappings to false will prevent any source map upload
    When I set environment variable "UPLOAD_RN_MAPPINGS" to "false"
    When I build the React Native app
    And I wait to receive a build
    And I wait to receive an upload

    Then 1 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |

    And 1 uploads are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                     |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

Scenario: Manually invoking source map upload task
    And I run the script "features/scripts/manual_upload_react_native.sh" synchronously
    And I wait to receive an upload
    And 1 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

# Hermes driver cannot build rn_60 fixture
@skip_rn60_fixture
Scenario: Source maps are uploaded in an app using Hermes
    When I set environment variable "RN_ENABLE_HERMES" to "true"
    When I build the React Native app
    And I wait to receive 1 build
    And I wait to receive 2 uploads

    Then 1 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 5              | 2.45.beta  | gradle-android |

    And 1 uploads are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                     |
        | 5           | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |

Scenario: Plugin handles server failure gracefully
    When I set the HTTP status code to 500
    And I run the script "features/scripts/manual_upload_react_native.sh" synchronously
    And I wait to receive 5 uploads

    Then 5 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |
        | 5              | 2.45.beta  | false     | false |

Scenario: Source maps are uploaded when assembling an app with a custom nodeModulesDir
    When I set environment variable "CUSTOM_NODE_MODULES_DIR" to "true"
    When I build the React Native app
    And I wait to receive a build
    And I wait to receive 2 uploads

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 5              | 2.45.beta  | gradle-android |

    And 1 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                     |
      | 5              | 2.45.beta  | com.bugsnag.android.rnapp |

    And 1 uploads are valid for the JS source map API and match the following:
        | appVersionCode | appVersion | overwrite | dev   |
        | 5              | 2.45.beta  | false     | false |
