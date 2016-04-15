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


Releasing a New Version
-----------------------

If you are a project maintainer, you can build and release a new version of
`bugsnag-android-gradle-plugin` as follows:

### 1. Prepare for release

-   Update the `CHANGELOG` and `README.md` with any new features

-   Update the version number in `build.gradle`

-   Commit and tag the release

    ```shell
    git commit -am "v1.x.x"
    git tag v1.x.x
    git push origin master && git push --tags
    ```

### 2. Upload the jar to the GitHub releases page

- https://github.com/bugsnag/bugsnag-android-gradle-plugin/releases

### 3. Release to Maven Central

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

-   Build and upload the new version

    ```shell
    ./gradlew clean uploadArchives
    ```

-   "Promote" the release build on Maven Central

    -   Go to the [sonatype open source dashboard](https://oss.sonatype.org/index.html#stagingRepositories)
    -   Click “Staging Repositories”
    -   Click the search box at the top right, and type “com.bugsnag”
    -   Select the com.bugsnag staging repository
    -   Click the “close” button in the toolbar, no message
    -   Click the “refresh” button
    -   Select the com.bugsnag closed repository
    -   Click the “release” button in the toolbar

### 4. Release to plugins.gradle.com

-   Get the API key from James and [add it to your Gradle configuration](https://plugins.gradle.org/docs/submit)
-   Run `gradle publishPlugins`

### 5. Update docs.bugsnag.com

Update the setup guide for Java (Android) with any new content.
