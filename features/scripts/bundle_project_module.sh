#!/usr/bin/env bash

# Alter git remote origin url in case protocol differs
PREV_REMOTE=`git config --get remote.origin.url`
git config --replace-all remote.origin.url "https://github.com/bugsnag/bugsnag-android-gradle-plugin.git"

function resetGitConfig {
  git config --replace-all remote.origin.url $PREV_REMOTE
}

trap resetGitConfig EXIT

cd $APP_FIXTURE_DIR
echo "Test fixture used: $APP_FIXTURE_DIR"
./gradlew :module:clean :module:bundle -x lint --stacktrace
