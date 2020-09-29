#!/usr/bin/env bash
set -e

cd features/fixtures/unity_2018/example
./gradlew clean assembleFooRelease --stacktrace
