package com.bugsnag.android.gradle

import com.android.build.api.artifact.Artifacts
import com.android.build.gradle.AppExtension
import org.gradle.api.Action
import org.gradle.api.Project

import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.Manifest

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

                File dexguardDir = project.file(dexguard.path).getCanonicalFile()

                // Get the version from the dexguard.jar manifest
                URL url = new URL("jar:file:$dexguardDir/lib/dexguard.jar!/")
                URLConnection jarURLConnection = url.openConnection() as JarURLConnection
                Manifest manifest = jarURLConnection.manifest
                Attributes attrs = manifest.mainAttributes
                return attrs.getValue("Implementation-Version")
            }
        } catch (MissingPropertyException ignored) {
            // running earlier version of DexGuard, ignore missing property
            return null
        }
    }
}
