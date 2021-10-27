#!/usr/bin/env bash
set -ex

export UPDATING_GRADLEW=true

pushd "$APP_FIXTURE_DIR"
echo "Updating gradle wrapper for: $APP_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

pushd "$NDK_FIXTURE_DIR"
echo "Updating gradle wrapper for: $NDK_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

pushd "$RN_FIXTURE_DIR"
echo "Updating gradle wrapper for: $RN_FIXTURE_DIR"
npm i --silent
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

pushd "$RN_MONOREPO_FIXTURE_DIR"
echo "Updating gradle wrapper for: $RN_MONOREPO_FIXTURE_DIR"
# The monorepo setup uses Yarn workspaces so we use Yarn over NPM here
yarn install --silent
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

pushd "$UNITY_2018_FIXTURE_DIR"
echo "Updating gradle wrapper for: $UNITY_2018_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

pushd "$UNITY_2019_FIXTURE_DIR"
echo "Updating gradle wrapper for: $UNITY_2019_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

pushd "$LIB_FIXTURE_DIR"
echo "Updating gradle wrapper for: $LIB_FIXTURE_DIR"
./gradlew wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
popd

unset UPDATING_GRADLEW