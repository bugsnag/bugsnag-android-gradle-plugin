#!/usr/bin/env bash

cd features/fixtures/rnapp/android
npm install
./gradlew :app:clean :app:build -x lint --stacktrace
