#!/usr/bin/env python3
"""把 Kotlin 里的硬编码中文文案外置到 res/values/strings.xml（分阶段执行）。

为什么要有这个脚本：全仓 2226 条含中文字面量，手改必错。本脚本基于 kt_scan.py 的
上下文判定，只处理**确定安全**的那一类，其余留给后续阶段，宁少勿错。

阶段划分（见 docs 里的分类表）：
  2a  A/C1 类、非 ui2/previews、非「非组合 lambda」   ← 本脚本当前实现
  2b  A/C1 类、ui2/previews 演示页
  3   B/C2 类（普通函数体内与默认参数，需 context.getString 或上提）
  4   D 类（带 ${} 插值，需转 %1$s 格式参数）
  5   C3 类（顶层/数据类属性，需把模型字段改成 @StringRes）

命名约定：
  common_<语义>            跨 ≥2 个文件重复出现的文案，全仓共用一条
  <文件 slug>_<角色>_<序号>  其余按「所在文件 + 具名参数角色」派生，序号在该组合内递增
角色取自代码里的具名参数（title/subtitle/text/label/contentDescription…），
取不到时按所属调用名归一化，最终兜底为 text。

用法：python3 scripts/externalize-strings.py [--dry]
"""
import collections
import json
import pathlib
import re
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / 'android/app/src/main/java/com/monkeycode/liquidui'
STRINGS = ROOT / 'android/app/src/main/res/values/strings.xml'
DRY = '--dry' in sys.argv

# ---- 文件 → slug -------------------------------------------------------------
SLUG = {
    'ui/features/overlayterms/OverlayTermsDemo.kt': 'ot_demo',
    'ui/features/overlayterms/OverlayTermsScreen.kt': 'overlay_terms',
    'ui/features/adminquery/AdminQueryDemo.kt': 'aq_demo',
    'ui/features/toolkit/ToolkitDemo.kt': 'tk_demo',
    'ui/features/toolkit/AppLog.kt': 'app_log',
    'ui/features/toolkit/AppUpdateChecker.kt': 'update',
    'ui/features/toolkit/MarkdownText.kt': 'markdown',
    'ui/features/toolkit/AdaptiveNav.kt': 'adaptive_nav',
    'ui/features/PermissionToolkit.kt': 'permission',
    'ui/features/AppIconSwitcher.kt': 'app_icon',
    'ui/features/ui2/Ui2PreviewHost.kt': 'ui2_host',
    'ui/screens/SettingsScreen.kt': 'settings',
    'ui/screens/ComponentsScreen.kt': 'components',
    'ui/screens/InfoScreens.kt': 'info',
    'ui/screens/ToolkitScreen.kt': 'toolkit',
    'ui/screens/WallpaperSettings.kt': 'wallpaper',
    'ui/screens/WelcomeGuideScreen.kt': 'welcome',
    'ui/screens/MotionScreen.kt': 'motion',
    'ui/screens/Ui2Gallery.kt': 'ui2_gallery',
    'ui/screens/AdminQueryScreen.kt': 'admin_query',
    'ui/screens/HomeScreen.kt': 'home',
    'ui/screens/MaterialBrowser.kt': 'material',
    'ui/screens/OnboardingScreen.kt': 'onboarding',
    'ui/components/Feedback.kt': 'feedback',
    'ui/components/Dialogs.kt': 'dialog',
    'ui/components/Controls.kt': 'controls',
    'ui/components/Rows.kt': 'rows',
    'ui/components/TextFields.kt': 'text_field',
    'ui/components/StudyStyleComponents.kt': 'study',
    'ui/components/LiquidBottomTabs.kt': 'bottom_tabs',
}

# ---- 跨文件共用文案（人工定名，语义优先）--------------------------------------
COMMON = {
    '全部': 'all', '通知': 'notification', '分类': 'category', '设置': 'settings',
    '清空': 'clear', '重试': 'retry', '取消': 'cancel', '确定': 'confirm', '确认': 'confirm',
    '导出': 'export', '复制全文': 'copy_all', '系统分享': 'share', '外观': 'appearance',
    '浅色': 'light', '深色': 'dark', '隐私说明': 'privacy', '应用图标': 'app_icon',
    '处理中…': 'processing', '检查中…': 'checking', '取消下载': 'cancel_download',
    '下载 APK 并安装': 'download_install', '重新下载并安装': 'redownload_install',
    '‹ 返回素材库': 'back_to_library', '提示词': 'prompt', '长按复制提示词': 'long_press_to_copy',
    '✓ 已复制，去粘贴给 AI 吧': 'copied_hint', '功能体验（真实实现，可直接操作）': 'demo_section_hint',
    '反例（别这么做）': 'anti_pattern', '素材库': 'library', '壁纸背景': 'wallpaper_bg',
    '液态玻璃': 'liquid_glass', '主题配色': 'theme_palette', '权限管理': 'permissions',
    '上海': 'city_shanghai', '开始使用': 'get_started', '开源仓库': 'open_source',
    'Android 功能素材库': 'library_subtitle', '系统壁纸': 'system_wallpaper', '已复制': 'copied', '复制': 'copy',
    '加载中': 'loading', '知道了': 'got_it', '关闭': 'close', '返回': 'back', '更多': 'more',
    '暂无': 'empty', '删除': 'delete', '保存': 'save', '完成': 'done', '下一步': 'next',
    '上一步': 'prev', '搜索': 'search', '筛选': 'filter', '今日': 'today', '昨日': 'yesterday',
}

