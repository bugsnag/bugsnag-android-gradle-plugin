package com.bugsnag.android.gradle

import com.android.build.api.artifact.Artifacts
import com.android.build.gradle.AppExtension
import org.gradle.api.Action
import org.gradle.api.Project

import java.nio.file.Paths

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
            if (dexguard.version != null) {
                return dexguard.version
            } else {
                // the path value is structured like this: DexGuard-8.7.02
                if (dexguard.path == null) {
                    return null
                }
                File dexguardDir = Paths.get(dexguard.path).toFile()
                String normalizedDir = dexguardDir.canonicalFile.name
                return normalizedDir.replace("DexGuard-", "")
            }
        } catch (MissingPropertyException ignored) {
            // running earlier version of DexGuard, ignore missing property
            return null
        }
    }
}
