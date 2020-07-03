#!/usr/bin/env bash

# Build test app
cd features/fixtures/ndkapp_agp340
./gradlew :app:clean :app:build -x lint \
    --stacktrace --refresh-dependencies
