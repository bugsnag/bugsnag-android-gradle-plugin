#!/usr/bin/env bash
set -e

cd $APP_FIXTURE_DIR
echo "Test fixture used: $APP_FIXTURE_DIR, AGP=$AGP_VERSION, Gradle=$GRADLE_WRAPPER_VERSION"
./gradlew :module:clean :module:bundleFoo -x lint --stacktrace
