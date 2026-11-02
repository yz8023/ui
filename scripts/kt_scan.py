#!/usr/bin/env python3
"""Kotlin 轻量扫描器：定位每个字符串字面量，并判定它所处的上下文。

为什么要自己写：用 head.count('{') 这种朴素深度统计会把**字符串与注释里的花括号**
一并算进去，实测导致 AppIconSwitcher.kt / Dialogs.kt 的 @Composable 上下文被误判、
整批字面量被漏掉。本扫描器逐字符推进，跳过注释、字符串、字符字面量后再维护括号栈。

对每个字面量给出：
  scope  = 'body'    在函数体内
           'sig'     在函数签名的默认参数值里
           'top'     不在任何函数内（顶层属性 / data class / object 成员 / enum 项）
  comp   = 所属函数是否 @Composable（scope='top' 时为 None）
  fun    = 所属函数名
"""
import pathlib
import re
from typing import List, Dict, Optional

DECL_KEYWORDS = {'val', 'var', 'class', 'object', 'interface', 'enum', 'return',
                 'if', 'for', 'while', 'when', 'do', 'else', 'try', 'catch', 'finally',
                 'throw', 'package', 'import', 'typealias'}
MODIFIERS = {'private', 'public', 'internal', 'protected', 'inline', 'suspend', 'operator',
             'infix', 'external', 'expect', 'actual', 'override', 'open', 'abstract',
             'final', 'data', 'sealed', 'annotation', 'companion', 'const', 'lateinit',
             'vararg', 'noinline', 'crossinline', 'tailrec', 'get', 'set'}


# 这些块是**非组合上下文**的 lambda：里面不能调 stringResource()。
# 典型事故：remember { mutableStateOf("全部") } 与 onClick = { toast("已复制") }。
NON_COMP_HEAD = re.compile(
    r'\b(?:remember\w*|runCatching|buildString|buildList|buildMap|lazy|'
    r'derivedStateOf|LaunchedEffect|SideEffect|DisposableEffect|produceState|snapshotFlow|launch|withContext|'
    r'coroutineScope|runBlocking|forEach|forEachIndexed|map|mapNotNull|filter|let|apply|also|onEach|'
    r'firstOrNull|firstNotNullOfOrNull|find|any|all|none|sumOf|sortedBy|sortedByDescending|maxByOrNull|'
    r'associateBy|groupBy|takeIf|run|repeat|with|collect|collectLatest|launchIn|'
    r'clickable|combinedClickable|selectable|toggleable|triStateToggleable|draggable|scrollable|'
    r'swipeable|anchoredDraggable|pointerInput|detectTapGestures|graphicsLayer|drawWithContent|'
    r'drawBehind|drawWithCache|onSizeChanged|onGloballyPositioned|onPlaced|layout|semantics|'
    r'clearAndSetSemantics|'
    # Lazy* 的作用域 lambda 是 LazyListScope/LazyGridScope.() -> Unit，**不是** @Composable；
    # 但它们的 item/items/stickyHeader 的 content 是可组合的，所以只登记 Lazy* 本身。
    # CI 实测：`LazyColumn { listOf(stringResource(...)).forEach { } }` 直接炸 3 条。
    r'LazyColumn|LazyRow|LazyVerticalGrid|LazyHorizontalGrid|'
    r'LazyVerticalStaggeredGrid|LazyHorizontalStaggeredGrid)\b[^{}]*$'
)
NON_COMP_PARAM = re.compile(r'\bon[A-Z]\w*\s*=?\s*$')


def _split_top(s: str):
    parts, d, cur = [], 0, ''
    for ch in s:
        if ch in '(<[': d += 1
        elif ch in ')>]': d -= 1
        if ch == ',' and d == 0:
            parts.append(cur); cur = ''
        else:
            cur += ch
    if cur.strip(): parts.append(cur)
    return parts


def build_lambda_kinds(root) -> dict:
    """{函数名: 尾随 lambda 是否 @Composable}。

    为什么必须解析签名而不能靠关键词表：PrimaryButton/SecondaryButton 的尾参是
    `content: @Composable RowScope.() -> Unit`（可组合），而 TKChip/TKButton/OTButton/
    QButton 的尾参是 `onClick: () -> Unit`（不可组合）。两者写法看起来一样：
    `XxxButton(...) { ... }`，只有被调函数的签名能区分。CI 曾因此报 30 条
    "@Composable invocations can only happen from the context of a @Composable function"。
    """
    kinds = {}
    for f in pathlib.Path(root).rglob('*.kt'):
        src = f.read_text(encoding='utf-8')
        for m in re.finditer(r'fun\s+(?:<[^>]*>\s*)?(?:[\w<>.]+\.)?([A-Za-z_]\w*)\s*\(', src):
            i, d = m.end(), 1
            while i < len(src) and d:
                if src[i] == '(': d += 1
                elif src[i] == ')': d -= 1
                i += 1
            parts = _split_top(src[m.end():i - 1])
            if not parts: continue
            last = parts[-1]
            if '->' not in last: continue
            kinds.setdefault(m.group(1), '@Composable' in last)
    return kinds


# 控制流块：既不授予也不剥夺组合上下文，判定时应「透明」跳过。
# 少了这一态，`LazyColumn { if (x) { stringResource() } }` 里的 if 会被判成 comp，
# 把外层 LazyColumn 的 noncomp 洗白；反过来用 any() 又会把 `LazyColumn { item { … } }`
# 里合法的 item 内容一起毒化。所以必须三态 + 取最内层非控制流块。
CTL_BLOCK = re.compile(r'\b(?:if|else|when|for|while|do|try|catch|finally)\s*(?:\([^{}]*\))?\s*$')


def innermost_noncomp(stack) -> bool:
    """由最内层的**具名 lambda 块**决定组合上下文，控制流块（if/when/for…）透明跳过。"""
    for b in reversed(stack):
        if b[0] == 'brace' and len(b) > 2 and b[2] in ('comp', 'noncomp'):
            return b[2] == 'noncomp'
    return False


def block_kind(lead: str, kinds: dict = None) -> str:
    """判断一个 `{` 的上下文：'noncomp' / 'comp' / 'ctl'（控制流，透明）。"""
    s = lead.rstrip()
    if CTL_BLOCK.search(s):
        return 'ctl'
    if NON_COMP_HEAD.search(s) or NON_COMP_PARAM.search(s):
        return 'noncomp'
    if kinds and s.endswith(')'):
        d, i = 0, len(s) - 1
        while i >= 0:
            if s[i] == ')': d += 1
            elif s[i] == '(':
                d -= 1
                if d == 0: break
            i -= 1
        mc = re.search(r'([A-Za-z_]\w*)\s*$', s[:i])
        if mc and mc.group(1) in kinds and not kinds[mc.group(1)]:
            return 'noncomp'
    return 'comp'


def _skip_string(src: str, i: int, raw: bool) -> tuple:
    """从引号处扫到字符串结束，正确跳过 `${...}` 模板表达式（其中可能再嵌套字符串）。

    此前不跳模板，导致 `"已导出 ${"%.1f".format(x)} MB"` 被切成三段假字面量，
    替换后把外层模板撕开（kotlinc 报 syntax error: Expecting '"'）。
    返回 (结束位置, 是否含插值)。模板内的字面量一律不单独记录，留给 D 阶段处理。
    """
    n = len(src)
    quote = '"""' if raw else '"'
    j = i + len(quote)
    interp = False
    while j < n:
        c = src[j]
        if not raw and c == '\\':
            j += 2; continue
        if src.startswith(quote, j):
            return j + len(quote), interp
        if not raw and c == '\n':
            return j, interp          # 未闭合，保底
        if c == '$' and j + 1 < n and (src[j + 1] == '{' or src[j + 1].isalpha() or src[j + 1] == '_'):
            if not (src.startswith("${'$'}", j)):
                interp = True
            if src[j + 1] == '{':
                k, d = j + 2, 1
                while k < n and d:
                    if src[k] == '"':
                        k, _ = _skip_string(src, k, src.startswith('"""', k))
                        continue
                    if src[k] == '\'':
                        k += 2 if src[k + 1:k + 2] == '\\' else 2
                        continue
                    if src[k] == '{': d += 1
                    elif src[k] == '}': d -= 1
                    k += 1
                j = k; continue
            j += 2; continue
        j += 1
    return n, interp

def scan(src: str, kinds: dict = None) -> List[Dict]:
    i, n = 0, len(src)
    out: List[Dict] = []
    ann: List[str] = []
    # 栈元素: ('brace', fun_index_or_None) | ('paren', fun_index_or_None)
    stack: List[list] = []
    funs: List[Dict] = []          # {'name','comp','state'}  state: sig/await/body
    top_fun: Optional[int] = None  # 当前最内层函数在 funs 里的下标

    def cur_fun():
        for idx in range(len(funs) - 1, -1, -1):
            if funs[idx]['state'] in ('sig', 'await', 'body'):
                return idx
        return None

    while i < n:
        c = src[i]
        # ---- 注释 ----
        if src.startswith('/*', i):
            j = src.find('*/', i + 2)
            i = n if j < 0 else j + 2
            continue
        if src.startswith('//', i):
            j = src.find('\n', i)
            i = n if j < 0 else j
            continue
        # ---- 字符串（记录并跳过；模板 ${} 内的代码本阶段不参与判定）----
        if src.startswith('"""', i) or c == '"':
            raw = src.startswith('"""', i)
            j, interp = _skip_string(src, i, raw)
            r = _rec(src, i, j, raw, funs, cur_fun())
            r['noncomp'] = innermost_noncomp(stack)
            r['blocks'] = [b[3] for b in stack if b[0] == 'brace' and len(b) > 3]  # 外层块 callee 链，审计用
            r['interp'] = interp
            out.append(r)
            i = j
            continue
        # ---- 字符字面量 ----
        if c == "'":
            j = i + 1
            while j < n:
                if src[j] == '\\':
                    j += 2; continue
                if src[j] == "'":
                    j += 1; break
                if src[j] == '\n':
                    break
                j += 1
            i = j
            continue
        # ---- 注解 ----
        if c == '@':
            m = re.match(r'@([A-Za-z_]\w*)', src[i:])
            if m:
                ann.append(m.group(1)); i += m.end(); continue
        # ---- fun 声明 ----
        m = re.match(r'fun\s+(?:<[^>]*>\s*)?(?:[\w<>.]+\.)?([A-Za-z_]\w*)\s*\(', src[i:])
        if m:
            funs.append({'name': m.group(1), 'comp': 'Composable' in ann, 'state': 'sig'})
            ann = []
            stack.append(['paren', len(funs) - 1])
            i += m.end()
            continue
        if c == '(':
            stack.append(['paren', None]); i += 1; continue
        if c == ')':
            for k in range(len(stack) - 1, -1, -1):
                if stack[k][0] == 'paren':
                    fi = stack[k][1]
                    del stack[k]
                    if fi is not None and funs[fi]['state'] == 'sig':
                        funs[fi]['state'] = 'await'
                    break
            i += 1; continue
        if c == '{':
            fi = None
            idx = cur_fun()
            if idx is not None and funs[idx]['state'] == 'await':
                funs[idx]['state'] = 'body'; fi = idx
            lead = src[max(0, i - 300):i]
            _m = re.search(r'([A-Za-z_][\w.]*)\s*(?:\([^{}]*?\))?\s*$', lead.rstrip())
            if fi is not None:
                # 这个 `{` 是**函数体**，不是尾随 lambda。必须由函数自身的可组合性决定，
                # 不能走 block_kind 回溯 callee：`private fun QButton(…, onClick: () -> Unit) {`
                # 会被查成「QButton 的尾随 lambda 不可组合」，从而把它 @Composable 函数体
                # 里的合法 stringResource 误判为越界（CI 实测合法）。
                kind = 'comp' if funs[fi]['comp'] else 'noncomp'
            else:
                kind = block_kind(lead, kinds)
            stack.append(['brace', fi, kind, _m.group(1) if _m else '?'])
            i += 1; continue
        if c == '}':
            for k in range(len(stack) - 1, -1, -1):
                if stack[k][0] == 'brace':
                    fi = stack[k][1]
                    del stack[k]
                    if fi is not None:
                        funs[fi]['state'] = 'done'
                    break
            ann = []
            i += 1; continue
        if c == ';':
            ann = []; i += 1; continue
        # ---- 标识符 / 关键字 ----
        m = re.match(r'[A-Za-z_]\w*', src[i:])
        if m:
            tok = m.group(0)
            if tok in DECL_KEYWORDS:
                ann = []
            elif tok not in MODIFIERS:
                # 普通标识符出现即说明注解不再修饰紧随其后的声明
                if ann and tok not in ('Composable',):
                    pass
            i += m.end(); continue
        i += 1
    return out


def _rec(src, a, b, raw, funs, idx) -> Dict:
    if idx is None:
        scope, comp, fname = 'top', None, None
    else:
        st = funs[idx]['state']
        scope = 'sig' if st == 'sig' else ('body' if st in ('body', 'await') else 'top')
        comp, fname = funs[idx]['comp'], funs[idx]['name']
    lit = src[a:b]
    body = lit[3:-3] if raw else lit[1:-1]
    line = src[:a].count('\n') + 1
    ls = src.rfind('\n', 0, a) + 1
    prefix = src[ls:a]
    ma = re.search(r'([A-Za-z_]\w*)\s*(?::[^=()]{0,80})?=\s*$', prefix)
    if ma:
        arg = ma.group(1)
    else:
        mc = re.search(r'([A-Z][A-Za-z]*)\s*\(\s*$', prefix)
        arg = (mc.group(1)[0].lower() + mc.group(1)[1:]) if mc else 'text'
    return dict(start=a, end=b, line=line, raw=raw, lit=lit, text=body,
                scope=scope, comp=comp, fun=fname, arg=arg)


CJK = re.compile(r'[\u4e00-\u9fff]')


def collect(root):
    """扫全仓，返回所有含中文的字面量记录（带 cat 分类）。不依赖任何临时文件。"""
    root = pathlib.Path(root)
    kinds = build_lambda_kinds(root)
    recs = []
    for f in sorted(root.rglob('*.kt')):
        src = f.read_text(encoding='utf-8')
        rel = str(f.relative_to(root))
        for r in scan(src, kinds):
            if not CJK.search(r['text']):
                continue
            r['file'] = rel
            if r['scope'] == 'body' and r['comp']:
                r['cat'] = 'A 组合体内'
            elif r['scope'] == 'sig' and r['comp']:
                r['cat'] = 'C1 组合默认参数'
            elif r['scope'] == 'body':
                r['cat'] = 'B 普通函数体内'
            elif r['scope'] == 'sig':
                r['cat'] = 'C2 普通函数默认参数'
            else:
                r['cat'] = 'C3 顶层/数据'
            if r.get('interp'):
                r['cat'] = 'D 带插值（需格式参数）'
            recs.append(r)
    return recs


if __name__ == '__main__':
    import pathlib, sys, collections, json
    root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else
                        'android/app/src/main/java/com/monkeycode/liquidui')
    CJK = re.compile(r'[\u4e00-\u9fff]')
    recs, per_file = [], collections.Counter()
    for f in sorted(root.rglob('*.kt')):
        src = f.read_text(encoding='utf-8')
        rel = str(f.relative_to(root))
        kinds = kinds if 'kinds' in dir() else build_lambda_kinds(root)
        for r in scan(src, kinds):
            if not CJK.search(r['text']):
                continue
            r['file'] = rel
            recs.append(r)
            key = ('A 组合体内' if r['scope'] == 'body' and r['comp'] else
                   'C1 组合默认参数' if r['scope'] == 'sig' and r['comp'] else
                   'D 组合体内带插值' if r['scope'] == 'body' and r['comp'] is True and '${' in r['lit'] else
                   'B 普通函数体内' if r['scope'] == 'body' else
                   'C2 普通函数默认参数' if r['scope'] == 'sig' else
                   'C3 顶层/数据')
            r['cat'] = key
            per_file[key] += 1
    # 重新归类：带插值的单独成 D
    for r in recs:
        if '${' in r['lit'] and r['cat'].startswith(('A', 'C1')):
            per_file[r['cat']] -= 1
            r['cat'] = 'D 带 ${} 插值（需格式参数）'
            per_file[r['cat']] += 1
    print('=== 扫描器分类结果 ===')
    for k, v in per_file.most_common():
        print(f'  {v:5d}  {k}')
    print('  总计', len(recs))
    json.dump(recs, open('/tmp/scan_all.json', 'w'), ensure_ascii=False)
    # 自检：三个已知现场
    print('\n=== 自检（此前被漏判的三处）===')
    for want in [('ui/features/AppIconSwitcher.kt', '桌面图标'),
                 ('ui/components/Dialogs.kt', '确定'),
                 ('ui/navigation/Screen.kt', '首页')]:
        hit = [r for r in recs if r['file'] == want[0] and r['text'] == want[1]]
        for h in hit[:1]:
            print(f"  {want[0]}:{h['line']} {h['text']!r} → scope={h['scope']} comp={h['comp']} fun={h['fun']} cat={h['cat']}")
        if not hit:
            print(f'  {want[0]} {want[1]!r} 未命中！')
