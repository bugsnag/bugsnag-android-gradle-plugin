#!/usr/bin/env bash

BUILD_VERSION=9000.0.0-test

# Build plugin
./gradlew build -Pversion=$BUILD_VERSION
# Install to maven local
mvn install:install-file \
    -Dfile=build/libs/bugsnag-android-gradle-plugin-$BUILD_VERSION.jar  \
    -DgroupId=com.bugsnag -DartifactId=bugsnag-android-gradle-plugin \
    -Dversion=$BUILD_VERSION \
    -Dpackaging=jar

# Build test app
cd features/fixtures/ndkapp
./gradlew :app:clean :app:build \
    -Pplugin_version=$BUILD_VERSION \
    --stacktrace --refresh-dependencies
