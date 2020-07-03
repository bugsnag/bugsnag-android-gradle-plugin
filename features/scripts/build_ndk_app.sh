#!/usr/bin/env bash

# Build test app
cd $NDK_FIXTURE_DIR
echo "Test fixture used: $NDK_FIXTURE_DIR"
./gradlew :app:clean :app:assemble -x lint --stacktrace
