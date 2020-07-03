#!/usr/bin/env bash

cd $RN_FIXTURE_DIR
echo "Test fixture used: $RN_FIXTURE_DIR"
npm install
./gradlew :app:clean :app:assemble -x lint --stacktrace
