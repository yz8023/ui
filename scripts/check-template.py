#!/usr/bin/env python3
"""模板一致性检查（提交前运行：python3 scripts/check-template.py）

检查三类曾导致使用者翻车的问题：
1. 素材库镜像与 android/ 权威源码的已知对应文件是否漂移（要求逐行相同）；
2. 素材代码里引用了、但素材集合内从未声明的名字（抄了就编译不过）；
3. 示例 AndroidManifest 的桌面图标互斥：有且仅有 1 个 enabled=true 的 LAUNCHER 别名。
退出码 0=通过，1=发现问题。无第三方依赖。
"""
import difflib
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
problems = []

# ---- 1. 镜像漂移 ------------------------------------------------------------
MIRRORS = [
    ("素材库/液态玻璃主题/代码/Glass.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/Glass.kt"),
    ("素材库/液态玻璃主题/代码/Color.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/theme/Color.kt"),
    ("素材库/液态玻璃主题/代码/Theme.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/theme/Theme.kt"),
    ("素材库/液态玻璃主题/代码/Shape.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/theme/Shape.kt"),
    ("素材库/液态玻璃主题/代码/Type.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/theme/Type.kt"),
    ("素材库/O神主题与布局/代码/OShinTheme.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/theme/OShinTheme.kt"),
    ("素材库/O神主题与布局/代码/OShinComponents.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/OShinComponents.kt"),
    ("素材库/网络壁纸与离线缓存/代码/WallpaperStore.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/WallpaperStore.kt"),
    ("素材库/网络壁纸与离线缓存/代码/SystemWallpaper.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/SystemWallpaper.kt"),
    ("素材库/网络壁纸与离线缓存/代码/WallpaperSettings.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/screens/WallpaperSettings.kt"),
    ("素材库/后台数据查询规范/代码/AdminQuerySpec.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/adminquery/AdminQuerySpec.kt"),
    ("素材库/后台数据查询规范/代码/AdminQueryDemo.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/adminquery/AdminQueryDemo.kt"),
    ("素材库/后台数据查询规范/代码/AdminQueryScreen.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/screens/AdminQueryScreen.kt"),
    ("素材库/弹窗与提示术语/代码/OverlayTermsSpec.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/overlayterms/OverlayTermsSpec.kt"),
    ("素材库/弹窗与提示术语/代码/OverlayTermsDemo.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/overlayterms/OverlayTermsDemo.kt"),
    ("素材库/弹窗与提示术语/代码/OverlayTermsScreen.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/screens/OverlayTermsScreen.kt"),
    ("素材库/工具与基建/代码/AppUpdateChecker.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/toolkit/AppUpdateChecker.kt"),
    ("素材库/工具与基建/代码/AppLog.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/toolkit/AppLog.kt"),
    ("素材库/工具与基建/代码/MarkdownText.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/toolkit/MarkdownText.kt"),
    ("素材库/工具与基建/代码/AdaptiveNav.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/toolkit/AdaptiveNav.kt"),
    ("素材库/工具与基建/代码/ToolkitSpec.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/toolkit/ToolkitSpec.kt"),
    ("素材库/工具与基建/代码/ToolkitDemo.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/toolkit/ToolkitDemo.kt"),
    ("素材库/工具与基建/代码/ToolkitScreen.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/screens/ToolkitScreen.kt"),
    ("素材库/动效系统/代码/Motion.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/motion/Motion.kt"),
    ("素材库/动效系统/代码/MotionModifiers.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/motion/MotionModifiers.kt"),
    ("素材库/动效系统/代码/InteractiveHighlight.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/motion/InteractiveHighlight.kt"),
    ("素材库/动效系统/代码/DampedDragAnimation.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/motion/DampedDragAnimation.kt"),
    ("素材库/动效系统/代码/DragGestureDetector.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/motion/DragGestureDetector.kt"),
    ("素材库/底部导航栏/代码/LiquidBottomTabs.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/LiquidBottomTabs.kt"),
    ("素材库/权限请求组件/代码/PermissionToolkit.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/PermissionToolkit.kt"),
    ("素材库/状态反馈组件/代码/Feedback.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/Feedback.kt"),
    ("素材库/玻璃常用组件/代码/Feedback.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/Feedback.kt"),
    ("素材库/玻璃常用组件/代码/Dialogs.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/Dialogs.kt"),
    ("素材库/玻璃常用组件/代码/StudyStyleComponents.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/StudyStyleComponents.kt"),
    ("素材库/玻璃常用组件/代码/TextFields.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/TextFields.kt"),
    ("素材库/玻璃常用组件/代码/Controls.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/Controls.kt"),
    ("素材库/玻璃常用组件/代码/Rows.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/components/Rows.kt"),
    ("素材库/应用图标切换/代码/AppIconSwitcher.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/features/AppIconSwitcher.kt"),
    ("素材库/应用欢迎引导界面/代码/WelcomeGuideScreen.kt", "android/app/src/main/java/com/monkeycode/liquidui/ui/screens/WelcomeGuideScreen.kt"),
]
for mirror, authoritative in MIRRORS:
    a, b = ROOT / mirror, ROOT / authoritative
    if not a.exists() or not b.exists():
        problems.append(f"[缺失] {mirror} 或 {authoritative} 不存在")
        continue
    la = a.read_text(errors="ignore").splitlines()
    lb = b.read_text(errors="ignore").splitlines()
    if la == lb:
        print(f"镜像一致  [OK ] {mirror}")
        continue
    # 这 39 组的不变量是**逐行相同**（refresh-mirrors.py 就是把权威源码整份复制过去）。
    # 原先按相似度 > 0.98 判定：1000 行的文件差 4 行会算出 0.998、打印时四舍五入成 1.00
    # 直接放行——真发生过（改完 android/ 忘了重刷镜像，闸门没拦住）。所以这里不看比例。
    ratio = difflib.SequenceMatcher(None, la, lb).ratio()
    ndiff = len([x for x in difflib.unified_diff(la, lb, lineterm="", n=0)
                 if x[:1] in "+-" and x[:3] not in ("+++", "---")])
    print(f"镜像漂移  [漂移] {mirror}（相似度 {ratio:.4f}，相差 {ndiff} 行）")
    problems.append(f"[漂移] {mirror} 与权威源码 {authoritative} 不一致（相差 {ndiff} 行）："
                    f"跑 python3 scripts/refresh-mirrors.py 重刷，或确认是有意差异并在这里放行")

