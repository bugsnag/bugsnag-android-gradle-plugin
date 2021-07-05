#!/usr/bin/env bash
set -e

# Build test app
cd $LIB_FIXTURE_DIR
echo "Test fixture used: $LIB_FIXTURE_DIR, AGP=$AGP_VERSION, Gradle=$GRADLE_WRAPPER_VERSION"
./gradlew :app:clean :app:assemble -x lint --stacktrace
