Feature: Plugin integrated in NDK app

Scenario: NDK apps send requests
    When I build the NDK app
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 4 requests are valid for the android NDK mapping API and match the following:
      | arch        | projectRoot | sharedObjectName |
      | arm64-v8a   | /\S+/       | libnative-lib.so |
      | armeabi-v7a | /\S+/       | libnative-lib.so |
      | x86         | /\S+/       | libnative-lib.so |
      | x86_64      | /\S+/       | libnative-lib.so |

    And 1 requests are valid for the android mapping API and match the following:
      | appId                      |
      | com.bugsnag.android.ndkapp |

    And 1 requests have an R8 mapping file with the following symbols:
      | jvmSymbols |
      | com.bugsnag.android.ndkapp.MainActivity |

Scenario: Custom projectRoot is added to payload
    When I set environment variable "PROJECT_ROOT" to "/repos/custom/my-app"
    And I build the NDK app
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 4 requests are valid for the android NDK mapping API and match the following:
      | arch        | projectRoot          |
      | arm64-v8a   | /repos/custom/my-app |
      | armeabi-v7a | /repos/custom/my-app |
      | x86         | /repos/custom/my-app |
      | x86_64      | /repos/custom/my-app |

    And 1 requests are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |

# Sets a non-existent objdump location for x86 and arm64-v8a, delivery should proceed as normal for other files
Scenario: Custom objdump location
    When I set environment variable "OBJDUMP_LOCATION" to "/fake/objdump"
    And I build the NDK app
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 4 requests are valid for the android NDK mapping API and match the following:
      | arch           |
      | armeabi-v7a    |
      | armeabi-v7a    |
      | x86_64         |
      | x86_64         |

    And 1 requests are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |

Scenario: Mapping files uploaded for custom sharedObjectPaths
    When I set environment variable "USE_SHARED_OBJECT_PATH" to "true"
    When I build the NDK app
    And I wait to receive 10 requests

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 8 requests are valid for the android NDK mapping API and match the following:
        | arch        | projectRoot | sharedObjectName |
        | arm64-v8a   | /\S+/       | libnative-lib.so |
        | arm64-v8a   | /\S+/       | libmonochrome.so |
        | armeabi-v7a | /\S+/       | libnative-lib.so |
        | armeabi-v7a | /\S+/       | libmonochrome.so |
        | x86         | /\S+/       | libnative-lib.so |
        | x86         | /\S+/       | libmonochrome.so |
        | x86_64      | /\S+/       | libnative-lib.so |
        | x86_64      | /\S+/       | libmonochrome.so |

    And 1 requests are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |
