#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$ROOT/build/kotlin"
OUT_JAR="$BUILD_DIR/live-coding-companion.jar"

mkdir -p "$BUILD_DIR"
kotlinc "$ROOT"/kotlin/src/main/kotlin/learning/livecoding/kotlin/*.kt -include-runtime -d "$OUT_JAR"
java -jar "$OUT_JAR" "${1:-list}"
