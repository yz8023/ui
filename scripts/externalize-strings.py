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
STAGE = next((a for a in sys.argv[1:] if a in ('2a', '2b', '3a', '3b')), '2a') \
    if '--stage' in sys.argv else '2a'

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
    'ui/features/ui2/previews/PreviewVideoLabs.kt': 'pv_video',
    'ui/features/ui2/previews/PreviewBaseForm.kt': 'pv_form',
    'ui/features/ui2/previews/PreviewFeedbackData.kt': 'pv_feedback',
    'ui/features/ui2/previews/PreviewNavLayout.kt': 'pv_nav',
    'ui/features/ui2/previews/PreviewMotionPages.kt': 'pv_motion_page',
    'ui/features/ui2/previews/PreviewMotionTools.kt': 'pv_motion_tool',
    'ui/features/ui2/previews/PreviewGlassMotion.kt': 'pv_glass',
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


def state_vars(src: str) -> set:
    """文件里用 remember/mutableStateOf 声明的状态变量名。"""
    return set(re.findall(r'\bva[rl]\s+(\w+)\s+by\s+(?:remember|rememberSaveable|rememberMutableStateOf)', src)) | \
           set(re.findall(r'\bva[rl]\s+(\w+)\s*=\s*(?:remember|rememberSaveable)\s*\{\s*mutable\w+StateOf', src))


def assigns_state(src: str, r: dict, names: set) -> bool:
    """字面量是否处在「给状态变量赋值」的语句里（含 `x = if (…) "…" else "…"`）。"""
    if not names:
        return False
    # 按行回看 3 行：`x = if (c) "…" else "…"` 的 else 分支字面量，
    # 其紧邻前缀是 `else `，只看字符窗口会漏，导致同一表达式一半硬编码一半走资源。
    lines = src[:r['start']].split('\n')
    window = '\n'.join(lines[-4:])
    for v in names:
        if re.search(r'\b' + re.escape(v) + r'\s*=', window):
            return True
    return False


def find_context(src: str, fun: str, pos: int):
    """返回在 pos 处可用的 Context 变量名，没有则 None。

    必须校验**声明位置在字面量之前**：`val context = LocalContext.current` 若写在
    使用点之后，前向引用编译不过。
    """
    m = re.search(r'fun\s+(?:[\w<>.]+\.)?' + re.escape(fun) + r'\s*\(', src)
    if not m:
        return None
    i, d = m.end(), 1
    while i < len(src) and d:
        d += (src[i] == '(') - (src[i] == ')')
        i += 1
    sig = src[m.start():i]
    mp = re.search(r'\b(\w+)\s*:\s*Context\b', sig)
    if mp:
        return mp.group(1)
    name = None
    for mm in re.finditer(r'\bval\s+(\w*[Cc]ontext\w*)\s*=\s*LocalContext\.current\b', src[i:pos]):
        name = mm.group(1)
    return name


def risky(src: str, r: dict, allow_noncomp: bool = False, svars: set = None) -> str:
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
    if svars is not None and assigns_state(src, r, svars):
        return '给状态变量赋文案（应改存 id/枚举）'
    if RISK_AFTER.match(after):
        return 'when 分支或 map 键'
    if STATE_BEFORE.search(before):
        return '状态初值'
    if r.get('interp'):
        return '含插值（留给 D 阶段）'
    if r.get('noncomp') and not allow_noncomp:
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


def xml_entity_only(t: str) -> str:
    """只还原 XML 实体与引号转义，**不动 \\n**。

    这才是 xml_escape() 对普通字符串字面量的精确逆运算：扫描器给的字面量文本里
    `\\n` 本来就是「反斜杠 + n」两个字符，xml_escape 只替换真换行，所以写进 XML 后
    仍是这两个字符。若在这里把它还原成真换行，「文案 → 既有资源名」反查键就错位，
    同一文案会被重复建档（阶段 4 的插值文案转义密集，会大量复制）。
    """
    return (t.replace("\\'", "'").replace('\\"', '"')
             .replace('&lt;', '<').replace('&gt;', '>').replace('&amp;', '&'))


def xml_unescape(t: str) -> str:
    """完整还原（含 \\n → 真换行），用于匹配三引号原始字符串。"""
    return xml_entity_only(t).replace('\\n', '\n')


