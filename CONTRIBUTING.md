Contributing
============

-   [Fork](https://help.github.com/articles/fork-a-repo) the [notifier on github](https://github.com/bugsnag/bugsnag-android-gradle-plugin)
-   Build and test your changes
-   Commit and push until you are happy with your contribution
-   [Make a pull request](https://help.github.com/articles/using-pull-requests)
-   Thanks!


Building the Plugin
-------------------
```shell
./gradlew clean build
```

Jar files are generated into `build/libs`.


Installing Locally
------------------

Install the plugin to your local Maven repository:

```shell
./gradlew clean install
```

Add `mavenLocal` to your buildscript repositories:

```groovy
buildscript {
    repositories {
        mavenLocal()
    }
}
```

If you need to remove the plugin, run the following command then perform a clean build:

```shell
rm ~/.m2/repository/com/bugsnag/bugsnag-android-gradle-plugin
```

Releasing a New Version
-----------------------

## Release Checklist
Please follow the testing instructions in [the platforms release checklist](https://github.com/bugsnag/platforms-release-checklist/blob/master/README.md), and any additional steps directly below.

- Trigger a New Build in Buildkite on the target branch, and verify that the private scenarios pass.

- Use `gradlew clean install` to install a release build to a local maven repo, and `gradlew clean build` to refresh the example project.

### Instructions

If you are a project maintainer, you can build and release a new version of
`bugsnag-android-gradle-plugin` as follows:

#### 0. One-time setup

-   Create a file `~/.gradle/gradle.properties` with the following contents:

    ```ini
    # Your credentials for https://oss.sonatype.org/
    nexusUsername=your-nexus-username
    nexusPassword=your-nexus-password

    # GPG key details
    signing.keyId=your-gpg-key-id # From gpg --list-keys
    signing.password=your-gpg-key-passphrase
    signing.secretKeyRingFile=/Users/james/.gnupg/secring.gpg
    ```
-   Get the API key from James and [add it to your Gradle configuration](https://plugins.gradle.org/docs/submit)

#### 1. Prepare for release

- Update the `CHANGELOG` and `README.md` with any new features
- Run `make VERSION={version_number} bump` to update the version number
- Inspect the changes, confirm that the changeset behaves as expected
- Run `make VERSION={version_number} release` to publish the release. Press
  return after running the command to kick off the build.

#### 2. Upload the jar to the GitHub releases page

- https://github.com/bugsnag/bugsnag-android-gradle-plugin/releases

#### 3. Release to Maven Central

-   "Promote" the release build on Maven Central

    -   Go to the [sonatype open source dashboard](https://oss.sonatype.org/index.html#stagingRepositories)
    -   Click “Staging Repositories”
    -   Click the search box at the top right, and type “com.bugsnag”
    -   Select the com.bugsnag staging repository
    -   Click the “close” button in the toolbar, no message
    -   Click the “refresh” button
    -   Select the com.bugsnag closed repository
    -   Click the “release” button in the toolbar

### 4. Update docs.bugsnag.com

Update the setup guide for Java (Android) with any new content.
