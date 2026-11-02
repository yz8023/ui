#!/usr/bin/env python3
"""阶段 5b（一次性脚本，跑完即删）：UI2 目录的 77 条素材元数据改 @StringRes。

改法（与阶段 5a 同一套范式，但 `category` 特殊）：
  · `name` / `summary` / `params` / `prompt` 是纯显示文案 → `@StringRes Int`；
  · `category` **既是文案又是筛选键**，所以拆成两半：字段本身留 String 但值改成
    稳定英文键（`basic_components` 等），文案进 `Ui2CategoryLabels: Map<String, Int>`。
    这样 `rememberSaveable` 存的还是 String（老安装的状态不会因为类型变了而失配），
    而且换文案/翻译不会影响筛选与已保存状态——这正是「数据侧不许硬编码文案」的口径。
  · `id` / `slug` 是标识符（`Ui2Preview` 按 `feature.id` 分派），`difficulty` 是 ★ 符号
    不是文案，三者都保持 String 不动。

自检（失败即 sys.exit(1)，不留半成品）：
  1. XML 往返：每条新资源反转义后必须与 Kotlin 原值**逐字节相同**；
  2. 资源名唯一、全 ASCII、与既有 866 条不撞名；
  3. 值不以 `@`/`?` 开头（aapt 会当资源引用）、无首尾空白（aapt 会剥掉）；
  4. 花括号增量平衡；改写后源码里的 `Ui2Feature(` 构造点数量不变。
"""
import importlib.util
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
BASE = ROOT / 'android/app/src/main/java/com/monkeycode/liquidui/ui/features/ui2'
STRINGS = ROOT / 'android/app/src/main/res/values/strings.xml'
CAT = BASE / 'Ui2Catalog.kt'
EXTRA = BASE / 'Ui2ExtraCatalog.kt'
GALLERY = ROOT / 'android/app/src/main/java/com/monkeycode/liquidui/ui/screens/Ui2Gallery.kt'
MOTION = BASE / 'previews/PreviewMotionTools.kt'

# 复用外置脚本的转义口径（含 % 一律 formatted="false"，' " 反斜杠转义，真换行 → \n 两字符）
_spec = importlib.util.spec_from_file_location('ext', ROOT / 'scripts/externalize-strings.py')
ext = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(ext)
xml_escape = ext.xml_escape
xml_unescape = ext.xml_unescape

sys.path.insert(0, str(ROOT))
from _argsplit import split_args, entry_positions, arg_value  # noqa: E402

# 中文分类 → 稳定英文键（14 类：主目录 8 + 增补 6）
CAT_KEY = {
    '基础组件': 'basic_components', '表单输入': 'form_input', '导航': 'navigation',
    '布局': 'layout', '反馈': 'feedback', '数据展示': 'data_display',
    '动效': 'motion', '应用模板': 'app_templates',
    '玻璃拆解': 'glass_breakdown', '动效案例': 'motion_cases', '视频专题': 'video_topics',
    '弹层馆': 'popup_gallery', '动效馆': 'motion_gallery', '工具': 'tools',
}
BASE_CATS = ['基础组件', '表单输入', '导航', '布局', '反馈', '数据展示', '动效', '应用模板']
EXTRA_CHIP_CATS = ['玻璃拆解', '动效案例']   # 只有这两类进筛选芯片（与改造前行为一致）
FIELDS = [('name', 2), ('category', 3), ('difficulty', 4),
          ('summary', 5), ('params', 6), ('prompt', 7)]


def parse(path):
    src = path.read_text(encoding='utf-8')
    out = []
    for m in entry_positions(src):
        args = split_args(src, m.end())
        assert len(args) == 8, f'{path.name} 实参 {len(args)} 个: {args[0][:40]!r}'
        e = {'id': arg_value(args[0]), 'slug': arg_value(args[1])}
        for nm, idx in FIELDS:
            e[nm] = arg_value(args[idx])
        e['span'] = (m.start(), m.end() + len(''.join(args)) + 1)  # 含收尾 )
        out.append(e)
    return src, out


def res_name(eid, field):
    return f'ui2_{eid.lower()}_{field}'


