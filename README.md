# ATM0 Arcane Library

Minecraft **1.7.10 / Forge**，目前開發版本 **0.2.1**。使用者專案 ATM0-NH。

**版本下載**

- [ATM0-Arcane-Library-0.2.1.jar](dist/ATM0-Arcane-Library-0.2.1.jar)
- [ATM0-Arcane-Library-0.2.1-source.zip](dist/ATM0-Arcane-Library-0.2.1-source.zip)
- [v0.2.1 更新說明](docs/CHANGELOG_0.2.1.md)

> 本版本仍是測試候選版（非已實機驗證的正式穩定版）。請先備份 Minecraft 世界，移除舊版 Arcane Library JAR，勿在 `mods` 中同時安裝兩版。

## 0.2.1：打掉保留 NBT，再次放置恢復

附魔與藥水圖書館都能在**正常生存模式挖掘**時，掉落**恰好一個**帶 NBT 的原方塊物品。重新放置會還原原先儲存的內容：

- 附魔書庫：所有附魔 ID、累積點數、最高學習等級。
- 藥水庫：基礎效果 Essence、Glowstone / Redstone / Gunpowder Charge，以及暫存的舊版本藥水記錄。
- NBT 使用 `ATM0LibraryData` 包裝（含 Format、Kind、Storage），放置時驗證方塊類型與資料格式。
- 1.7.10 Forge 的 `removedByPlayer` 延後清除 TileEntity，`getDrops` 把儲存內容寫入掉落物，`harvestBlock` 結束後才真正移除；故絲綢之觸採相同 `getDrops` 分支。
- 創造模式破壞不產出額外帶資料物品；放置時不複製座標、方塊 ID 或其他原 TileEntity 身份資料。
- 不新增方塊、TileEntity 或物品的註冊 ID；與先前 `0.1.2`/`0.2.0` 的儲存格式相容。

## 延續的 0.2.0 功能

- 附魔書 Shift 快速存入、組合多個附魔到單本書，不消耗經驗，不分書櫃 Tier。
- 藥水效果均質化、三種 Charge、拒收多效果藥水，輸入消耗瓶子、輸出不須另備瓶子。
- 自訂 UI 素材；目前 GUI 清單與捲動仍為測試階段。

## 專案架構

```text
src/main/java/dev/frostey/arcanelibrary/v2/
  V2Hooks.java                      0.2.0 功能與存檔資料邏輯
  V21BlockHooks.java                0.2.1 掉落／放置的 NBT 轉移邏輯
src/main/resources/assets/          GUI 材質
vendor/                             0.1.2 基底 JAR（重建必需）
tools/PatchJar.java                 ASM 位元碼注入原本 1.7.10 類別
tools/VerifyJar.java                位元碼結構檢查
scripts/build.sh, scripts/build.ps1  建置候選 JAR
tests/run_mock_tests.sh             模擬 API 執行測試
tests/stubs/                        離線 Forge/Minecraft Mock 類別
```

**原始碼範圍：** 這是「原版 JAR + ASM 修改 + Java 新功能」的 **patch-based source**，不是完整還原的 ForgeGradle 專案。原始 GUI、Container、TileEntity 仍由 `vendor/` 內的舊版基底 JAR 提供；新增功能與位元碼修改可在 `src/main/java/`、`tools/` 中檢視。長期建議另行還原為完整 Forge 專案。

## 編譯與測試

需 JDK 21 執行 patcher（使用 `jdk.internal.org.objectweb.asm`），新加的 Hooks 會以 **Java 8 bytecode** 編譯，以配合 Minecraft 1.7.10 執行環境。

Linux/macOS：

```bash
bash scripts/build.sh
bash tests/run_mock_tests.sh
```

Windows：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build.ps1
```

編譯後位置 `build/ATM0-Arcane-Library-0.2.1.jar`。

**測試範圍：** JAR 位元碼結構檢驗和模擬 Minecraft API 的 NBT／掉落流程已通過。**尚未驗證**完整 Forge 1.7.10 啟動、多人連線、Modpack 中其他模組的自訂破壞流程和 Mouse Tweaks 真實手感；完成前請勿將資料唯一副本放入機器。

## 授權

自製素材在 `src/main/resources/assets/`；未包含 Sotnah 或 Apothic Enchanting 原始 PNG。若使用第三方素材，請先確認其再散布權利。
