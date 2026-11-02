#!/usr/bin/env python3
"""i18n 静态复核闸门。本地 kotlinc 没有 Compose classpath，验不出组合上下文合法性，
所以外置的每一轮都靠这个脚本 + CI 双重把关。

查四件事：
  1. 上下文合法性：stringResource 只能出现在可组合上下文（@Composable 函数体、
     可组合 lambda、@Composable 函数的默认参数值）。用「把整个调用替换成探针字面量、
     再交给 kt_scan 判定」的办法，因为单行 grep 会漏跨行、也会把
     `TextButton(onClick={}){ Text(stringResource()) }` 误报。
  2. 参数个数：`stringResource(id, a, b)` 的实参个数必须 == 格式串里最大的 %N$。
     少了会 IllegalFormatException（编译期查不出，运行时才崩），多了 aapt 会警告。
  3. formatted="false" 的资源不许带实参调用（带参时 %% 不会还原成 %，界面显示两个百分号）。
  4. 格式串本身合法：每个 % 后面必须是 % 或 N$s/N$d 这类位置化占位符。
  5. 委托可空属性不能直接当格式参数：`var x by remember { mutableStateOf<String?>(null) }`
     在 `if (x != null)` 里也**不能**智能转换（委托属性），当 vararg Any 传会编译失败。
     模板里 `"$x"` 不报错（可空直接插值），换成 stringResource(id, x) 才暴露，CI 实测过。

用法： python3 scripts/probe-i18n.py [源码根]
退出码非 0 表示有问题。
"""
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

HERE = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import kt_scan  # noqa: E402

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else
                    HERE.parent / 'android/app/src/main/java/com/monkeycode/liquidui')
STRINGS = HERE.parent / 'android/app/src/main/res/values/strings.xml'
SR_PROBE, GS_PROBE = '"\u25d9SR\u25d9"', '"\u25d9GS\u25d9"'


def mask_call(src: str, i: int) -> int:
    """从 `(` 起按平衡括号（跳过字符串）找到配对的 `)`，返回其下一位。"""
    assert src[i] == '('
    d, j, n = 0, i, len(src)
    while j < n:
        c = src[j]
        if c == '"':
            j += 1
            while j < n and src[j] != '"':
                j += 2 if src[j] == '\\' else 1
        elif c == '(':
            d += 1
        elif c == ')':
            d -= 1
            if d == 0:
                return j + 1
        j += 1
    raise ValueError('括号不平衡')


def split_args(s: str) -> list:
    """按顶层逗号切实参。"""
    out, d, cur, i, n = [], 0, [], 0, len(s)
    while i < n:
        c = s[i]
        if c == '"':
            j = i + 1
            while j < n and s[j] != '"':
                j += 2 if s[j] == '\\' else 1
            cur.append(s[i:j + 1]); i = j + 1; continue
        if c in '([{':
            d += 1
        elif c in ')]}':
            d -= 1
        if c == ',' and d == 0:
            out.append(''.join(cur).strip()); cur = []
        else:
            cur.append(c)
        i += 1
    if ''.join(cur).strip():
        out.append(''.join(cur).strip())
    return out


def load_resources():
    """{资源名: (文案, formatted_false)}，文案保留 XML 里的转义原样。"""
    res = {}
    for r in ET.parse(STRINGS).getroot().findall('string'):
        res[r.get('name')] = (r.text or '', r.get('formatted') == 'false')
    return res


def placeholders(text: str) -> list:
    """格式串里的位置化占位符序号。"""
    return [int(m.group(1)) for m in re.finditer(r'%(\d+)\$[a-zA-Z]', text)]


