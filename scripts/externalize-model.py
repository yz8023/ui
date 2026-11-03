#!/usr/bin/env python3
"""把「目录型数据模型」的显示文案字段改成 `@StringRes Int`（阶段 5b 起的做法，5c 通用化）。

适用形状（三个 Spec 都是这个形状，UI2 目录也是、已在阶段 5b 用同法做完）：

    data class XxxEntry(val id: String, val name: String, val category: String, …)
    val XxxCategories = listOf("联网与发布", …)
    val XxxEntries = listOf(XxxEntry(id = "U01", name = "…", …), …)

三条规则，不是一刀切：
  · 纯显示文案字段 → `@StringRes Int`，字段名加 `Res` 后缀；长字段的 `"a" + "b"` 多段拼接
    **整段合并成一个资源**（一段一条会把语义切碎）；
  · `category` 既是文案又是筛选键 → 字段留 `String` 但值改成稳定英文键，文案进
    `XxxCategoryLabels: Map<String, Int>`。这样目录页的 `rememberSaveable` 状态类型不变
    （老安装保存的筛选不会因为改造成资源 id 而失配），换文案/加语言也不影响筛选；
  · `id` / `slug` / `en` / `difficulty` 不是文案（`id` 还用于预览分派）→ 原样不动。

渲染点四种改法（都在 CI 已验证过的范式内）：
    Text(stringResource(e.nameRes))                                   // 组合体内
    "${e.id} · ${stringResource(Labels.getValue(e.category))}"        // 字符串模板内
    context.getString(e.nameRes).contains(query)                      // remember{} 等非组合 lambda
    clipboard.setText(AnnotatedString(context.getString(e.promptRes))) // onLongClick 等非组合 lambda

自检（任一失败即 exit(1)，不留半成品）：XML 往返逐字节相同 / 资源名唯一全 ASCII 不撞名 /
值不以 @? 开头且无首尾空白 / 条目数不变 / 花括号增量平衡 / Spec 里迁移字段无中文残留 /
Screen 里迁移字段无漏改。跑完还要过 `scripts/probe-i18n.py`（七道闸）与 kotlinc 语法检查。

用法：`python3 scripts/externalize-model.py [--only tk,ot,aq] [--dry]`
"""
import importlib.util
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
KT = ROOT / 'android/app/src/main/java/com/monkeycode/liquidui'
STRINGS = ROOT / 'android/app/src/main/res/values/strings.xml'
sys.path.insert(0, str(ROOT / 'scripts'))
from kt_args import split_args, entry_positions, parse_concat, split_named  # noqa: E402

_spec = importlib.util.spec_from_file_location('ext', ROOT / 'scripts/externalize-strings.py')
ext = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(ext)

MODELS = [
    dict(
        key='tk', stage='5c', path='ui/features/toolkit/ToolkitSpec.kt',
        cls='ToolkitEntry', list_name='ToolkitEntries', prefix='tk',
        text=['name', 'summary', 'why', 'keyImpl', 'pitfalls', 'params', 'prompt'],
        keep=['id', 'en', 'difficulty'], cat='category',
        cats_name='ToolkitCategories', labels_name='ToolkitCategoryLabels',
        cats_doc='/** 目录页的分类筛选项（稳定英文键，顺序 = 芯片展示顺序；文案见 ToolkitCategoryLabels）。 */',
        cat_keys={'联网与发布': 'network_release', '可观测性': 'observability',
                  '内容渲染': 'content_render', '布局适配': 'layout_adapt'},
        screen='ui/screens/ToolkitScreen.kt', screen_pkg='ui.features.toolkit',
    ),
    dict(
        key='ot', stage='5c', path='ui/features/overlayterms/OverlayTermsSpec.kt',
        cls='OverlayTerm', list_name='OverlayTerms', prefix='ot',
        text=['name', 'summary', 'definition', 'whenToUse', 'vsNeighbor', 'androidImpl',
              'badCase', 'params', 'prompt'],
        keep=['id', 'en', 'difficulty'], cat='category',
        cats_name='OverlayCategories', labels_name='OverlayCategoryLabels',
        cats_doc='/** 目录页的分类筛选项（稳定英文键，顺序 = 芯片展示顺序；文案见 OverlayCategoryLabels）。 */',
        cat_keys={'阻塞类': 'blocking', '轻提示类': 'transient', '锚定类': 'anchored',
                  '面板类': 'panel', '状态类': 'status'},
        screen='ui/screens/OverlayTermsScreen.kt', screen_pkg='ui.features.overlayterms',
    ),
    dict(
        key='aq', stage='5c', path='ui/features/adminquery/AdminQuerySpec.kt',
        cls='AdminQueryNorm', list_name='AdminQueryNorms', prefix='aq',
        text=['name', 'summary', 'rule', 'badCase', 'params', 'prompt'],
        keep=['id', 'difficulty'], cat='category',
        cats_name='AdminQueryCategories', labels_name='AdminQueryCategoryLabels',
        cats_doc='/** 目录页的分类筛选项（稳定英文键，顺序 = 芯片展示顺序；文案见 AdminQueryCategoryLabels）。 */',
        cat_keys={'筛选': 'filter', '时间': 'time_range', '标签': 'tags',
                  '看板': 'dashboard', '组合': 'combined'},
        screen='ui/screens/AdminQueryScreen.kt', screen_pkg='ui.features.adminquery',
    ),
]
CJK = re.compile(r'[\u4e00-\u9fff]')


