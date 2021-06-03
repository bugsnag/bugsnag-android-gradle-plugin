#!/usr/bin/env bash
set -ex

pushd "$RN_FIXTURE_DIR"
npm i --silent
popd

pushd "$RN_MONOREPO_FIXTURE_DIR"
# The monorepo setup uses Yarn workspaces so we use Yarn over NPM here
yarn install --silent
popd
