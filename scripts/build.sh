#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${1:-$ROOT/build/ATM0-Arcane-Library-0.2.0-exp1.jar}"
mkdir -p "$ROOT/build/patcher" "$ROOT/build/hooks" "$(dirname "$OUT")"
JDK_EXPORT=(--add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED --add-exports java.base/jdk.internal.org.objectweb.asm.tree.analysis=ALL-UNNAMED)
javac --release 8 -d "$ROOT/build/hooks" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V2Hooks.java"
javac "${JDK_EXPORT[@]}" -d "$ROOT/build/patcher" "$ROOT/tools/PatchJar.java" "$ROOT/tools/VerifyJar.java"
java "${JDK_EXPORT[@]}" -cp "$ROOT/build/patcher" PatchJar "$ROOT/vendor/ATM0-Arcane-Library-0.1.2-exp3-listui.jar" "$OUT" "$ROOT/build/hooks" "$ROOT/src/main/resources"
java "${JDK_EXPORT[@]}" -cp "$ROOT/build/patcher" VerifyJar "$OUT"
printf '\nBuilt %s\n' "$OUT"
