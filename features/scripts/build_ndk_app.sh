#!/usr/bin/env bash
set -e

# Build test app
cd $NDK_FIXTURE_DIR
echo "Test fixture used: $NDK_FIXTURE_DIR, AGP=$AGP_VERSION, Gradle=$GRADLE_WRAPPER_VERSION"
./gradlew :app:clean :app:assemble -x lint --stacktrace $CUSTOM_JVM_ARGS
