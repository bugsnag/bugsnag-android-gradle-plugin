package com.bugsnag.android.gradle

import com.android.build.api.artifact.Artifacts
import com.android.build.gradle.AppExtension
import org.gradle.api.Action

/**
 * Contains functions which exploit Groovy's metaprogramming to provide backwards
 * compatibility for older AGP versions that would be impractical to achieve with
 * Kotlin's static type system.
 */
class GroovyCompat {

    static void registerUuidTaskAGP41(
        AppExtension android,
        Action<String> onVariantsCallback,
        Action<Artifacts> onPropertiesCallback
    ) {
        android.onVariants {
            onVariantsCallback.execute(name)
            onProperties {
                onPropertiesCallback.execute(artifacts)
            }
        }
    }
}
