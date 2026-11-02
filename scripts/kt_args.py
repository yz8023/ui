#!/usr/bin/env python3
"""Kotlin 实参切分与字面量求值（给 `externalize-model.py` 用，也可单独 import）。

与 `kt_scan.py` 的分工：`kt_scan` 回答「这个字面量在什么上下文里」（能不能外置），
本模块回答「这个构造调用的实参分别是什么、值是多少」（怎么外置）。

三个函数：
  `entry_positions(src, cls)`  所有 `Cls(` 构造点，排除 `data class Cls(` 与 `List<Cls>`；
  `split_args(src, start)`     从 `(` 之后按顶层逗号切实参，正确跳过 `"…"`、`\"\"\"…\"\"\"`、`${…}`；
  `parse_concat(expr)`         把 `"a" + "b" + \"\"\"c\"\"\"` 求值成 `abc`；含非字面量片段就抛错。
"""
import re


def split_args(src, start):
    """从 `(` 之后（start 指向 `(` 的下一个字符）按顶层逗号切实参。

    返回 `(实参源码文本列表, 收尾 `)` 之后的偏移)`，方便调用方拿到整个构造调用的 span。
    正确跳过普通串、raw 串（`\"\"\"`）与其内部的逗号/括号；`${…}` 模板整体跳过。
    """
    i, d, args, cur, state = start, 1, [], [], None
    while i < len(src) and d:
        c = src[i]
        if state == 'raw':
            if src.startswith('"""', i):
                cur.append('"""')
                i += 3
                state = None
                continue
            cur.append(c)
            i += 1
            continue
        if state == 'str':
            cur.append(c)
            if c == '\\':
                cur.append(src[i + 1])
                i += 2
                continue
            if c == '"':
                state = None
            i += 1
            continue
        if src.startswith('"""', i):
            cur.append('"""')
            state = 'raw'
            i += 3
            continue
        if c == '"':
            cur.append(c)
            state = 'str'
            i += 1
            continue
        if c in '([{':
            d += 1
        elif c in ')]}':
            d -= 1
        if d == 0:
            args.append(''.join(cur))
            return args, i + 1
        if c == ',' and d == 1:
            args.append(''.join(cur).strip())
            cur = []
            i += 1
            continue
        cur.append(c)
        i += 1
    raise AssertionError('实参括号没闭合')


def entry_positions(src, cls):
    """所有 `cls(` 构造点的 match 对象，排除 `data class cls(` 声明与 `List<cls>` 之类的类型位置。"""
    out = []
    for m in re.finditer(re.escape(cls) + r'\(', src):
        pre = src[max(0, m.start() - 12):m.start()]
        if 'class' in pre or 'List<' in pre or 'fun ' in pre:
            continue
        out.append(m)
    return out


def _unescape(v):
    """Kotlin 普通串的转义还原（raw 串不走这里）。"""
    out, i = [], 0
    while i < len(v):
        c = v[i]
        if c == '\\' and i + 1 < len(v):
            n = v[i + 1]
            out.append({'n': '\n', 't': '\t', 'r': '\r', '"': '"', "'": "'",
                        '\\': '\\', 'b': '\b', '0': '\0'}.get(n, n))
            i += 2
            continue
        out.append(c)
        i += 1
    return ''.join(out)


def _scan_literal(expr, i):
    """从 expr[i] 处读一个字面量，返回 (值, 结束偏移)。"""
    if expr.startswith('"""', i):
        j = i + 3
        while j < len(expr):
            if expr.startswith('"""', j):
                break
            j += 1
        raw = expr[i + 3:j]
        # `${'$'}` 是 Kotlin 输出字面 $ 的写法（提示词里常带 `$item` 这类代码示例），值里就是一个 $。
        # 模板检查要在**挖掉它之后**做，否则还原出来的字面 $ 会被误判成未解析模板。
        v = raw.replace("${'$'}", '$')
        probe = raw.replace("${'$'}", '')
        assert '${' not in probe and not re.search(r'\$[A-Za-z_{]', probe), \
            f'raw 串含真模板，需按 D 类处理: {probe[:80]!r}'
        return v, j + 3
    assert expr[i] == '"', f'不是字符串字面量: {expr[i:i + 40]!r}'
    j = i + 1
    while j < len(expr):
        if expr[j] == '\\':
            j += 2
            continue
        if expr[j] == '"':
            break
        j += 1
    raw = expr[i + 1:j]
    v = raw.replace("${'$'}", '$')
    probe = raw.replace("${'$'}", '')
    assert '${' not in probe and not re.search(r'(?<!\\)\$[A-Za-z_{]', probe), \
        f'串含真模板，需按 D 类处理: {probe[:80]!r}'
    return _unescape(v), j + 1


def parse_concat(expr):
    """`"a" + "b" + \"\"\"c\"\"\"` → `abc`；空实参 → None；含非字面量片段 → 抛 AssertionError。

    Spec 里的长字段（why / keyImpl / pitfalls / params）都是多段 `+` 拼接，
    必须整段合并成**一个**资源，一段一条会把语义切碎。
    """
    expr = expr.strip()
    if not expr:
        return None
    parts, i = [], 0
    while i < len(expr):
        while i < len(expr) and expr[i] in ' \n\t\r':
            i += 1
        if i >= len(expr):
            break
        if expr[i] == '+':
            i += 1
            continue
        v, i = _scan_literal(expr, i)
        parts.append(v)
    assert parts, f'没解析出任何字面量: {expr[:60]!r}'
    return ''.join(parts)


def split_named(arg):
    """`name = "…"` → ('name', '"…"')；位置实参 → (None, arg)。"""
    arg = arg.strip()
    m = re.match(r'^([A-Za-z_]\w*)\s*=\s*(?=")', arg, re.S)
    if m:
        return m.group(1), arg[m.end():]
    return None, arg
