#!/usr/bin/env bash

# Alter git remote origin url in case protocol differs
PREV_REMOTE=`git config --get remote.origin.url`
git config --replace-all remote.origin.url "https://github.com/bugsnag/agp.git"

function resetGitConfig {
  git config --replace-all remote.origin.url $PREV_REMOTE
}

trap resetGitConfig EXIT

cd features/fixtures/app
./gradlew :module:clean :module:build --stacktrace