def build(entries):
    """→ (XML 行列表, {资源名: 原值})"""
    lines, values = [], {}

    def add(name, text):
        assert re.fullmatch(r'[a-z0-9_]+', name), f'非法资源名 {name!r}'
        assert name not in values, f'资源名重复 {name}'
        assert text == text.strip(), f'{name} 首尾有空白（aapt 会剥掉）'
        assert not text.startswith(('@', '?')), f'{name} 以 {text[0]} 开头（aapt 当资源引用）'
        esc, need_raw = xml_escape(text)
        attr = ' formatted="false"' if need_raw else ''
        lines.append(f'    <string name="{name}"{attr}>{esc}</string>')
        values[name] = text

    for cn in BASE_CATS + [c for c in CAT_KEY if c not in BASE_CATS]:
        add(f'ui2_category_{CAT_KEY[cn]}', cn)
    for e in entries:
        for field in ('name', 'summary', 'params', 'prompt'):
            add(res_name(e['id'], field), e[field])
    return lines, values


def entry_src(e):
    return (f'    Ui2Feature("{e["id"]}", "{e["slug"]}", R.string.{res_name(e["id"], "name")}, '
            f'"{CAT_KEY[e["category"]]}", "{e["difficulty"]}", '
            f'R.string.{res_name(e["id"], "summary")}, R.string.{res_name(e["id"], "params")}, '
            f'R.string.{res_name(e["id"], "prompt")}),')


CATALOG_KDOC = '''/**
 * UI2 体验馆目录（46 条）：元数据与提示词移植自 yz8023/UI2（library/index.json
 * 与 library/prompts/全部提示词.md），文档原件见仓库 `素材库/UI2体验馆/`。
 * 每条的真实 Compose 预览在 previews/ 各分类文件中实现，宿主见 Ui2PreviewHost。
 * UI2 编号与本模板 F01–F15 编号是两套体系，勿混用（id 带 UI2 语义仅作来源标识）。
 *
 * 文案一律走 `strings.xml`（资源名前缀 `ui2_<id 小写>_`），本文件只剩标识符：
 *   · `nameRes` / `summaryRes` / `paramsRes` / `promptRes` = `@StringRes Int`，渲染点解析；
 *   · `category` = 稳定英文键（既是筛选键也进 `rememberSaveable`），文案查 `Ui2CategoryLabels`；
 *   · `id` / `slug` / `difficulty` 不是文案（`Ui2Preview` 按 id 分派、difficulty 是 ★ 符号）。
 * 换语言或改品牌文字都只改 XML，不用碰这里。
 */'''


def rewrite_catalogs(cat_src, cat_entries, extra_src, extra_entries, values):
    labels = ',\n'.join(
        f'    "{CAT_KEY[cn]}" to R.string.ui2_category_{CAT_KEY[cn]}'
        for cn in list(CAT_KEY))
    new_cat = f'''package com.monkeycode.liquidui.ui.features.ui2

import androidx.annotation.StringRes
import com.monkeycode.liquidui.R

{CATALOG_KDOC}
data class Ui2Feature(
    val id: String,
    val slug: String,
    @StringRes val nameRes: Int,
    val category: String,
    val difficulty: String,
    @StringRes val summaryRes: Int,
    @StringRes val paramsRes: Int,
    @StringRes val promptRes: Int,
)

/** 主目录分类键（顺序 = 筛选芯片展示顺序）。键是稳定标识，不是显示文案。 */
val Ui2Categories = listOf({', '.join(f'"{CAT_KEY[c]}"' for c in BASE_CATS)})

/**
 * 分类键 → 文案资源（含增补目录的 6 类）。渲染：`stringResource(Ui2CategoryLabels.getValue(key))`。
 * 用 `getValue` 而不是 `[]`：键写错要当场崩，不要静默显示空白。
 */
val Ui2CategoryLabels: Map<String, Int> = mapOf(
{labels},
)

val Ui2Features: List<Ui2Feature> = listOf(

{chr(10).join(entry_src(e) for e in cat_entries)}
)
'''
    new_extra = f'''package com.monkeycode.liquidui.ui.features.ui2

import com.monkeycode.liquidui.R

/**
 * UI2 体验馆增补目录：46 条之外的内容（22 个交互拆解、XPopup 弹层馆、
 * motion-web 动效馆、结构生成器与并排对比），同样逐条真实 Compose 实现。
 * 系列前缀：G=玻璃拆解 M=动效案例 V=视频专题 X=弹层馆 W=动效馆 T=工具。
 * 字段口径与 `Ui2Feature`（见 Ui2Catalog.kt）一致：文案走 `strings.xml`，
 * `category` 是稳定英文键、文案查 `Ui2CategoryLabels`。
 */

/** 增补目录里进筛选芯片的分类键（只有这两类，与改造前一致；其余 4 类只在详情页显示）。 */
val Ui2ExtraCategories = listOf({', '.join(f'"{CAT_KEY[c]}"' for c in EXTRA_CHIP_CATS)})

val Ui2ExtraFeatures: List<Ui2Feature> = listOf(

{chr(10).join(entry_src(e) for e in extra_entries)}
)
'''
    return new_cat, new_extra


