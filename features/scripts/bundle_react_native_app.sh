#!/usr/bin/env bash
set -e

cd $RN_FIXTURE_DIR
echo "Test fixture used: $RN_FIXTURE_DIR, AGP=$AGP_VERSION, Gradle=$GRADLE_WRAPPER_VERSION"
npm install
./gradlew :app:clean :app:bundle -x lint --stacktrace
