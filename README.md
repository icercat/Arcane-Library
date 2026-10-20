# ATM0 Arcane Library

Minecraft **1.7.10 / Forge** 專案，開發基準為 `ATM0-Arcane-Library-0.1.2-exp3-listui.jar`。

目前 GitHub 提供 **0.2.0-exp1 實驗版**，包含已編譯 JAR、可檢視的修補程式原始碼、測試與重建腳本，以及建置所需的 0.1.2 基底 JAR。

> **重要：這是一份「原版 JAR + ASM 修改 + Java 新功能」的修補式程式碼，不是能直接以 ForgeGradle 編譯的完整原始碼專案。** 完整的舊版 GUI、Container、TileEntity 仍來自 `vendor/` 內的基底 JAR，而新增與修改邏輯可見於 `src/main/java/`、`tools/PatchJar.java`。不應將這份程式碼描述為已完成原生重寫。

## 下載

- [ATM0-Arcane-Library-0.2.0-exp1.jar](dist/ATM0-Arcane-Library-0.2.0-exp1.jar) — 可放入 mods 的候選版。
- [0.2.0-exp1-source.zip](dist/ATM0-Arcane-Library-0.2.0-exp1-source.zip) — 含建置腳本、源碼、材質、測試、基底 JAR 的打包檔。

請務必先備份世界，將舊版 JAR 移出 `mods`，不要同時放兩版。

## 本版功能

- 附魔書 Shift 快速存入；可在提取時組合多個附魔到同一本書；不收 XP，無 Tier。
- 藥水單效果均質化；Glowstone / Redstone / Gunpowder Charge；拒收多效果藥水；輸入消耗瓶子，輸出自動生成藥水。
- 獨立 GUI 材質，不再載入原版合成台背景。
- 以既有 0.1.2 的物件 ID 與 NBT 資料為基礎做修補。

**尚未驗證：** 完整 Forge 1.7.10 啟動、ATM0-NH 實機多人／伺服器、Mouse Tweaks 快速拖曳、高延遲同步、所有模組藥水 NBT。GUI 仍有暫時的五個翻頁按鈕，不是完成版搜尋／捲軸介面。請勿將此版本視為正式穩定版。

## 目錄

```text
src/main/java/dev/frostey/arcanelibrary/v2/V2Hooks.java  新增功能（Java 8 相容）
src/main/resources/assets/                            拆分的原創 GUI PNG
tools/PatchJar.java                                    ASM 位元碼修補器
tools/VerifyJar.java                                   ASM 驗證工具
tools/draw_assets.py                                  材質生成腳本
tests/mock_test.py                                     測試虛擬 API 建立器
tests/stubs/                                          離線測試的 Java stub
vendor/ATM0-Arcane-Library-0.1.2-exp3-listui.jar       建置基底
scripts/build.sh                                      Linux/macOS 建置腳本
scripts/build.ps1                                     Windows PowerShell 建置腳本
dist/ATM0-Arcane-Library-0.2.0-exp1.jar               已編譯候選版
```

## 重新編譯（JDK 21）

Linux/macOS：`bash scripts/build.sh`  
Windows PowerShell：`powershell -ExecutionPolicy Bypass -File scripts/build.ps1`

重建工具需 JDK 21（ASM 使用 `jdk.internal.org.objectweb.asm` 的匯出旗標）；遊戲執行端需要與 Minecraft 1.7.10 相容的 Java 環境。`V2Hooks.java` 以 Java 8 bytecode 目標編譯。

這些腳本只依賴本 repo 所包含的基底 JAR 與 JDK，不需要 ForgeGradle。這種方式適合維護實驗版；長期仍應將完整舊程式反編譯還原成正常 ForgeGradle 原始碼工程。

## 授權與來源

本版自製 GUI 素材位於 `src/main/resources/assets/`。未嵌入 Sotnah / Apothic Enchanting 原始 GUI PNG。若未來替換成第三方材質，請先確認其再散布權利並保留必要署名。舊版基底 JAR 與你自己的程式碼授權應另行核對，不以第三方 MIT 授權覆蓋它們。
