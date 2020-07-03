#!/usr/bin/env bash

cd features/fixtures/app_agp340
./gradlew :module:clean :module:bundleFoo -x lint --stacktrace
