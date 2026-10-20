JDK 21 required for build tooling; Java 8 bytecode for V2Hooks. See root README.

## 0.2.1

The patch now injects Forge 1.7.10 `removedByPlayer`, `getDrops`, `harvestBlock`, `canSilkHarvest`, and `onBlockPlacedBy` into **both** library block classes. `V21BlockHooks` creates one NBT-bearing block item, validates its type on placement and restores *storage only*. Run `bash tests/run_mock_tests.sh` after rebuilding to test a survival drop/restore cycle, creative break handling and potion Essence/Charge preservation.