def patch(path, pairs):
    src = path.read_text(encoding='utf-8')
    for old, new in pairs:
        assert src.count(old) == 1, f'{path.name}: {src.count(old)} 次命中 {old[:70]!r}'
        src = src.replace(old, new, 1)
    path.write_text(src, encoding='utf-8')


def patch_consumers():
    # ---- Ui2Gallery.kt ----
    g = GALLERY
    src = g.read_text(encoding='utf-8')
    reps = [
        # import：LocalContext 排在 res.stringResource 之前（字母序 platform < res）
        ('import androidx.compose.ui.res.stringResource\n',
         'import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.res.stringResource\n'),
        ('import com.monkeycode.liquidui.ui.features.ui2.Ui2Categories\n',
         'import com.monkeycode.liquidui.ui.features.ui2.Ui2Categories\n'
         'import com.monkeycode.liquidui.ui.features.ui2.Ui2CategoryLabels\n'),
        ('import com.monkeycode.liquidui.ui.features.ui2.Ui2ExtraFeatures\n',
         'import com.monkeycode.liquidui.ui.features.ui2.Ui2ExtraCategories\n'
         'import com.monkeycode.liquidui.ui.features.ui2.Ui2ExtraFeatures\n'),
        # 列表页：remember{} 是非组合 lambda，只能用 context.getString
        ('    var query by rememberSaveable { mutableStateOf("") }\n'
         '    var category by rememberSaveable { mutableStateOf<String?>(null) }\n',
         '    val context = LocalContext.current\n'
         '    var query by rememberSaveable { mutableStateOf("") }\n'
         '    // category 存的是**分类键**（String），不是资源 id：老安装保存的状态照样能匹配\n'
         '    var category by rememberSaveable { mutableStateOf<String?>(null) }\n'),
        ('                (query.isBlank() || f.name.contains(query) || f.summary.contains(query) || f.id.contains(query, true))',
         '                (query.isBlank() || context.getString(f.nameRes).contains(query) ||\n'
         '                    context.getString(f.summaryRes).contains(query) || f.id.contains(query, true))'),
        ('            (Ui2Categories + listOf("玻璃拆解", "动效案例")).forEach { c -> Ui2Chip(c, category == c) { category = if (category == c) null else c } }',
         '            (Ui2Categories + Ui2ExtraCategories).forEach { key ->\n'
         '                Ui2Chip(stringResource(Ui2CategoryLabels.getValue(key)), category == key) {\n'
         '                    category = if (category == key) null else key\n'
         '                }\n'
         '            }'),
        # 卡片
        ('                Text(feature.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)\n'
         '                Text(feature.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)',
         '                Text(stringResource(feature.nameRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)\n'
         '                Text(stringResource(feature.summaryRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)'),
        # 详情页
        ('    val clipboard = LocalClipboardManager.current\n',
         '    val clipboard = LocalClipboardManager.current\n    val context = LocalContext.current\n'),
        ('                Text("${feature.id} · ${feature.category} · ${feature.difficulty}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)',
         '                Text(\n'
         '                    "${feature.id} · ${stringResource(Ui2CategoryLabels.getValue(feature.category))} · ${feature.difficulty}",\n'
         '                    style = MaterialTheme.typography.labelMedium,\n'
         '                    color = MaterialTheme.colorScheme.onSurfaceVariant,\n'
         '                )'),
        ('            Text(feature.name, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)',
         '            Text(stringResource(feature.nameRes), Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)'),
        ('            Text(feature.summary, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)',
         '            Text(stringResource(feature.summaryRes), Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)'),
        ('            Text(feature.params, Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)',
         '            Text(stringResource(feature.paramsRes), Modifier.padding(horizontal = 22.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)'),
        # onLongClick 是非组合 lambda → context.getString
        ('                                    clipboard.setText(AnnotatedString(feature.prompt))',
         '                                    clipboard.setText(AnnotatedString(context.getString(feature.promptRes)))'),
        ('                        Text(\n                            feature.prompt,\n',
         '                        Text(\n                            stringResource(feature.promptRes),\n'),
    ]
    patch(g, reps)

    # ---- PreviewMotionTools.kt（T02 并排对比的两个下拉 + 标题）----
    patch(MOTION, [
        ('all.take(24).forEach { f -> androidx.compose.material3.DropdownMenuItem(text = { Text("${f.id} ${f.name}") }, onClick = { aId = f.id; menuA = false }) }',
         'all.take(24).forEach { f -> androidx.compose.material3.DropdownMenuItem(text = { Text("${f.id} ${stringResource(f.nameRes)}") }, onClick = { aId = f.id; menuA = false }) }'),
        ('all.drop(22).forEach { f -> androidx.compose.material3.DropdownMenuItem(text = { Text("${f.id} ${f.name}") }, onClick = { bId = f.id; menuB = false }) }',
         'all.drop(22).forEach { f -> androidx.compose.material3.DropdownMenuItem(text = { Text("${f.id} ${stringResource(f.nameRes)}") }, onClick = { bId = f.id; menuB = false }) }'),
        ('Text("${f.id} · ${f.name}", fontWeight = FontWeight.B',
         'Text("${f.id} · ${stringResource(f.nameRes)}", fontWeight = FontWeight.B'),
    ])


