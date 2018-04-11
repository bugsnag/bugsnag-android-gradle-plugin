#!/usr/bin/env bash

if [ -z "$VARIANT_OUTPUT_NAME" ]; then
    echo VARIANT_OUTPUT_NAME environment variable is not set
    exit 1
fi

cd features/fixtures/app
./gradlew :module:clean :module:build uploadBugsnag${VARIANT_OUTPUT_NAME}Mapping --stacktrace
