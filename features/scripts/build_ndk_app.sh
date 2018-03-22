#!/usr/bin/env bash

cd features/fixtures/ndkapp
./gradlew :app:clean :app:build --stacktrace
