Feature: Exported Unity project uploads mapping files

Scenario: Unity 2018 exported gradle project uploads JVM/release information
    When I run the script "features/scripts/build_unity_2018.sh" synchronously
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       |
      | 1           | 1.0         | com.bugsnag.example |

Scenario: Unity 2019 exported gradle project uploads JVM/release information
    When I run the script "features/scripts/build_unity_2019.sh" synchronously
    And I wait to receive 2 requests

    Then 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      |
      | 1              | 1.0        | gradle-android |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       |
      | 1           | 1.0         | com.bugsnag.example |