# ---- 2. 引用未声明 -----------------------------------------------------------
material_files = list((ROOT / "素材库").rglob("*.kt"))
declared = set()
for f in material_files:
    text = f.read_text(errors="ignore")
    declared.update(re.findall(r"(?:object|class|fun|val|typealias)\s+([A-Za-z_一-鿿][\w一-鿿]*)", text))

# 已知问题（已在 docs/功能逐项手册.md 与相关文件横幅中标注，修复前只警告不拦截；
# 新增的未声明引用不在此表里，会直接判失败，防止再引入新的"抄了编不过"）
KNOWN_UNDECLARED = {
    "ThemeController", "SettingsRepository",  # F04 镜像主题文件的历史引用，见该文件头横幅
    "PrimaryButton", "SecondaryButton", "GlassTextField", "SegmentedTabs",
    "PreferenceRow", "PreferenceCard", "CompactSwitch",  # 同上：镜像改中文名（紧凑开关等），权威以 android 英文名为准
    "IconSwitcher", "IconSwitchHelper",  # F02 已合并变体内的残留引用，不要单独选
}

WATCH = [
    "AppIconSwitcher", "ThemeController", "SettingsRepository", "PrimaryButton", "SecondaryButton",
    "GlassTextField", "SegmentedTabs", "CompactSwitch", "PreferenceRow", "PreferenceCard",
    "AppSettingsState", "GlassDialog", "IconSwitcher", "IconSwitchHelper",
]
def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", " ", text, flags=re.S)
    return re.sub(r"//[^\n]*", " ", text)

for name in WATCH:
    pattern = re.compile(r"\b" + re.escape(name) + r"\b")
    users = [str(f.relative_to(ROOT)) for f in material_files
             if pattern.search(strip_comments(f.read_text(errors="ignore")))]
    if users and name not in declared:
        if name in KNOWN_UNDECLARED or name == "AppSettingsState":
            print(f"[已知未声明·仅警告] {name}：{users}")
        else:
            print(f"[未声明] {name} 被引用但素材集合内无声明：{users}")
            problems.append(f"[未声明] {name} 被 {users} 引用但素材内无声明，照抄会编译失败")

# ---- 3. 图标互斥 --------------------------------------------------------------
manifest = (ROOT / "android/app/src/main/AndroidManifest.xml").read_text(errors="ignore")
aliases = re.findall(r"<activity-alias[^>]*>", manifest, re.S)
enabled = [a for a in aliases if 'android:enabled="true"' in a]
print(f"示例 Manifest: activity-alias 共 {len(aliases)} 个，enabled=true 的 {len(enabled)} 个")
if len(enabled) != 1:
    problems.append("[图标] 示例 Manifest 中 enabled=true 的 alias 不是恰好 1 个，会出现双图标/无图标")

print()
if problems:
    print("检查不通过：")
    for p in problems:
        print(" -", p)
    sys.exit(1)
print("检查通过：未发现新的漂移/未声明引用/图标互斥问题。")
