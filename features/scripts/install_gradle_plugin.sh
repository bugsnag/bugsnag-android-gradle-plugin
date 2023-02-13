#!/usr/bin/env bash
set -e

./gradlew build publishToMavenLocal -x groovyDoc -x detekt -x test -PVERSION_NAME=9000.0.0-test
