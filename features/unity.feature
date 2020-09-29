Feature: Exported Unity project uploads mapping files

Scenario: Unity 2018 exported gradle project uploads JVM/release/Unity information
    When I run the script "features/scripts/build_unity_2018.sh" synchronously
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId               |
      | 1           | 1.0         | com.bugsnag.example |

    And 4 requests are valid for the android unity NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | armeabi-v7a | /\S+/       | libil2cpp.sym    |
        | armeabi-v7a | /\S+/       | libunity.sym.so  |
        | x86         | /\S+/       | libil2cpp.sym    |
        | x86         | /\S+/       | libunity.sym.so  |

Scenario: Unity 2019 exported gradle project uploads JVM/release/Unity information
    When I run the script "features/scripts/build_unity_2019.sh" synchronously
    And I wait to receive 4 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId               |
      | 1           | 1.0         | com.bugsnag.example |

    # Unity 2019 doesn't contain symbols for x86
    And 2 requests are valid for the android unity NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | armeabi-v7a | /\S+/       | libil2cpp.sym.so |
        | armeabi-v7a | /\S+/       | libunity.sym.so  |

Scenario: Shared object files not uploaded when uploadNdkUnityLibraryMappings set to false
    When I set environment variable "UNITY_SO_UPLOAD" to "false"
    And I run the script "features/scripts/build_unity_2019.sh" synchronously
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId               |
        | 1           | 1.0         | com.bugsnag.example |

Scenario: Bundling a Unity project uploads JVM/release/Unity information
    When I run the script "features/scripts/bundle_unity_2019.sh" synchronously
    And I wait to receive 4 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId               |
        | 1           | 1.0         | com.bugsnag.example |

# Unity 2019 doesn't contain symbols for x86
    And 2 requests are valid for the android unity NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | armeabi-v7a | /\S+/       | libil2cpp.sym.so |
        | armeabi-v7a | /\S+/       | libunity.sym.so  |

Scenario: Building a Unity product flavor uploads Unity SO files
    When I set environment variable "UNITY_FLAVORS" to "true"
    When I run the script "features/scripts/build_unity_2018_flavors.sh" synchronously
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId               |
        | 1           | 1.0         | com.bugsnag.example |

    And 4 requests are valid for the android unity NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | armeabi-v7a | /\S+/       | libil2cpp.sym    |
        | armeabi-v7a | /\S+/       | libunity.sym.so  |
        | x86         | /\S+/       | libil2cpp.sym    |
        | x86         | /\S+/       | libunity.sym.so  |

Scenario: Building a Unity product flavor uploads Unity SO files
    When I set environment variable "UNITY_ABI_SPLITS" to "true"
    When I run the script "features/scripts/build_unity_2018_abi_splits.sh" synchronously
    And I wait to receive 2 requests

    And 2 requests are valid for the android unity NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | armeabi-v7a | /\S+/       | libil2cpp.sym    |
        | armeabi-v7a | /\S+/       | libunity.sym.so  |