# ---- 角色归一化 ---------------------------------------------------------------
ROLE = {
    'title': 'title', 'subtitle': 'subtitle', 'text': 'text', 'label': 'label',
    'contentDescription': 'content_desc', 'sectionTitle': 'section_title',
    'message': 'message', 'description': 'desc', 'hint': 'hint',
    'confirmText': 'confirm', 'dismissText': 'dismiss', 'placeholder': 'placeholder',
    'name': 'name', 'value': 'value', 'caption': 'caption', 'headline': 'headline',
    'note': 'note', 'empty': 'empty', 'summary': 'summary', 'body': 'body',
}


RISK_BEFORE = re.compile(
    r'(?:==|!=|===|!==|\bin\b|!in)\s*$'                                    # 比较操作数
    r'|(?:mutableStateOf|mutableStateListOf|mutableStateMapOf|mutableFloatStateOf|'
    r'mutableIntStateOf)\s*\(\s*$'                                          # 状态初值
    r'|\b(?:listOf|mapOf|setOf|arrayOf)\s*\(\s*$', re.X)                   # 先放行，下面按后置符号再判
RISK_AFTER = re.compile(r'^\s*(?:->|\bto\b)')                                # when 分支 / map 键值对
STATE_BEFORE = re.compile(r'(?:mutableStateOf|mutableStateListOf|mutableStateMapOf|'
                          r'mutableFloatStateOf|mutableIntStateOf)\s*\(\s*$')
CMP_BEFORE = re.compile(r'(?:==|!=|===|!==)\s*$|\b(?:in|!in)\s+$')


def risky(src: str, r: dict) -> str:
    """返回跳过原因；空串表示可安全外置。

    这几类不是 UI 文案，外置会改语义：
      1. 比较操作数：`r.status == "异常"`——数据侧仍是硬编码中文，换语言即失配；
      2. when 分支：`"全部" -> …`；
      3. map 键：`mapOf("异常" to color)`；
      4. 状态初值：`remember { mutableStateOf("全部") }`（且 lambda 非组合上下文）。
    """
    before = src[max(0, r['start'] - 60):r['start']]
    after = src[r['end']:r['end'] + 12]
    if CMP_BEFORE.search(before):
        return '比较/包含操作数'
    if RISK_AFTER.match(after):
        return 'when 分支或 map 键'
    if STATE_BEFORE.search(before):
        return '状态初值'
    if r.get('interp'):
        return '含插值（留给 D 阶段）'
    if r.get('noncomp'):
        return '非组合 lambda'
    return ''

def is_interp(lit: str) -> bool:
    """字面量是否含真实插值。${'$'} 是仓库里用来输出字面 $ 的转义，不算插值。"""
    s = lit.replace("${'$'}", '')
    return re.search(r'\$[A-Za-z_{]', s) is not None

def role_of(arg: str) -> str:
    if arg in ROLE:
        return ROLE[arg]
    # 误降首字母的私有组合函数（OTNote→oTNote 等）与 listOf/stateOf 一律归为 text
    return 'text'


def xml_escape(text: str):
    need_raw = False
    t = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    t = t.replace("'", "\\'").replace('"', '\\"')
    t = t.replace('\n', '\\n')
    if '%' in text:
        need_raw = True           # 含 % 的一律标 formatted="false"，避免被当格式串
    return t, need_raw


def add_imports(src: str) -> str:
    if 'import androidx.compose.ui.res.stringResource' not in src:
        ms = list(re.finditer(r'^import androidx\.compose\.[^\n]*\n', src, re.M))
        if ms:
            src = src[:ms[-1].end()] + 'import androidx.compose.ui.res.stringResource\n' + src[ms[-1].end():]
        else:
            src = re.sub(r'^(package [^\n]*\n)', r'\1\nimport androidx.compose.ui.res.stringResource\n', src, 1, re.M)
    if 'import com.monkeycode.liquidui.R' not in src:
        ms = list(re.finditer(r'^import com\.monkeycode\.[^\n]*\n', src, re.M))
        if ms:
            src = src[:ms[0].start()] + 'import com.monkeycode.liquidui.R\n' + src[ms[0].start():]
        else:
            src = re.sub(r'^(package [^\n]*\n)', r'\1\nimport com.monkeycode.liquidui.R\n', src, 1, re.M)
    return src


