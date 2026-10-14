# F03 液态玻璃提示词（宿主已是 Compose 时用）

```
按模板仓库 yz8023/ui 的 F03 给我的 Compose 项目接液态玻璃。先读该仓库 AGENTS.md 和
docs/功能逐项手册.md 的 F03 节，并先体检我的工程后回开工表，等我确认再动手。

我的版本现状：【Kotlin / AGP / Compose BOM / compileSdk / minSdk 分别填】

实施要求：
1. 版本对齐模板基准：compileSdk 36、Kotlin 2.3.10、AGP 8.7.3、Compose BOM 2024.10.01、
   backdrop io.github.kyant0:backdrop-android:2.0.0-alpha03、
   shapes io.github.kyant0:shapes-android:1.2.0；gradle.properties 保留
   android.suppressUnsupportedCompileSdk=36。版本达不到就先报告，不要换低版本替代库凑数。
2. 整文件复制权威源码，不改名（类名/函数名保持英文原样，只改 package 与 R）：
   ui/theme/Color.kt、Shape.kt、Type.kt、Theme.kt，ui/components/Glass.kt，
   以及 AppRoot.kt 的双层 backdrop 录制接线（auroraBackdrop + contentBackdrop +
   LocalGlassBackdrop/LocalGlassContentBackdrop）。只抄 Glass.kt 不接根部录制视为未做。
3. 先在首页放一张 GlassCard 验证"能折射身后光斑/壁纸"这一个点，截图或录屏确认后，
   再铺其他页面和 F08/F10。顺序必须是 F03 → F09 → F10 → F08，不许跳步。
4. 弹窗一律用模板的 GlassDialogHost 同窗浮层，禁止用 androidx 的 Dialog 还宣称真玻璃。
5. 设置参数只走 AppSettingsState 的 updateXxx 方法，不要新造 setXxx（会撞 JVM 签名）；
   状态只许有一个来源。

验收（逐项汇报）：
- 编译命令与输出；版本逐项与基准核对结果；
- 玻璃/普通/MD3 三态各一屏的现象说明（滑动时卡片折射内容是否跟着动）；
- 弹窗真玻璃、减少动效下内容可见、API<33 设备的降级项明示（没有低版本设备就写明未测）。
遇到"面板是实心/只有半透明 tint"，先对照手册 F03 失败指纹表，不要自己发明新的玻璃画法。
```
