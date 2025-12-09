#!/usr/bin/env bash

set -euo pipefail

# === Variables ===
GRADLE_DIR="$HOME/.gradle"
KEY_FILE="$HOME/temp_key"
KEY_RING="$HOME/publishKey.gpg"
GRADLE_PROPERTIES="$GRADLE_DIR/gradle.properties"

# === GPG Key Setup ===
echo "$KEY" > "$KEY_FILE"
base64 --decode -i "$KEY_FILE" -o "$KEY_RING"

# === Gradle Configuration ===
mkdir -p "$GRADLE_DIR"
{
  echo "signing.keyId=$KEY_ID"
  echo "signing.password=$KEY_PASS"
  echo "signing.secretKeyRingFile=$KEY_RING"
  echo "NEXUS_USERNAME=$PUBLISH_USER"
  echo "NEXUS_PASSWORD=$PUBLISH_PASS"
  echo "nexusUsername=$PUBLISH_USER"
  echo "nexusPassword=$PUBLISH_PASS"
  echo "mavenCentralUsername=$PUBLISH_USER"
  echo "mavenCentralPassword=$PUBLISH_PASS"
} >> "$GRADLE_PROPERTIES"

# === Build, Publish and Close===
./gradlew clean publishAllPublicationsToSonatypeRepository publishPlugins --no-daemon --max-workers=1

echo "Go to https://central.sonatype.com/ to release the final artefact."