def snake(name):
    return re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()


def kt_literal(v):
    assert '\n' not in v, f'保留字段不该有换行: {v[:40]!r}'
    return '"' + v.replace('\\', '\\\\').replace('"', '\\"') + '"'


def add_imports(src, needed):
    """把缺的 import 插到既有 import 块的正确字母序位置；没有 import 块就插在 package 之后。"""
    lines = src.splitlines(keepends=True)
    idx = [i for i, l in enumerate(lines) if l.startswith('import ')]
    for imp in sorted(needed):
        stmt = f'import {imp}\n'
        if stmt in lines:
            continue
        if not idx:
            j = next(i for i, l in enumerate(lines) if l.startswith('package '))
            lines.insert(j + 1, '\n' + stmt)
            idx = [i for i, l in enumerate(lines) if l.startswith('import ')]
            continue
        pos = idx[-1] + 1
        for i in idx:
            if lines[i] > stmt:
                pos = i
                break
        lines.insert(pos, stmt)
        idx = [i for i, l in enumerate(lines) if l.startswith('import ')]
    return ''.join(lines)


def parse_spec(m):
    src = (KT / m['path']).read_text(encoding='utf-8')
    dc = re.search(r'data class ' + m['cls'] + r'\(', src)
    assert dc, f'{m["path"]}: 找不到 data class {m["cls"]}'
    fields, dend = split_args(src, dc.end())
    # 实参里可能夹着本脚本自己生成的 // 说明行（category 那条），先剥掉再解析
    fields = [re.sub(r'//[^\n]*\n?', '', f).strip() for f in fields]
    fields = [f for f in fields if f]
    names = []
    for f in fields:
        mm = re.match(r'^(?:@StringRes\s+)?val\s+(\w+)\s*:\s*(\w+)', f)
        assert mm, f'{m["path"]}: 字段没看懂 {f[:50]!r}'
        names.append((mm.group(1), mm.group(2)))
    if any(n.endswith('Res') for n, _ in names):
        return None                      # 已迁移过（幂等重跑走这条）
    flat = [n for n, _ in names]
    assert set(flat) == set(m['text']) | set(m['keep']) | {m['cat']}, \
        f'{m["path"]}: 字段与配置不符 {set(flat) ^ (set(m["text"]) | set(m["keep"]) | {m["cat"]})}'
    assert all(t == 'String' for _, t in names), f'{m["path"]}: 有非 String 字段 {names}'

    entries = []
    for e in entry_positions(src, m['cls']):
        args, end = split_args(src, e.end())
        if src[end:end + 1] == ',':
            end += 1
        vals = {}
        for a in args:
            if not a.strip():
                continue                       # 尾随逗号产生的空实参
            k, v = split_named(a)
            assert k, f'{m["path"]}: 期望具名实参，拿到 {a[:40]!r}'
            vals[k] = parse_concat(v)
        assert set(vals) == set(flat), f'{m["path"]}: 实参字段与数据类不符 {set(vals) ^ set(flat)}'
        assert vals[m['cat']] in m['cat_keys'], f'{m["path"]}: 未知分类 {vals[m["cat"]]!r}'
        # span 起点退到行首：原缩进要一起吃掉，否则替换文本自带的 4 空格会叠成 8 空格
        ls = src.rfind('\n', 0, e.start()) + 1
        assert src[ls:e.start()].strip() == '', f'{m["path"]}: 构造点前面不是纯缩进 {src[ls:e.start()]!r}'
        entries.append(dict(vals=vals, start=ls, end=end))
    return src, fields, names, flat, entries, dc, dend


