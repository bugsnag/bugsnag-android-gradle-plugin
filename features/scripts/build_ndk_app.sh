#!/usr/bin/env bash

# Build test app
cd features/fixtures/ndkapp
./gradlew :app:clean :app:build \
    --stacktrace --refresh-dependencies