def load_existing():
    """读现有 strings.xml：{资源名: 文案}、{文案: 资源名}、每个 (slug,角色) 已用到的最大序号。"""
    import xml.etree.ElementTree as ET
    if not STRINGS.exists():
        return {}, {}, {}
    by_name, by_text, counters = {}, {}, {}
    for r in ET.parse(STRINGS).getroot().findall('string'):
        nm = r.get('name'); raw = r.text or ''
        txt = xml_entity_only(raw)
        by_name[nm] = txt
        by_text.setdefault(txt, nm)                 # 普通字符串字面量的表示
        by_text.setdefault(xml_unescape(raw), nm)   # 三引号原始字符串的表示
        m = re.match(r'^(.+)_(text|title|subtitle|label|content_desc|section_title|message|desc|hint|'
                     r'confirm|dismiss|placeholder|name|value|caption|headline|note|empty|summary|body)_(\d+)$', nm)
        if m:
            k = (m.group(1), m.group(2))
            counters[k] = max(counters.get(k, 0), int(m.group(3)))
    return by_name, by_text, counters


def xml_escape(text: str):
    need_raw = False
    t = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
    t = t.replace("'", "\\'").replace('"', '\\"')
    t = t.replace('\n', '\\n')
    if '%' in text:
        need_raw = True           # 含 % 的一律标 formatted="false"，避免被当格式串
    return t, need_raw


def fun_body_start(src: str, fun: str):
    """返回 fun 函数体 `{` 之后的偏移；表达式体（`= …`）或找不到则 None。"""
    for m in re.finditer(r'fun\s+(?:<[^>]*>\s*)?(?:[\w<>.]+\.)?' + re.escape(fun) + r'\s*\(', src):
        i, d = m.end(), 1
        while i < len(src) and d:
            d += (src[i] == '(') - (src[i] == ')')
            i += 1
        seg = src[i:i + 300]
        mb = re.search(r'[={]', seg)
        if mb and mb.group(0) == '{':
            return i + mb.end()
    return None


def val_name(res: str, taken: set) -> str:
    """资源名转 camelCase 变量名，撞名加序号。"""
    parts = res.split('_')
    nm = parts[0] + ''.join(x.capitalize() for x in parts[1:])
    base, k = nm, 2
    while nm in taken:
        nm = f'{base}{k}'
        k += 1
    return nm


def hoist_into_composable(src: str, rs: list) -> tuple:
    """把同一文件里的非组合 lambda 文案上提到各自宿主 @Composable 函数体开头。

    返回 (新源码, 上提条数, 放弃条数)。放弃的是找不到函数体 `{` 的（表达式体等）。
    """
    per_fun = {}
    for r in rs:
        per_fun.setdefault(r['fun'], []).append(r)
    insertions, done, dropped = [], 0, 0
    for fun, group in per_fun.items():
        body = fun_body_start(src, fun)
        if body is None:
            dropped += len(group)
            continue
        taken = set(re.findall(r'\b[A-Za-z_]\w*\b', src))
        res2val, decls = {}, []
        for r in group:
            if r['res'] in res2val:                 # 同函数内同资源共用一个 val
                r['val'] = res2val[r['res']]
                continue
            nm = val_name(r['res'], taken)
            taken.add(nm)
            res2val[r['res']] = nm
            r['val'] = nm
            decls.append(f'val {nm} = stringResource(R.string.{r["res"]})')
        line_start = src.rfind('\n', 0, body) + 1
        indent = re.match(r'\s*', src[line_start:]).group(0) + '    '
        insertions.append((body, ''.join(f'\n{indent}{d}' for d in decls)))
        done += len(group)
    # 替换与插入必须合成**同一批编辑**按偏移降序统一应用：
    # 字面量替换会改变长度（`"全选部门（20）"` → `aqDemoText46`），若先替换再按
    # 旧偏移插入，插入点会整体错位（实测把 val 插进了 `@OptIn(…Api::class)` 中间）。
    edits = [(r['start'], r['end'], r['val']) for r in rs if 'val' in r]
    for r in rs:
        if 'val' in r:
            assert src[r['start']:r['end']] == r['lit'], f'{r["file"]}:{r["line"]} 偏移失配'
    edits += [(body, body, ins) for body, ins in insertions]
    for a, b, txt in sorted(edits, key=lambda x: -x[0]):
        src = src[:a] + txt + src[b:]
    return src, done, dropped


def add_imports(src: str, need_string_resource: bool = True) -> str:
    if need_string_resource and 'import androidx.compose.ui.res.stringResource' not in src:
        ms = list(re.finditer(r'^import androidx\.compose\.[^\n]*\n', src, re.M))
        if ms:
            src = src[:ms[-1].end()] + 'import androidx.compose.ui.res.stringResource\n' + src[ms[-1].end():]
        else:
            src = re.sub(r'^(package [^\n]*\n)', r'\1\nimport androidx.compose.ui.res.stringResource\n', src, count=1, flags=re.M)
    if 'import com.monkeycode.liquidui.R' not in src:
        ms = list(re.finditer(r'^import com\.monkeycode\.[^\n]*\n', src, re.M))
        if ms:
            src = src[:ms[0].start()] + 'import com.monkeycode.liquidui.R\n' + src[ms[0].start():]
        else:
            src = re.sub(r'^(package [^\n]*\n)', r'\1\nimport com.monkeycode.liquidui.R\n', src, count=1, flags=re.M)
    return src


def main():
    sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
    import kt_scan
    recs = kt_scan.collect(SRC)
    srcs = {}
    svars = {}
    skipped = collections.Counter()
    stage = []
    for r in recs:
        if STAGE in ('3a', '3b'):
            if r['cat'] != 'A 组合体内' or not r.get('noncomp') or r.get('interp'):
                continue
            if r['file'] not in srcs:
                srcs[r['file']] = (SRC / r['file']).read_text(encoding='utf-8')
            ctx = find_context(srcs[r['file']], r['fun'], r['start'])
            if STAGE == '3a':
                if not ctx:
                    skipped['宿主无 context（3b 上提）'] += 1
                    continue
                r['ctx'] = ctx
            else:
                if ctx:
                    skipped['宿主有 context（3a 已做）'] += 1
                    continue
        else:
            if r['cat'] not in ('A 组合体内', 'C1 组合默认参数'):
                continue
            if r.get('noncomp'):
                skipped['非组合 lambda'] += 1
                continue
            if ('/ui2/previews/' in r['file']) != (STAGE == '2b'):
                continue
            if r['file'] not in srcs:
                srcs[r['file']] = (SRC / r['file']).read_text(encoding='utf-8')
        if r['file'] not in srcs:
            srcs[r['file']] = (SRC / r['file']).read_text(encoding='utf-8')
        if r['file'] not in svars:
            svars[r['file']] = state_vars(srcs[r['file']])
        why = risky(srcs[r['file']], r, allow_noncomp=(STAGE in ('3a', '3b')), svars=svars[r['file']]) or (
            '含插值（留给 D 阶段）' if is_interp(r['lit']) else '')
        if why:
            skipped[why] += 1
            continue
        stage.append(r)
    print('本阶段跳过：' + '，'.join(f'{k} {v} 条' for k, v in skipped.most_common()))
    # 跨文件重复 → common_*
    files_of = {}
    for r in stage:
        files_of.setdefault(r['text'], set()).add(r['file'])
    ex_name, ex_text, counters = load_existing()
    names, used = dict(ex_text), dict(ex_name)     # 已有资源名优先复用，避免同文案生成两条
    shared_n = [max([int(n.rsplit('_', 1)[-1]) for n in ex_name if n.startswith('common_shared_')] or [0])]
    print(f'[{STAGE}] 既有 strings.xml {len(ex_name)} 条，其中 {len(ex_text)} 种文案可复用')
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
    hoisted = dropped = 0
    for rel, rs in byfile.items():
        p = SRC / rel
        src = p.read_text(encoding='utf-8')
        if STAGE == '3b':
            src, d, k = hoist_into_composable(src, rs)
            hoisted += d; dropped += k
        else:
            for r in sorted(rs, key=lambda x: -x['start']):
                assert src[r['start']:r['end']] == r['lit'], f'{rel}:{r["line"]} 偏移失配'
                call = (f"{r['ctx']}.getString(R.string.{r['res']})" if STAGE == '3a'
                        else f"stringResource(R.string.{r['res']})")
                src = src[:r['start']] + call + src[r['end']:]
        p.write_text(add_imports(src, need_string_resource=(STAGE != '3a')), encoding='utf-8')
    print(f'  改写 Kotlin 文件 {len(byfile)} 个' +
          (f'，上提 {hoisted} 处' + (f'，放弃 {dropped} 处（找不到函数体）' if dropped else '') if STAGE == '3b' else ''))

    # ---- 写 strings.xml：保留既有条目与注释，新条目按源文件分组追加 ----
    cur_xml = STRINGS.read_text(encoding='utf-8')
    fresh = [(nm, txt, f) for nm, txt, f in entries if nm not in ex_name]
    block = []
    cur = None
    for nm, txt, f in sorted(fresh, key=lambda x: (x[2], x[0])):
        if f != cur:
            block.append(f'\n    <!-- ===== {f.split("/java/")[-1]}（阶段 {STAGE}） ===== -->\n')
            cur = f
        esc, raw = xml_escape(txt)
        block.append(f'    <string name="{nm}"{" formatted=\"false\"" if raw else ""}>{esc}</string>\n')
    if block:
        assert '</resources>' in cur_xml
        cur_xml = cur_xml.replace('</resources>', ''.join(block) + '</resources>', 1)
    STRINGS.write_text(cur_xml, encoding='utf-8')
    print(f'  strings.xml 新增 {len(fresh)} 条（复用既有 {len(entries) - len(fresh)} 条），总计 {len(ex_name) + len(fresh)} 条')


if __name__ == '__main__':
    main()
