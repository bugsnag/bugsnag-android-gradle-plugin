Feature: Plugin integrated in NDK app

# note: liblog is in the SO folder when built on Travis due to environment
# differences compared to building locally so these scenarios can fail when run locally

@requires_agp4_0_or_higher
Scenario: NDK apps send requests
    When I build the NDK app
    And I wait to receive a build
    And I wait to receive 9 uploads

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 8 uploads are valid for the android NDK mapping API and match the following:
      | arch        | projectRoot | sharedObjectName |
      | arm64-v8a   | /\S+/       | liblog.so        |
      | arm64-v8a   | /\S+/       | libnative-lib.so |
      | armeabi-v7a | /\S+/       | liblog.so        |
      | armeabi-v7a | /\S+/       | libnative-lib.so |
      | x86         | /\S+/       | liblog.so        |
      | x86         | /\S+/       | libnative-lib.so |
      | x86_64      | /\S+/       | liblog.so        |
      | x86_64      | /\S+/       | libnative-lib.so |

    And 1 uploads are valid for the android mapping API and match the following:
      | appId                      |
      | com.bugsnag.android.ndkapp |

    And 1 uploads have an R8 mapping file with the following symbols:
      | jvmSymbols |
      | com.bugsnag.android.ndkapp.MainActivity |

@requires_agp4_0_or_higher
Scenario: Custom projectRoot is added to payload
    When I set environment variable "PROJECT_ROOT" to "/repos/custom/my-app"
    And I build the NDK app
    And I wait to receive a build
    And I wait to receive 9 uploads

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 8 uploads are valid for the android NDK mapping API and match the following:
      | arch        | projectRoot          |
      | arm64-v8a   | /repos/custom/my-app |
      | armeabi-v7a | /repos/custom/my-app |
      | x86         | /repos/custom/my-app |
      | x86_64      | /repos/custom/my-app |
      | arm64-v8a   | /repos/custom/my-app |
      | armeabi-v7a | /repos/custom/my-app |
      | x86         | /repos/custom/my-app |
      | x86_64      | /repos/custom/my-app |

    And 1 uploads are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |

# Sets a non-existent objdump location for x86 and arm64-v8a, delivery should proceed as normal for other files
@requires_agp4_0_or_higher
Scenario: Custom objdump location
    When I set environment variable "OBJDUMP_LOCATION" to "/fake/objdump"
    And I build the NDK app
    And I wait to receive a build
    And I wait to receive 5 uploads

    Then 1 builds are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 4 uploads are valid for the android NDK mapping API and match the following:
      | arch           |
      | armeabi-v7a    |
      | armeabi-v7a    |
      | x86_64         |
      | x86_64         |

    And 1 uploads are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |

@requires_agp4_0_or_higher
Scenario: Mapping files uploaded for custom sharedObjectPaths
    When I set environment variable "USE_SHARED_OBJECT_PATH" to "true"
    When I build the NDK app
    And I wait to receive a build
    And I wait to receive 13 uploads

    Then 1 builds are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 12 uploads are valid for the android NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | arm64-v8a   | /\S+/       | liblog.so        |
        | arm64-v8a   | /\S+/       | libnative-lib.so |
        | arm64-v8a   | /\S+/       | libmonochrome.so |
        | armeabi-v7a | /\S+/       | liblog.so        |
        | armeabi-v7a | /\S+/       | libnative-lib.so |
        | armeabi-v7a | /\S+/       | libmonochrome.so |
        | x86         | /\S+/       | liblog.so        |
        | x86         | /\S+/       | libnative-lib.so |
        | x86         | /\S+/       | libmonochrome.so |
        | x86_64      | /\S+/       | liblog.so        |
        | x86_64      | /\S+/       | libnative-lib.so |
        | x86_64      | /\S+/       | libmonochrome.so |

    And 1 uploads are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |
