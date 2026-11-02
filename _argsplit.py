import re
def split_args(src, start):
    """从 `(` 之后开始，按顶层逗号切实参；正确跳过 "…"、\"\"\"…\"\"\"、${…} 模板。"""
    i, d, args, cur, state = start, 1, [], [], None
    while i < len(src) and d:
        c = src[i]
        if state == 'raw':
            if src.startswith('"""', i):
                cur.append('"""'); i += 3; state = None; continue
            cur.append(c); i += 1; continue
        if state == 'str':
            cur.append(c)
            if c == '\\': cur.append(src[i+1]); i += 2; continue
            if c == '"': state = None
            i += 1; continue
        if src.startswith('"""', i): cur.append('"""'); state = 'raw'; i += 3; continue
        if c == '"': cur.append(c); state = 'str'; i += 1; continue
        if c in '([{': d += 1
        elif c in ')]}': d -= 1
        if d == 0: args.append(''.join(cur)); break
        if c == ',' and d == 1: args.append(''.join(cur).strip()); cur = []; i += 1; continue
        cur.append(c); i += 1
    return args

def entry_positions(src):
    """所有 `Ui2Feature(` 构造点（排除 `data class Ui2Feature(` 声明）。"""
    out = []
    for m in re.finditer(r'Ui2Feature\(', src):
        pre = src[max(0, m.start()-12):m.start()]
        if 'class' in pre or 'List<' in pre: continue
        out.append(m)
    return out

def arg_value(a):
    """实参源码文本 → Python 值（只处理纯字面量；含模板/表达式的抛错）。"""
    a = a.strip()
    if a.startswith('"""'):
        assert a.endswith('"""') and len(a) >= 6, a[:60]
        v = a[3:-3]
        assert '${' not in v and not re.search(r'\$[A-Za-z_]', v), f'raw 串含模板: {v[:60]!r}'
        return v
    assert a.startswith('"') and a.endswith('"'), a[:60]
    v = a[1:-1]
    assert '${' not in v and not re.search(r'\$[A-Za-z_]', v), f'串含模板: {v[:60]!r}'
    return (v.replace('\\n', '\n').replace('\\"', '"').replace("\\'", "'")
             .replace('\\\\', '\\').replace('\\t', '\t'))