def build_dataclass(m, names):
    out = [f'data class {m["cls"]}(']
    for n, _ in names:
        if n in m['text']:
            out.append(f'    @StringRes val {n}Res: Int,')
        elif n == m['cat']:
            out.append(f'    // 稳定英文键（筛选与 rememberSaveable 用它），文案查 {m["labels_name"]}')
            out.append(f'    val {n}: String,')
        else:
            out.append(f'    val {n}: String,')
    out.append(')')
    return '\n'.join(out)


def entry_text(m, names, vals):
    eid = vals['id'].lower()
    out = [f'    {m["cls"]}(']
    for n, _ in names:
        if n in m['text']:
            out.append(f'        {n}Res = R.string.{m["prefix"]}_{eid}_{snake(n)},')
        elif n == m['cat']:
            out.append(f'        {n} = "{m["cat_keys"][vals[n]]}",')
        else:
            out.append(f'        {n} = {kt_literal(vals[n])},')
    out.append('    ),')
    return '\n'.join(out)


def cats_block(m):
    keys = [m['cat_keys'][c] for c in m['cat_keys']]
    labels = ',\n'.join(f'    "{k}" to R.string.{m["prefix"]}_category_{k}' for k in keys)
    return (f'{m["cats_doc"]}\n'
            f'val {m["cats_name"]} = listOf({", ".join(chr(34) + k + chr(34) for k in keys)})\n\n'
            f'/**\n'
            f' * 分类键 → 文案资源。渲染：`stringResource({m["labels_name"]}.getValue(key))`。\n'
            f' * 用 `getValue` 不用 `[]`：键写错要当场崩，不要静默显示空白。\n'
            f' */\n'
            f'val {m["labels_name"]}: Map<String, Int> = mapOf(\n{labels},\n)')


def patch_screen(m, dry=False):
    """按四条通用规则改渲染点；返回 (改动条数明细, 新源码)。"""
    p = KT / m['screen']
    src = p.read_text(encoding='utf-8')
    recv = sorted({mm.group(1) for mm in re.finditer(
        r'\b(\w+)\.(?:' + '|'.join(m['text']) + r')\b', src)})
    assert recv, f'{m["screen"]}: 没找到任何渲染点'
    stats = {}
    for r in recv:
        for f in m['text']:
            # 1) 非组合 lambda 里的 .contains(query) 之类成员调用 → context.getString
            pat = re.compile(rf'\b{r}\.{f}\.(?=\w)')
            src, n1 = pat.subn(f'context.getString({r}.{f}Res).', src)
            # 2) 剪贴板：AnnotatedString(x.prompt) → context.getString
            pat = re.compile(rf'AnnotatedString\({r}\.{f}\)')
            src, n2 = pat.subn(f'AnnotatedString(context.getString({r}.{f}Res))', src)
            # 3) 字符串模板内
            pat = re.compile(rf'\$\{{{r}\.{f}\}}')
            src, n3 = pat.subn(f'${{stringResource({r}.{f}Res)}}', src)
            # 4) 其余裸表达式 → stringResource
            pat = re.compile(rf'(?<![\w.${{]){r}\.{f}\b(?!Res)')
            src, n4 = pat.subn(f'stringResource({r}.{f}Res)', src)
            if n1 + n2 + n3 + n4:
                stats[f'{r}.{f}'] = (n1, n2, n3, n4)
        # category 只在模板里换文案；筛选比较保持比键
        pat = re.compile(rf'\$\{{{r}\.{m["cat"]}\}}')
        src, n5 = pat.subn(f'${{stringResource({m["labels_name"]}.getValue({r}.{m["cat"]}))}}', src)
        if n5:
            stats[f'{r}.{m["cat"]}(模板)'] = (0, 0, n5, 0)

    # 芯片行：`Cats.forEach { c -> Chip(c, category == c) { … } }`。
    # ToolkitScreen/OverlayTermsScreen 是单行写法、AdminQueryScreen 是多行写法，
    # 用 \s* 容忍两种排版，统一输出多行形式（与 Ui2Gallery 阶段 5b 的结果一致）。
    chip = re.compile(
        rf'^(?P<ind>[ ]*){m["cats_name"]}\.forEach \{{ c ->\s*'
        rf'(?P<fn>\w+)\(c, category == c\) \{{ category = if \(category == c\) null else c \}}\s*'
        r'\}', re.M)

    def chip_rep(mm):
        i = mm.group('ind')
        return (f'{i}{m["cats_name"]}.forEach {{ key ->\n'
                f'{i}    {mm.group("fn")}(stringResource({m["labels_name"]}.getValue(key)), category == key) {{\n'
                f'{i}        category = if (category == key) null else key\n'
                f'{i}    }}\n'
                f'{i}}}')

    src, nchip = chip.subn(chip_rep, src)
    assert nchip == 1, f'{m["screen"]}: 芯片行命中 {nchip} 次'

    # 两处 context 锚点
    a1 = '    var query by rememberSaveable { mutableStateOf("") }\n'
    assert src.count(a1) == 1, f'{m["screen"]}: query 锚点 {src.count(a1)} 次'
    src = src.replace(a1, '    val context = LocalContext.current\n' + a1, 1)
    a2 = '    val clipboard = LocalClipboardManager.current\n'
    assert src.count(a2) == 1, f'{m["screen"]}: clipboard 锚点 {src.count(a2)} 次'
    src = src.replace(a2, a2 + '    val context = LocalContext.current\n', 1)

    src = add_imports(src, ['androidx.compose.ui.platform.LocalContext',
                            f'com.monkeycode.liquidui.{m["screen_pkg"]}.{m["labels_name"]}'])
    # 漏改检查
    left = []
    for r in recv:
        for f in m['text'] + [m['cat']]:
            for mm in re.finditer(rf'(?<![\w.]){r}\.{f}\b(?!Res)', src):
                pre = src[max(0, mm.start() - 60):mm.start()]
                ctx = src[max(0, mm.start() - 30):mm.end() + 20].replace('\n', ' ')
                # 有意保留的两种：分类键参与筛选比较；以及替换后自己产生的 getValue(x.category)
                if f == m['cat'] and ('getValue(' in pre or
                                      re.search(rf'{r}\.{f} ==|== *{r}\.{f}|filter', ctx)):
                    continue
                left.append(f'{r}.{f}: …{ctx}…')
    assert not left, f'{m["screen"]} 漏改 {len(left)} 处:\n  ' + '\n  '.join(left[:6])
    if not dry:
        p.write_text(src, encoding='utf-8')
    return stats, nchip


