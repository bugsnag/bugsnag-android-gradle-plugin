#!/usr/bin/env bash

./gradlew build install -x groovyDoc -x codenarcMain -x codenarcTest -x test -PlocalVersion=9000.0.0-test
