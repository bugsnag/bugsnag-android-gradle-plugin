#!/usr/bin/env bash

cd $APP_FIXTURE_DIR
echo "Test fixture used: $APP_FIXTURE_DIR"
./gradlew :module:clean :module:bundleFoo -x lint --stacktrace
