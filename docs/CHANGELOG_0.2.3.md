# ATM0 Arcane Library 0.2.3 — Wider Library UI and hover details

## Layout

- Widened both GUI backgrounds from 176×230 to 252×230 logical pixels; unchanged vertical height.
- Enlarged the five-row enchantment/potion list from 113px to 189px per row.
- Expanded Sotnah's UI assets without resampling or replacing the decorative borders; source pixels preserved by a seam-expansion method. Added `library_wide.png` (512×256 atlas) and asset generation script `tools/make_wide_atlas.py`.
- Moved the filter/input/output hitboxes from X=143 to X=219; re-centered the real 27-slot player inventory and 9-slot hotbar at X+38. Note that slot positioning modifies **actual container Slot coordinates**, not just the background texture.
- Spread potion-mode buttons across the 252px width.
- Removed the old GUI Java method's *hard-coded 14-character truncation*. Long names scale to the available row width; hovered tooltips always use their complete unmodified name.

## Hover tooltip

- Enchantments: **full name**, **highest unlocked level**, **exact stored points** (64-bit, no abbreviated rounding).
- Potions: **full name**, **stored Essence quantity**, base/strong level, plus current global Glowstone/Redstone/Gunpowder Charge balances.
- Hover details render after the normal Container GUI to avoid being covered by slots/items, and only when the mouse is within an occupied list row.

## Preserved

- Direct output-slot pickup (no Take button), mixed enchanted books, potion homogenization, no XP/tier requirements.
- NBT-preserving survival harvest and replacement from 0.2.1.

## Validation

- `bash scripts/build.sh`: bytecode structural verification (298 methods, 0 failures) plus slot/gui coordinate checks.
- `bash tests/run_mock_tests.sh`: potion storage/mixing, NBT harvest/replace, tooltip lookup for enchantment and potion, full names and 64-bit counts.
- GUI rendering and Mouse Tweaks interactions still require an actual Minecraft 1.7.10 Forge session for final confirmation.
