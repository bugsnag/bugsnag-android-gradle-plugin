#!/usr/bin/env bash

./gradlew build install -x groovyDoc -x detekt -x test -PlocalVersion=9000.0.0-test