def main():
    recs = json.load(open('/tmp/scan_all.json'))
    srcs = {}
    skipped = collections.Counter()
    stage = []
    for r in recs:
        if r['cat'] not in ('A 组合体内', 'C1 组合默认参数'):
            continue
        if '/ui2/previews/' in r['file']:
            continue
        if r['file'] not in srcs:
            srcs[r['file']] = (SRC / r['file']).read_text(encoding='utf-8')
        why = risky(srcs[r['file']], r) or ('含插值（留给 D 阶段）' if is_interp(r['lit']) else '')
        if why:
            skipped[why] += 1
            continue
        stage.append(r)
    print('本阶段跳过：' + '，'.join(f'{k} {v} 条' for k, v in skipped.most_common()))
    # 跨文件重复 → common_*
    files_of = {}
    for r in stage:
        files_of.setdefault(r['text'], set()).add(r['file'])
    names, used = {}, {}
    shared_n = [0]
    counters = {}
    entries = []          # (name, text, group)
    for r in sorted(stage, key=lambda x: (x['file'], x['start'])):
        txt = r['text']
        if txt in names:
            r['res'] = names[txt]
            continue
        if len(files_of[txt]) >= 2 and txt in COMMON:
            nm = 'common_' + COMMON[txt]
        elif len(files_of[txt]) >= 2:
            shared_n[0] += 1
            nm = f'common_shared_{shared_n[0]}'
        else:
            slug = SLUG.get(r['file'], pathlib.Path(r['file']).stem.lower())
            role = role_of(r['arg'])
            key = (slug, role)
            counters[key] = counters.get(key, 0) + 1
            nm = f'{slug}_{role}_{counters[key]}'
        # 名字撞车就加后缀
        base, k = nm, 2
        while nm in used and used[nm] != txt:
            nm = f'{base}_{k}'; k += 1
        assert re.match(r'^[a-z][a-z0-9_]*$', nm), f'非法资源名 {nm!r}（{txt[:20]!r}）'
        used[nm] = txt
        names[txt] = nm
        r['res'] = nm
        entries.append((nm, txt, r['file']))

    print(f'待外置 {len(stage)} 条 → {len(entries)} 个资源名（去重 {len(stage)-len(entries)} 条）')
    print(f'  其中 common_* {sum(1 for n,_,_ in entries if n.startswith("common_"))} 个')

    if DRY:
        for n, t, f in entries[:25]:
            print(f'    {n:34s} {t[:40]!r}')
        return

    # ---- 改 Kotlin（按偏移从后往前替换）----
    byfile = {}
    for r in stage:
        byfile.setdefault(r['file'], []).append(r)
    for rel, rs in byfile.items():
        p = SRC / rel
        src = p.read_text(encoding='utf-8')
        for r in sorted(rs, key=lambda x: -x['start']):
            assert src[r['start']:r['end']] == r['lit'], f'{rel}:{r["line"]} 偏移失配'
            src = src[:r['start']] + f"stringResource(R.string.{r['res']})" + src[r['end']:]
        p.write_text(add_imports(src), encoding='utf-8')
    print(f'  改写 Kotlin 文件 {len(byfile)} 个')

    # ---- 写 strings.xml ----
    keep = re.findall(r'<string name="(app_name|nav_[a-z_]+)"[^>]*>.*?</string>',
                      STRINGS.read_text(encoding='utf-8'), re.S)
    head = STRINGS.read_text(encoding='utf-8')
    preserved = re.findall(r'([ \t]*<string name="(?:app_name|nav_[a-z_]+)".*?</string>\n)', head, re.S)
    out = ['<?xml version="1.0" encoding="utf-8"?>\n',
           '<!--\n',
           '  本文件是套壳时改文案的唯一入口。命名约定：\n',
           '    nav_*                 底部导航与页面标题（对应 ui/navigation/Screen.kt）\n',
           '    common_*              跨页面共用文案（取消 / 确认 / 全部 / 提示词 …）\n',
           '    <文件 slug>_<角色>_<序号>  某个页面内部的文案，slug 见 scripts/externalize-strings.py\n',
           '  角色取自代码里的具名参数：title / subtitle / text / label / content_desc …\n',
           '  还有 B/C2/D/C3 四类共约 1250 条尚未外置（顶层数据需先改 @StringRes 模型），\n',
           '  进度见 docs/把本仓库当壳用-套壳上手指南.md 第 8 项。\n',
           '-->\n',
           '<resources>\n']
    out += preserved
    cur = None
    for nm, txt, f in sorted(entries, key=lambda x: (x[2], x[0])):
        if f != cur:
            out.append(f'\n    <!-- ===== {f.split("/java/")[-1]} ===== -->\n')
            cur = f
        esc, raw = xml_escape(txt)
        attr = ' formatted="false"' if raw else ''
        out.append(f'    <string name="{nm}"{attr}>{esc}</string>\n')
    out.append('</resources>\n')
    STRINGS.write_text(''.join(out), encoding='utf-8')
    print(f'  strings.xml 写入 {len(entries)} 条（含既有 app_name/nav_* {len(preserved)} 条）')


if __name__ == '__main__':
    main()
