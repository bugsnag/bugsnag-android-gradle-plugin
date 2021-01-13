#!/usr/bin/env bash
set -e

./gradlew install -x detekt -x test -PlocalVersion=9000.0.0-test
