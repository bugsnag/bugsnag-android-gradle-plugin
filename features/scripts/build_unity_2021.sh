#!/usr/bin/env bash
set -e

cd features/fixtures/unity_2021
./gradlew clean assembleRelease --stacktrace
