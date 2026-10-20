# 0.2.1 — Portable Library NBT

## Added

- Enchantment Library and Potion Library retain their storage inside their dropped block item NBT.
- Replacing a dropped block restores all supported storage data.
- `ATM0LibraryData: { Format: 1, Kind: "enchantment"|"potion", Storage: { ... } }` keeps storage independent from placed tile coordinates.
- Delayed tile removal for survival harvesting, via Forge 1.7.10 `removedByPlayer` hook, NBT-aware `getDrops`, `harvestBlock` final removal, normal `onBlockPlacedBy` restoration.
- Silk Touch takes the same normal NBT-aware drops path; creative break does not emit extra stored blocks.
- New test harness `TestNBTDrop`: enchant/potion round-trip, one drop only, no wrong-type restore, no saved tile coordinates, creative branch.

## Unchanged

- Registered blocks and tiles, existing NBT keys, old 0.2.0 features, existing UI sprites.
- No extra bottle requirement; no XP or tier requirement for enchant library.

## Known limitations

- Forge/ATM0-NH in-game integration has **not** been run. The test suite uses mock world and NBT APIs; protect your save before testing.
- The existing v0.2.0 interface has known navigation and client/server limitations; this patch addresses *portable storage*, not a complete UI rework.
- Unusual mod-specific breaking/movement methods that bypass vanilla/Forge drops and placement must be verified individually.