def main():
    kinds = kt_scan.build_lambda_kinds(ROOT)
    res = load_resources()
    calls = []          # (file, line, kind, res_name, args, span)
    for f in sorted(ROOT.rglob('*.kt')):
        src = f.read_text(encoding='utf-8')
        for m in re.finditer(r'(?:\b(\w+)\.getString|stringResource)\s*\(', src):
            open_paren = m.end() - 1
            try:
                close = mask_call(src, open_paren)
            except ValueError:
                continue
            inside = src[open_paren + 1:close - 1]
            args = split_args(inside)
            if not args or not args[0].startswith('R.string.'):
                continue
            name = args[0][len('R.string.'):]
            calls.append({'file': str(f.relative_to(ROOT)), 'line': src[:m.start()].count('\n') + 1,
                          'kind': 'getString' if m.group(1) else 'stringResource',
                          'res': name, 'args': args[1:], 'start': m.start(), 'end': close,
                          'offset': m.start()})

    # 委托可空属性的声明位置：按**最近的前置声明**判定，不能按文件收集名字。
    # 同一文件里常有两个同名变量（OverlayTermsDemo 里 PreviewP04 的 `picked` 非空、
    # PreviewP10 的 `picked` 可空），按文件收集会把前者误报。
    delegated_decl = {}
    for f in sorted(ROOT.rglob('*.kt')):
        s = f.read_text(encoding='utf-8')
        decls = []
        for m in re.finditer(
                r'\bva[rl]\s+(\w+)\s+by\s+(?:remember|rememberSaveable)\w*\s*\{\s*mutable\w*StateOf(<[^>]*\?>)?',
                s):
            decls.append((m.start(), m.group(1), bool(m.group(2))))
        if decls:
            delegated_decl[str(f.relative_to(ROOT))] = (s, decls)

    def is_nullable_delegated(rel: str, name: str, pos: int) -> bool:
        info = delegated_decl.get(rel)
        if not info:
            return False
        _s, decls = info
        best = None
        for off, nm, nullable in decls:
            if nm == name and off < pos and (best is None or off > best[0]):
                best = (off, nullable)
        return bool(best and best[1])

    errs = []
    # ---- 1. 资源存在性 + 参数个数 + formatted ----
    nsr = ngs = 0
    for c in calls:
        if c['kind'] == 'stringResource':
            nsr += 1
        else:
            ngs += 1
        if c['res'] not in res:
            errs.append(f"[缺资源] {c['file']}:{c['line']} R.string.{c['res']} 不在 strings.xml")
            continue
        text, formatted_false = res[c['res']]
        ph = placeholders(text)
        need = max(ph) if ph else 0
        if len(c['args']) != need:
            errs.append(f"[参数数] {c['file']}:{c['line']} R.string.{c['res']} 需 {need} 个占位符、"
                        f"实给 {len(c['args'])} 个：{text[:60]!r}")
        for a in c['args']:
            if a.isidentifier() and is_nullable_delegated(c['file'], a, c['offset']):
                errs.append(f"[委托属性] {c['file']}:{c['line']} 参数 {a} 是可空委托属性，"
                            f'智能转换不成立，需先存局部 val 或用 ?: 兜底')
        if c['args'] and formatted_false:
            errs.append(f"[formatted] {c['file']}:{c['line']} R.string.{c['res']} 标了 "
                        f'formatted="false" 却带 {len(c["args"])} 个实参（%% 不会还原）')
        if not c['args'] and not formatted_false and re.search(r'%(?!%)', text):
            errs.append(f"[裸%] {c['file']}:{c['line']} R.string.{c['res']} 无实参却含裸 %：{text[:60]!r}")

    # ---- 2. 格式串合法性（全表）----
    for name, (text, formatted_false) in res.items():
        if formatted_false:
            continue
        # 必须按 token 消耗：%% 是「字面百分号」整体，若用 %(?!%|...) 逐个看，
        # 第二个 % 会被误判成非法序列（实测虚报 7 条）。
        i = 0
        while i < len(text):
            if text[i] != '%':
                i += 1; continue
            if text.startswith('%%', i):
                i += 2; continue
            m = re.match(r'%\d+\$[a-zA-Z]', text[i:])
            if m:
                i += m.end(); continue
            errs.append(f'[格式串] {name} 含非法 % 序列：{text[max(0, i-20):i+12]!r}')
            i += 1

    # ---- 3. 上下文合法性（探针法）----
    bad_ctx = 0
    for f in sorted(ROOT.rglob('*.kt')):
        src = f.read_text(encoding='utf-8')
        probe, off, changed = [], 0, False
        for c in [c for c in calls if c['file'] == str(f.relative_to(ROOT))]:
            if c['kind'] != 'stringResource':
                continue
            probe.append(src[off:c['start']]); probe.append(SR_PROBE); off = c['end']; changed = True
        if not changed:
            continue
        probe.append(src[off:])
        probe = ''.join(probe)
        for r in kt_scan.scan(probe, kinds):
            if r['lit'] != SR_PROBE:
                continue
            ln = probe[:r['start']].count('\n') + 1
            if r['scope'] == 'sig':
                continue                      # @Composable 默认参数值合法（阶段一 CI 实测）
            if r['scope'] != 'body' or not r.get('comp') or r.get('noncomp'):
                bad_ctx += 1
                errs.append(f"[越界] {f.name}:{ln} fun={r['fun']} scope={r['scope']} "
                            f"comp={r.get('comp')} noncomp={r.get('noncomp')} blocks={r.get('blocks')}")

    print(f'stringResource 调用 {nsr} 个｜getString 调用 {ngs} 个｜strings.xml {len(res)} 条')
    if errs:
        print(f'\n⚠️ {len(errs)} 个问题（越界 {bad_ctx}）：')
        for e in errs[:40]:
            print('  ' + e)
        if len(errs) > 40:
            print(f'  …另有 {len(errs) - 40} 条')
        return 1
    print('全部通过 ✓（上下文越界 0、参数数全对、格式串合法、formatted 用法一致）')
    return 0


if __name__ == '__main__':
    sys.exit(main())