def main():
    dry = '--dry' in sys.argv
    only = None
    if '--only' in sys.argv:
        only = set(sys.argv[sys.argv.index('--only') + 1].split(','))
    todo = [m for m in MODELS if not only or m['key'] in only]

    existing = set(re.findall(r'<string name="([^"]+)"', STRINGS.read_text(encoding='utf-8')))
    all_values, report, seg_lines = {}, [], {}
    for m in todo:
        parsed = parse_spec(m)
        if parsed is None:
            print(f'  [{m["key"]}] 已迁移过，跳过')
            continue
        src, fields, names, flat, entries, dc, dend = parsed
        # ---- 资源 ----
        lines, values = [], {}

        def add(name, text):
            assert re.fullmatch(r'[a-z0-9_]+', name), f'非法资源名 {name!r}'
            assert name not in values and name not in existing, f'资源名重复/撞名 {name}'
            assert text == text.strip(), f'{name} 首尾有空白（aapt 会剥掉）'
            assert not text.startswith(('@', '?')), f'{name} 以 {text[0]} 开头（aapt 当资源引用）'
            esc, need_raw = ext.xml_escape_value(text)   # 真值口径：字面反斜杠要先自我转义
            lines.append(f'    <string name="{name}"{" formatted=" + chr(34) + "false" + chr(34) if need_raw else ""}>{esc}</string>')
            values[name] = text

        cat_lines, entry_lines = [], []
        for cn, k in m['cat_keys'].items():
            n0 = len(lines)
            add(f'{m["prefix"]}_category_{k}', cn)
            cat_lines += lines[n0:]
        for e in entries:
            eid = e['vals']['id'].lower()
            for f in m['text']:
                n0 = len(lines)
                add(f'{m["prefix"]}_{eid}_{snake(f)}', e['vals'][f])
                entry_lines += lines[n0:]
        all_values.update(values)
        seg_lines[m['key']] = (cat_lines, entry_lines)

        # ---- 改 Spec ----
        b0 = src.count('{') - src.count('}')
        edits = []
        new_dc = build_dataclass(m, names)
        edits.append((dc.start(), dend, new_dc))
        cm = re.search(r'(?:/\*\*[^\n]*\*/\s*\n)?val ' + m['cats_name'] + r' = listOf\([^\n]*\)', src)
        assert cm, f'{m["path"]}: 找不到 {m["cats_name"]}'
        edits.append((cm.start(), cm.end(), cats_block(m)))
        for e in entries:
            edits.append((e['start'], e['end'], entry_text(m, names, e['vals']) + '\n'))
        gen = [new_dc, cats_block(m)] + [t for _, _, t in sorted(edits, key=lambda x: x[0])[1:]]
        for s0, s1, txt in sorted(edits, key=lambda x: -x[0]):
            assert not (s0 < dend and s1 > dc.start()) or (s0, s1) == (dc.start(), dend), '编辑区间重叠'
            src = src[:s0] + txt + src[s1:]
        src = add_imports(src, ['androidx.annotation.StringRes', 'com.monkeycode.liquidui.R'])
        b1 = src.count('{') - src.count('}')
        assert b0 == b1, f'{m["path"]}: 花括号增量 {b0} → {b1}'
        n_entry = len(entry_positions(src, m['cls']))
        assert n_entry == len(entries), f'{m["path"]}: 条目数 {len(entries)} → {n_entry}'
        # 中文残留只查**本次重建的片段**：同一文件里可能还有别的数据（如 AdminQuerySpec 的模拟
        # 数据集）属于后续批次，不该被这条闸拦住。注释里的中文是说明文字，剥掉再查。
        body = '\n'.join(gen)
        body = re.sub(r'/\*.*?\*/', '', re.sub(r'//[^\n]*', '', body), flags=re.S)
        body = re.sub(r'"""(.*?)"""', '""', body, flags=re.S)
        cjk_left = [l for l in re.findall(r'"((?:[^"\\]|\\.)*)"', body) if CJK.search(l)]
        assert not cjk_left, f'{m["path"]}: 重建片段里还剩 {len(cjk_left)} 个中文字面量，例 {cjk_left[:2]}'

        stats, nchip = patch_screen(m, dry=dry)
        report.append((m, len(entries), len(values), stats))
        if not dry:
            (KT / m['path']).write_text(src, encoding='utf-8')

    if not report:
        print('没有需要迁移的模型')
        return
    for m, ne, nv, stats in report:
        print(f'  [{m["key"]}] {m["cls"]} {ne} 条 → 新资源 {nv} 个（含分类 {len(m["cat_keys"])}）；'
              f'{m["screen"].split("/")[-1]} 改了 {sum(sum(v) for v in stats.values())} 处、芯片行 {1} 处')
        if '--verbose' in sys.argv:
            for k, v in stats.items():
                print(f'        {k:22s} 成员调用 {v[0]} / 剪贴板 {v[1]} / 模板 {v[2]} / 裸表达式 {v[3]}')
    print(f'  合计新资源 {len(all_values)} 个')

    if dry:
        print('\n[dry] XML 样例：')
        sample = [l for k in seg_lines for l in seg_lines[k][0][:1] + seg_lines[k][1][:1]]
        for l in sample:
            print('   ', l[:140] + ('…' if len(l) > 140 else ''))
        print(f'[dry] 预计 strings.xml {len(existing)} → {len(existing) + len(all_values)} 条')
        return

    sx = STRINGS.read_text(encoding='utf-8')
    block = ''
    for m, ne, nv, _ in report:
        cat_lines, entry_lines = seg_lines[m['key']]
        assert len(cat_lines) + len(entry_lines) == nv, f'{m["key"]}: 分段行数对不上'
        block += (f'\n    <!-- ===== {m["path"]}（阶段 {m["stage"]}） ===== -->\n'
                  f'    <!-- 分类键文案（{m["labels_name"]} 查表用） -->\n' + '\n'.join(cat_lines) +
                  f'\n    <!-- {ne} 条条目的 {" / ".join(m["text"])} -->\n' + '\n'.join(entry_lines) + '\n')
    assert sx.count('</resources>') == 1
    STRINGS.write_text(sx.replace('</resources>', block + '</resources>', 1), encoding='utf-8')

    # ---- 自检：XML 往返逐字节相同 ----
    import xml.etree.ElementTree as ET
    got = {r.get('name'): r.text or '' for r in ET.parse(STRINGS).getroot().findall('string')}
    bad = [(n, all_values[n], ext.xml_unescape_value(got.get(n, ''))) for n in all_values
           if n not in got or ext.xml_unescape_value(got[n]) != all_values[n]]
    assert not bad, f'往返不一致 {len(bad)} 条，例 {bad[0][0]}: {bad[0][1][:40]!r} != {bad[0][2][:40]!r}'
    assert len(got) == len(existing) + len(all_values), f'条数 {len(existing)}+{len(all_values)} != {len(got)}'
    print(f'✓ XML 往返逐字节一致（{len(all_values)} 条）；strings.xml {len(existing)} → {len(got)} 条')


if __name__ == '__main__':
    main()
