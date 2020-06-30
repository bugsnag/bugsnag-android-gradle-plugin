#!/usr/bin/env bash

# Build test app
cd features/fixtures/ndkapp
./gradlew :app:clean :app:build -x lint \
    --stacktrace --refresh-dependencies
