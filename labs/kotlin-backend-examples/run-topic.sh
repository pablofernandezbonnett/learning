#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$ROOT_DIR/build"
JAR_PATH="$BUILD_DIR/learning-backend-examples.jar"
SOURCES_FILE="$BUILD_DIR/sources.txt"

mkdir -p "$BUILD_DIR"
find "$ROOT_DIR/src/main/kotlin" -name '*.kt' | sort > "$SOURCES_FILE"

kotlinc \
  -jvm-target 21 \
  @"$SOURCES_FILE" \
  -include-runtime \
  -d "$JAR_PATH"

java -jar "$JAR_PATH" "$@"
