Feature: Plugin integrated in project with ABI APK splits

Scenario: ABI Splits project builds successfully
    When I build "abi_splits" using the "standard" bugsnag config
    And I wait to receive 16 requests

    Then 8 requests are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 1              | 1.0        |
      | 2              | 1.0        |
      | 3              | 1.0        |
      | 4              | 1.0        |
      | 5              | 1.0        |
      | 6              | 1.0        |
      | 7              | 1.0        |
      | 8              | 1.0        |

    And 8 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       |
      | 1           | 1.0         | com.bugsnag.android.example |
      | 2           | 1.0         | com.bugsnag.android.example |
      | 3           | 1.0         | com.bugsnag.android.example |
      | 4           | 1.0         | com.bugsnag.android.example |
      | 5           | 1.0         | com.bugsnag.android.example |
      | 6           | 1.0         | com.bugsnag.android.example |
      | 7           | 1.0         | com.bugsnag.android.example |
      | 8           | 1.0         | com.bugsnag.android.example |

    And 8 requests have an R8 mapping file with the following symbols:
      | jvmSymbols |
      | com.Bar |
      | void doSomething() |

Scenario: ABI Splits automatic upload disabled
    When I build "abi_splits" using the "all_disabled" bugsnag config
    And I wait for 3 seconds
    Then I should receive no requests

Scenario: ABI Splits manual upload of build API
    When I build the "Armeabi-release" variantOutput for "abi_splits" using the "all_disabled" bugsnag config
    And I wait to receive a request
    Then the request is valid for the Android Mapping API
    And the field "apiKey" for multipart request equals "TEST_API_KEY"
    And the field "versionCode" for multipart request equals "3"
    And the field "versionName" for multipart request equals "1.0"
    And the field "appId" for multipart request equals "com.bugsnag.android.example"