def main():
    dry = '--dry' in sys.argv
    cat_src, cat_entries = parse(CAT)
    extra_src, extra_entries = parse(EXTRA)
    entries = cat_entries + extra_entries
    print(f'解析：Ui2Catalog {len(cat_entries)} 条、Ui2ExtraCatalog {len(extra_entries)} 条，共 {len(entries)} 条')

    xml_lines, values = build(entries)
    print(f'新资源 {len(xml_lines)} 条（分类 {len(CAT_KEY)} + 每条 4 个字段 × {len(entries)}）')

    # 撞名检查
    existing = set(re.findall(r'<string name="([^"]+)"', STRINGS.read_text(encoding='utf-8')))
    clash = existing & set(values)
    assert not clash, f'与既有资源撞名 {sorted(clash)[:5]}'
    print(f'与既有 {len(existing)} 条无撞名 ✓')

    if dry:
        print('\n[dry] XML 首 3 行样例：')
        for l in xml_lines[:3]:
            print('   ', l[:150] + ('…' if len(l) > 150 else ''))
        print('[dry] 条目改写样例：')
        print('   ', entry_src(entries[0])[:190])
        print('   ', entry_src(extra_entries[0])[:190])
        print(f'[dry] 预计 strings.xml {len(existing)} → {len(existing) + len(values)} 条')
        return

    new_cat, new_extra = rewrite_catalogs(cat_src, cat_entries, extra_src, extra_entries, values)
    b0 = cat_src.count('{') - cat_src.count('}') + extra_src.count('{') - extra_src.count('}')
    b1 = new_cat.count('{') - new_cat.count('}') + new_extra.count('{') - new_extra.count('}')
    assert b0 == b1 == 0, f'花括号不平衡 {b0} → {b1}'

    # ---- 写 XML（追加到 </resources> 前，按来源文件分段）----
    sx = STRINGS.read_text(encoding='utf-8')
    n_cat = len(BASE_CATS) + len([c for c in CAT_KEY if c not in BASE_CATS]) + len(cat_entries) * 4
    block = ('\n    <!-- ===== ui/features/ui2/Ui2Catalog.kt + Ui2ExtraCatalog.kt（阶段 5b）'
             ' ===== -->\n'
             '    <!-- 分类键文案（Ui2CategoryLabels 查表用） -->\n'
             + '\n'.join(xml_lines[:len(CAT_KEY)])
             + '\n    <!-- 46 条主目录 + 31 条增补目录：name / summary / params / prompt -->\n'
             + '\n'.join(xml_lines[len(CAT_KEY):]) + '\n')
    assert sx.count('</resources>') == 1
    sx = sx.replace('</resources>', block + '</resources>', 1)
    STRINGS.write_text(sx, encoding='utf-8')

    CAT.write_text(new_cat, encoding='utf-8')
    EXTRA.write_text(new_extra, encoding='utf-8')
    patch_consumers()

    # ---- 自检 1：XML 往返逐字节相同 ----
    import xml.etree.ElementTree as ET
    got = {r.get('name'): r.text or '' for r in ET.parse(STRINGS).getroot().findall('string')}
    bad = [(n, values[n], xml_unescape(got[n])) for n in values
           if n not in got or xml_unescape(got[n]) != values[n]]
    assert not bad, f'往返不一致 {len(bad)} 条，例：{bad[0][0]} {bad[0][1][:40]!r} != {bad[0][2][:40]!r}'
    print(f'✓ XML 往返逐字节一致（{len(values)} 条）')
    print(f'✓ strings.xml {len(existing)} → {len(got)} 条')
    print(f'✓ Ui2Catalog.kt {len(cat_src.splitlines())} → {len(new_cat.splitlines())} 行；'
          f'Ui2ExtraCatalog.kt {len(extra_src.splitlines())} → {len(new_extra.splitlines())} 行')


if __name__ == '__main__':
    main()
