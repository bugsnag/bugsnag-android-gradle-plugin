#!/usr/bin/env bash

cd features/fixtures/app
./gradlew :module:clean :module:bundleFoo --stacktrace
