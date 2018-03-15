#!/usr/bin/env bash

if [ -z "$PROJECT_MODULE" ]; then
    echo PROJECT_MODULE environment variable is not set
    exit 1
fi

cd features/fixtures/app
./gradlew :$PROJECT_MODULE:clean :$PROJECT_MODULE:build --stacktrace
