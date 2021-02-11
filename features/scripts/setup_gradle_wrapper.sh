#!/usr/bin/env bash
set -e

export UPDATING_GRADLEW=true
cd "$APP_FIXTURE_DIR"
echo "Updating gradle wrapper for: $APP_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"

cd ../../../ && cd "$NDK_FIXTURE_DIR"
echo "Updating gradle wrapper for: $NDK_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"

cd ../../../ && cd "$RN_FIXTURE_DIR"
echo "Updating gradle wrapper for: $RN_FIXTURE_DIR"
npm i
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"

cd ../../../../ && "$RN_MONOREPO_FIXTURE_DIR"
echo "Updating gradle wrapper for: $RN_MONOREPO_FIXTURE_DIR"
# The monorepo setup uses Yarn workspaces so we use Yarn over NPM here
yarn install
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"

cd ../../../../ && cd "$UNITY_2018_FIXTURE_DIR"
echo "Updating gradle wrapper for: $UNITY_2018_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"

cd ../../../../ && cd "$UNITY_2019_FIXTURE_DIR"
echo "Updating gradle wrapper for: $UNITY_2019_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"

unset UPDATING_GRADLEW
