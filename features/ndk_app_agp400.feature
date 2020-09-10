Feature: Plugin integrated in NDK app

@requires_agp4_0_or_higher
Scenario: NDK apps send requests
    When I build the NDK app
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 5 requests are valid for the android mapping API and match the following:
      | arch        | projectRoot |
      | arm64-v8a   | foo         |
      | armeabi-v7a | foo         |
      | x86         | foo         |
      | x86_64      | foo         |
      | foo         | foo         |

@requires_agp4_0_or_higher
Scenario: Custom projectRoot is added to payload
    When I set environment variable "PROJECT_ROOT" to "/repos/custom/my-app"
    And I build the NDK app
    And I wait to receive 6 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 5 requests are valid for the android mapping API and match the following:
      | arch        | projectRoot          |
      | arm64-v8a   | /repos/custom/my-app |
      | armeabi-v7a | /repos/custom/my-app |
      | x86         | /repos/custom/my-app |
      | x86_64      | /repos/custom/my-app |
      | foo         | /repos/custom/my-app |

# Sets a non-existent objdump location for x86 and arm64-v8a, delivery should proceed as normal for other files
@requires_agp4_0_or_higher
Scenario: Custom objdump location
    When I set environment variable "OBJDUMP_LOCATION" to "/fake/objdump"
    And I build the NDK app
    And I wait to receive 4 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 3 requests are valid for the android mapping API and match the following:
      | arch           |
      | armeabi-v7a    |
      | x86_64         |
