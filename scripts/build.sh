#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${1:-$ROOT/build/ATM0-Arcane-Library-0.2.3.jar}"
mkdir -p "$ROOT/build/patcher" "$ROOT/build/hooks" "$(dirname "$OUT")"
JDK_EXPORT=(--add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED --add-exports java.base/jdk.internal.org.objectweb.asm.tree.analysis=ALL-UNNAMED)
javac --release 8 -d "$ROOT/build/hooks" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V2Hooks.java" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V21BlockHooks.java" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V22OutputHooks.java" "$ROOT/src/main/java/dev/frostey/arcanelibrary/v2/V22GuiHooks.java"
javac "${JDK_EXPORT[@]}" -d "$ROOT/build/patcher" "$ROOT/tools/PatchJar.java" "$ROOT/tools/VerifyJar.java"
java "${JDK_EXPORT[@]}" -cp "$ROOT/build/patcher" PatchJar "$ROOT/vendor/ATM0-Arcane-Library-0.1.2-exp3-listui.jar" "$OUT" "$ROOT/build/hooks" "$ROOT/src/main/resources"
java "${JDK_EXPORT[@]}" -cp "$ROOT/build/patcher" VerifyJar "$OUT"
# Bytecode regression: slot positions > 127 must be SIPUSH, never overflowing BIPUSH.
SLOTS="$(javap -c -p -classpath "$OUT" dev.frostey.arcanelibrary.container.ContainerEnchantmentLibrary)"
grep -Eq 'sipush[[:space:]]+148' <<< "$SLOTS" || { echo "Main inventory Y=148 missing"; exit 1; }
grep -Eq 'sipush[[:space:]]+219' <<< "$SLOTS" || { echo "Machine X=219 missing"; exit 1; }
GUI="$(javap -c -p -classpath "$OUT" dev.frostey.arcanelibrary.client.GuiEnchantmentLibrary)"
grep -Eq 'sipush[[:space:]]+252' <<< "$GUI" || { echo "GUI width 252 missing"; exit 1; }
grep -Eq 'bipush[[:space:]]+38' <<< "$SLOTS" || { echo "Inventory centered offset missing"; exit 1; }
grep -Eq 'sipush[[:space:]]+206' <<< "$SLOTS" || { echo "Hotbar Y=206 missing"; exit 1; }
printf '\nBuilt %s\n' "$OUT"
