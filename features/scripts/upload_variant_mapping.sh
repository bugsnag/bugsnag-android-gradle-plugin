#!/usr/bin/env bash

if [ -z "$VARIANT_OUTPUT_NAME" ]; then
    echo VARIANT_OUTPUT_NAME environment variable is not set
    exit 1
fi

cd features/fixtures/app_agp340
./gradlew :module:clean :module:build -x lint uploadBugsnag${VARIANT_OUTPUT_NAME}Mapping --stacktrace
