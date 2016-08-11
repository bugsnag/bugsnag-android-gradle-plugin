package com.bugsnag.android.gradle

import java.io.FileWriter
import java.io.PrintWriter
import java.util.UUID

import groovy.util.XmlNodePrinter
import groovy.util.XmlParser
import groovy.xml.Namespace

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
    Task to add a unique build UUID to AndroidManifest.xml during the build
    process. This is used by Bugsnag to identify which proguard mapping file
    should be used to deobfuscate each crash report.

    https://docs.gradle.org/current/userguide/custom_tasks.html

    This task must be called after "process${variantName}Manifest", since it
    requires that an AndroidManifest.xml exists in `build/intermediates`.
*/
class BugsnagManifestTask extends DefaultTask {
    String manifestPath

    BugsnagManifestTask() {
        super()
        this.description = "Adds a unique build UUID to AndroidManifest to link proguard mappings to crash reports"
    }

    @TaskAction
    def updateManifest() {
        // Parse the AndroidManifest.xml
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        def xml = new XmlParser().parse(manifestPath)

        def application = xml.application[0]
        if (application) {
            def metaDataTags = application['meta-data']

            // remove any old BUILD_UUID_TAG elements
            def buildUuidTags = metaDataTags.findAll{
                it.attributes()[ns.name].equals(BugsnagPlugin.BUILD_UUID_TAG)
            }.each{
                it.parent().remove(it)
            }

            // Uniquely identify the build so that we can identify the proguard file.
            def buildUUID = UUID.randomUUID().toString()

            // Add the new BUILD_UUID_TAG element
            application.appendNode('meta-data', [(ns.name): BugsnagPlugin.BUILD_UUID_TAG, (ns.value): buildUUID])

            // Write the manifest file
            def writer = new FileWriter(manifestPath)
            def printer = new XmlNodePrinter(new PrintWriter(writer))
            printer.preserveWhitespace = true
            printer.print(xml)
        }
    }

    def isInstantRun() {
        project.properties["android.optional.compilation"]?.contains("INSTANT_DEV")
    }

    def shouldRun() {
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        def app = new XmlParser().parse(manifestPath).application[0]
        if (app) {
            let tagCount = app['meta-data'].findAll {
                it.attributes()[ns.name].equals(BugsnagPlugin.BUILD_UUID_TAG)
            }.size()
            tagCount == 0 || !isInstantRun()
        } else {
            false
        }
    }
}
