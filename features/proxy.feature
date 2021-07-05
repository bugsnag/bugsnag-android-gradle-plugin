Feature: Plugin sends requests when a proxy is set
# note: default JVM behaviour ignores localhost for proxy traffic, these scenarios need to
# override this. see https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html#proxies

Scenario: Basic HTTP proxy
    When I start an http proxy
    And I set the fixture JVM arguments to "-Dhttp.proxyHost=localhost -Dhttp.proxyPort=9000 -Dhttp.nonProxyHosts="
    And I build "default_app" using the "standard" bugsnag config
    And I wait to receive 2 builds
    Then the proxy handled a request for "localhost:9339"

    And 1 requests are valid for the build API and match the following:
      | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
      | 1              | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 requests are valid for the android mapping API and match the following:
      | versionCode | versionName | appId                       | overwrite |
      | 1           | 1.0         | com.bugsnag.android.example | null      |

Scenario: Authenticated HTTP proxy with creds
    When I start an authenticated http proxy
    And I set the fixture JVM arguments to "-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=9000 -Dhttp.proxyUser=user -Dhttp.proxyPassword=password -Dhttp.nonProxyHosts="
    And I build "default_app" using the "standard" bugsnag config
    And I wait to receive 2 builds
    Then the proxy handled a request for "localhost:9339"

    And 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      | sourceControl.provider | sourceControl.repository                                     |
        | 1              | 1.0        | gradle-android | github                 | https://github.com/bugsnag/bugsnag-android-gradle-plugin.git |

    And 1 requests are valid for the android mapping API and match the following:
        | versionCode | versionName | appId                       | overwrite |
        | 1           | 1.0         | com.bugsnag.android.example | null      |

Scenario: Authenticated HTTP proxy without creds
    When I start an authenticated http proxy
    And I set the fixture JVM arguments to "-Dhttp.proxyHost=localhost -Dhttp.proxyPort=9000 -Dhttp.nonProxyHosts="
    And I build "default_app" using the "standard" bugsnag config
    Then I wait for 5 seconds
    And I should receive no builds

@skip_agp4_0_or_higher
@skip_agp3_4_0
Scenario: NDK request for basic HTTP proxy AGP < 4
    When I start an http proxy
    And I set the fixture JVM arguments to "-Dhttp.proxyHost=localhost -Dhttp.proxyPort=9000 -Dhttp.nonProxyHosts="
    When I build the NDK app
    And I wait to receive 6 builds

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 4 requests are valid for the android NDK mapping API and match the following:
        | arch        |
        | arm64-v8a   |
        | armeabi-v7a |
        | x86         |
        | x86_64      |

    And 1 requests are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |

@requires_agp4_0_or_higher
Scenario: NDK request for basic HTTP proxy AGP >= 4
    When I start an http proxy
    And I set the fixture JVM arguments to "-Dhttp.proxyHost=localhost -Dhttp.proxyPort=9000 -Dhttp.nonProxyHosts="
    When I build the NDK app
    And I wait to receive 10 builds

    Then 1 requests are valid for the build API and match the following:
        | appVersionCode | appVersion | buildTool      |
        | 1              | 1.0        | gradle-android |

    And 8 requests are valid for the android NDK mapping API and match the following:
        | arch        |
        | arm64-v8a   |
        | arm64-v8a   |
        | armeabi-v7a |
        | armeabi-v7a |
        | x86         |
        | x86         |
        | x86_64      |
        | x86_64      |

    And 1 requests are valid for the android mapping API and match the following:
        | appId                      |
        | com.bugsnag.android.ndkapp |
