#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$ROOT/build/java"
CLASSES_DIR="$BUILD_DIR/classes"
SOURCES_FILE="$BUILD_DIR/sources.txt"

rm -rf "$CLASSES_DIR"
mkdir -p "$CLASSES_DIR"
find "$ROOT/java/src/main/java" -name '*.java' | sort > "$SOURCES_FILE"
javac --release 21 -d "$CLASSES_DIR" @"$SOURCES_FILE"
java -cp "$CLASSES_DIR" learning.livecoding.java.Main "${1:-list}"
