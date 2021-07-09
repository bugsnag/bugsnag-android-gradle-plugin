Feature: Plugin integrated in project with productFlavors

Scenario: Flavors automatic upload on build
    When I build "flavors" using the "standard" bugsnag config
    And I wait to receive 2 builds
    And I wait to receive 2 uploads

    Then 2 builds are valid for the build API and match the following:
      | appVersionCode | appVersion |
      | 1              | 1.0        |
      | 1              | 1.0        |

    And 2 uploads are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                           |
      | 1           | 1.0         | com.bugsnag.android.example.bar |
      | 1           | 1.0         | com.bugsnag.android.example.foo |

    And 2 uploads have an R8 mapping file with the following symbols:
      | jvmSymbols |
      | com.Bar |
      | void doSomething() |

Scenario: Flavors automatic upload disabled
    When I build "flavors" using the "all_disabled" bugsnag config
    And I wait for 5 seconds
    Then I should receive no builds

Scenario: Flavors manual upload of build API
    When I build the "Foo-release" variantOutput for "flavors" using the "all_disabled" bugsnag config
    And I wait to receive an upload
    Then the upload is valid for the Android Mapping API
    And the upload payload field "apiKey" equals "TEST_API_KEY"
    And the upload payload field "versionCode" equals "1"
    And the upload payload field "versionName" equals "1.0"
    And the upload payload field "appId" equals "com.bugsnag.android.example.foo"
