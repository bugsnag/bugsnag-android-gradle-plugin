#!/usr/bin/env bash
set -ex

export UPDATING_GRADLEW=true

update_gradle_wrapper_properties () {
    sed -i.bak -e "s/^distributionUrl.*$/distributionUrl=https\\\\:\/\/services.gradle.org\/distributions\/gradle-$GRADLE_WRAPPER_VERSION-bin.zip/" gradle/wrapper/gradle-wrapper.properties
}

update_gradle_wrapper_in () {
    pushd "$1"
    echo "Updating gradle wrapper for: $1"
    update_gradle_wrapper_properties
    popd
}

update_gradle_wrapper_in "$APP_FIXTURE_DIR"
update_gradle_wrapper_in "$NDK_FIXTURE_DIR"
update_gradle_wrapper_in "$UNITY_2018_FIXTURE_DIR"
update_gradle_wrapper_in "$UNITY_2019_FIXTURE_DIR"
update_gradle_wrapper_in "$LIB_FIXTURE_DIR"

pushd "$RN_FIXTURE_DIR"
echo "Updating gradle wrapper for: $RN_FIXTURE_DIR"
npm i --silent
update_gradle_wrapper_properties
popd

pushd "$RN_MONOREPO_FIXTURE_DIR"
echo "Updating gradle wrapper for: $RN_MONOREPO_FIXTURE_DIR"
# The monorepo setup uses Yarn workspaces so we use Yarn over NPM here
yarn install --silent
update_gradle_wrapper_properties
popd

unset UPDATING_GRADLEW
