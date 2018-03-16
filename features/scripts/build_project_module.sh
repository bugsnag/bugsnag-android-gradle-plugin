#!/usr/bin/env bash

cd features/fixtures/app
./gradlew :module:clean :module:build --stacktrace
