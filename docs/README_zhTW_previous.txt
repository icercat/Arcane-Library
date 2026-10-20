ATM0 Arcane Library 0.2.0-exp1 — 測試候選版
======================================================
Minecraft 1.7.10 Forge。以 0.1.2-exp3-listui JAR 進行離線位元碼修改，並新增 Java 8 相容的 V2Hooks 邏輯層。

已封裝：
- 原方塊/TileEntity 註冊 ID 保持不變。
- 雙圖書館支援 Shift 點擊背包物品快速存入；使用標準 Container/Slot 協議，預計相容 Mouse Tweaks，但尚未實機測試。
- 附魔提取不消耗 XP，沒有 Tier；UI 中選附魔等級並按 Add 可排隊多種附魔，按 Take 組成一本 StoredEnchantments 附魔書。
- 藥水以單一效果均質化入庫，額外生成 Glowstone/Redstone/Gunpowder 三種 Charge；拒收多效果藥水。
- 提取藥水不需要玻璃瓶，直接輸出新的完整藥水物品。選 Normal/Strong/Long/Splash（互斥的強度/時長，飛濺可搭配）並按 Take。
- 原始 Enchantments NBT 格式維持，舊 Potions NBT 中可識別的單效果成品轉入新 Essence/Charge 方案；無法安全轉換的舊資料仍保留於原 NBT。
- TileEntity 新增 1.7.10 的資料更新封包回呼，修復 Client/Server 畫面不同步。
- GUI 176x230 像素，自製像素材質及拆分 PNG 元件，不再引用 crafting_table.png。

目前限制：
- 尚未在完整的 Minecraft 1.7.10 Forge / ATM0-NH 中啟動執行；測試範圍為離線 ASM 結構驗證與虛擬 API 功能測試。
- GUI 為臨時可替換的原創材質，不是 Sotnah 原始 binary PNG；保留類似布局。若要完全一致，仍需把 Sotnah library.png 放進專案，並調整 GUI 圖集座標。
- GUI 仍使用 5 個可翻頁的按鈕，沒有完整拖曳式 scroll-bar / 搜尋篩選。
- 第三個 Filter 槽與隨滑鼠拖曳連續投料未通過實測。
- 準確的模組藥水 NBT 相容性未全部驗證；目前只接受能按 1.7.10 原版 ItemPotion 正常轉換的單效果資料。
- 機器物品空間、裝備相容、同時多人高延遲下仍須驗收。

使用：
1. 備份存檔、原 mods 資料夾與 0.1.2-exp3-listui.jar。
2. 不可同時安裝兩個版本：先移出舊版 JAR，再放入 0.2.0-exp1。
3. 先用乾淨測試世界測試，再測試原世界的備份副本。
4. 建議先在本地單人世界及獨立 Server 確認功能、NBT 持久化以及 Console log，確認無誤再部署正式伺服器。

離線驗證：
- ASM BasicVerifier: 187 methods / 0 failed。
- Mock API 測試通過：普通/強化/飛濺藥水均質化、Charge 產生與消耗、多效果拒收、NBT 持久化、舊資料遷移、混合附魔提取。

建置方式：
- 需要 JDK 21（僅供離線 patch 工具）及 JDK 8 目標的 V2Hooks，離線 patch 工具使用 JDK 內建 ASM（--add-exports）。
- V2Hooks 使用純 Java 反射，不需在編譯時直接引用 Minecraft API，但在遊戲執行時需要 Minecraft 1.7.10 + Forge。
