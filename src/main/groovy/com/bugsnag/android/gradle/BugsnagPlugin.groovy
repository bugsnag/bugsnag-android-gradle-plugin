package com.bugsnag.android.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class BugsnagPlugin implements Plugin<Project> {
  private static final String TASK_NAME = 'bugsnag'
  private static final String API_KEY_TAG = 'com.bugsnag.android.API_KEY'

  void apply(Project project) {
    project.android.applicationVariants.all { variant ->
      // Only create Bugsnag tasks for proguard-enabled variants
      if (!variant.buildType.minifyEnabled) {
        return
      }

      // Get the variant name
      def variantName = getVariantName(variant)

      // Find the processed manifest for this variant
      def manifestPath = variant.outputs[0].processManifest.manifestOutputFile

      // Create Bugsnag post-build task
      def bugsnagTask = project.task("$TASK_NAME$variantName") << {
        // Fetch the Bugsnag API key from the AndroidManifest
        def ns = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", "android")
        def xml = new XmlParser().parse(manifestPath)
        def apiKeyTags = xml.application['meta-data'].findAll{ it.attributes()[ns.name].equals(API_KEY_TAG) }
        if (apiKeyTags.size() == 0) {
          project.logger.warn("Could not find '$API_KEY_TAG' <meta-data> tag in your AndroidManifest.xml")
          return
        }

        def apiKey = apiKeyTags[0].attributes()[ns.value]
        println apiKey

        // TODO: Make sure a proguard mapping file is output
        // build/outputs/proguard/release/mapping.txt

        // TODO: Get the build version

        // TODO: Upload the mapping file to Bugsnag
      }

      // Run Bugsnag post-build tasks as part of a build
      project.tasks.build.dependsOn bugsnagTask
    }
  }

  // Get the "variant" name (flavor + buildtype)
  def getVariantName(variant) {
    def buildTypeName = variant.buildType.name.capitalize()

    def projectFlavorNames = variant.productFlavors.collect { it.name.capitalize() }
    if (projectFlavorNames.isEmpty()) {
      projectFlavorNames = [""]
    }
    def projectFlavorName = projectFlavorNames.join()

    return "$projectFlavorName$buildTypeName"
  }
}
