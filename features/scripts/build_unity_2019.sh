#!/usr/bin/env bash
set -e

cd features/fixtures/unity_2019
./gradlew clean assembleRelease --stacktrace
