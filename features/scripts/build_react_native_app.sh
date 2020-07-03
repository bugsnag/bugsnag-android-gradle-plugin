#!/usr/bin/env bash

cd features/fixtures/rnapp_agp340/android
npm install
./gradlew :app:clean :app:build -x lint --stacktrace
