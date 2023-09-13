package com.bugsnag.android.gradle

import com.android.build.api.artifact.Artifacts
import com.android.build.gradle.AppExtension
import org.gradle.api.Action
import org.gradle.api.Project

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

    /**
     * Retrieves the Dexguard Plugin version from either the path or the version on the DexGuard plugin.
     */
    static String getDexguardVersionString(Project project) {
        def dexguard = project.extensions.findByName("dexguard")

        try {
            if (dexguard == null) {
                return null
            }
            if (dexguard.hasProperty("version") && dexguard.version != null) {
                return dexguard.version
            } else {
                return "9.0.0"
            }
        } catch (MissingPropertyException ignored) {
            // running earlier version of DexGuard, ignore missing property
            return null
        }
    }

    static boolean isDexguardEnabledForVariant(Project project, String variantName) {
        def dexguard = project.extensions.findByName("dexguard")

        try {
            if (dexguard == null) {
                return null
            }

            if (dexguard.configurations != null) {
                return dexguard.configurations.findByName(variantName) != null
            } else {
                // no configurations = assume all variants configured
                return true
            }
        } catch (MissingPropertyException ignored) {
            // running earlier version of DexGuard, ignore missing property
            return false
        }
    }
}
