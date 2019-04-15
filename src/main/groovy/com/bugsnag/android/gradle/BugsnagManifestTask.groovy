package com.bugsnag.android.gradle

import groovy.xml.Namespace
import org.gradle.api.tasks.TaskAction
/**
 Task to add a unique build UUID to AndroidManifest.xml during the build
 process. This is used by Bugsnag to identify which proguard mapping file
 should be used to deobfuscate each crash report.

 https://docs.gradle.org/current/userguide/custom_tasks.html

 This task must be called after "process${variantName}Manifest", since it
 requires that an AndroidManifest.xml exists in `build/intermediates`.
 */
class BugsnagManifestTask extends BugsnagVariantOutputTask {

    BugsnagManifestTask() {
        super()
        this.description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    def updateManifest() {
        def manifestPaths = getManifestPaths()

        // Uniquely identify the build so that we can identify the proguard file.
        def buildUUID = UUID.randomUUID().toString()

        for (def manifestPath in manifestPaths) {
            if (!manifestPath.exists()) {
                continue
            }

            project.logger.debug("Updating manifest with build UUID: " + manifestPath)

            // Parse the AndroidManifest.xml
            def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
            def xml = new XmlParser().parse(manifestPath)

            def application = xml.application[0]
            if (application) {
                def metaDataTags = application['meta-data']

                // remove any old BUILD_UUID_TAG elements
                metaDataTags.findAll {
                    (it.attributes()[ns.name] == BugsnagPlugin.BUILD_UUID_TAG)
                }.each {
                    it.parent().remove(it)
                }

                // Add the new BUILD_UUID_TAG element
                application.appendNode('meta-data', [(ns.name): BugsnagPlugin.BUILD_UUID_TAG, (ns.value): buildUUID])

                // Write the manifest file
                FileWriter writer = null

                try {
                    writer = new FileWriter(manifestPath)
                    def printer = new XmlNodePrinter(new PrintWriter(writer))
                    printer.preserveWhitespace = true
                    printer.print(xml)
                } catch (Exception e) {
                    project.logger.warn("Failed to update manifest with Bugsnag metadata", e)
                } finally {
                    if (writer != null) {
                        writer.close()
                    }
                }
            } else {
                project.logger.error("Bugsnag detected invalid manifest with no " +
                    "application element so did not write Build UUID")
            }
        }
    }

    def isInstantRun() {
        project.properties["android.optional.compilation"]?.contains("INSTANT_DEV")
    }

    def shouldRun() {
        def manifestPaths = getManifestPaths()

        for (def manifestPath in manifestPaths) {
            if (!manifestPath.exists()) {
                continue
            }

            def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
            def app = new XmlParser().parse(manifestPath).application[0]
            if (app) {
                def tagCount = app['meta-data'].findAll {
                    (it.attributes()[ns.name] == BugsnagPlugin.BUILD_UUID_TAG)
                }.size()
                if (tagCount == 0 || !isInstantRun()) {
                    return true
                }
            }
        }
        return false
    }
}
