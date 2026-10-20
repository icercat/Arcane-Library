#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$ROOT/build/testclasses"
mapfile -t STUBS < <(find "$ROOT/tests/stubs" -name '*.java' | sort)
javac --release 8 -d "$ROOT/build/testclasses" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V2Hooks.java" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V21BlockHooks.java" "${STUBS[@]}"
java -cp "$ROOT/build/testclasses" TestV2
java -cp "$ROOT/build/testclasses" TestNBTDrop
