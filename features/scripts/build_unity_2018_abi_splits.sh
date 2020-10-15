#!/usr/bin/env bash
set -e

cd features/fixtures/unity_2018/example
./gradlew clean uploadBugsnagUnityArmeabi-v7a-releaseMapping --stacktrace
