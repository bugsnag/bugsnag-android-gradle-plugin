#!/usr/bin/env bash
set -ex

cd "$RN_MONOREPO_FIXTURE_DIR"
echo "Test fixture used: $RN_MONOREPO_FIXTURE_DIR, AGP=$AGP_VERSION, Gradle=$GRADLE_WRAPPER_VERSION"

yarn install

./gradlew :app:clean :app:assemble -x lint --stacktrace
