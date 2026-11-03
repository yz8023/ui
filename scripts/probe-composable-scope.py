#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
探针·组合上下文越界（DrawScope / 手势 / 副作用 lambda 里取组合态）。

**这类错本地 kotlinc 永远抓不到**：没有 Compose classpath 时，合法与非法的
`MaterialTheme` / `stringResource` 都表现为 unresolved reference，长得一模一样；
只有真编译（CI）能分辨，代价是一轮 3–5 分钟的 CI。批 D 的 S06 就栽在这里
（`Canvas { drawArc(color = MaterialTheme.colorScheme.primary) }`）。

规则：`MaterialTheme.colorScheme` 与 `stringResource(...)` 都是 @Composable getter／函数，
只能在组合上下文里调用。下列 lambda **不是**组合上下文，块内出现它们即报错：

  Canvas / drawWithContent / drawBehind / graphicsLayer / onGloballyPositioned /
  onSizeChanged / pointerInput / detect*Gestures / LaunchedEffect / SideEffect /
  DisposableEffect / remember / rememberSaveable / derivedStateOf / snapshotFlow /
  NestedScrollConnection 的方法体

改法：在组合体里先把值上提成局部 val（颜色、文案、尺寸都适用），lambda 内只用 val；
需要 Context 的地方用 `LocalContext.current` 在外面取好再 `context.getString(...)`。

用法：python3 scripts/probe-composable-scope.py [路径...]（默认扫 android/）
退出码：0 = 无越界；1 = 有越界（逐条打印文件:行号）。
"""
import pathlib
import re
import sys

# 先剥注释：KDoc 里写「Canvas 实绘」会让正则匹到注释，再把函数体误当成块（批 D 踩过）
def strip_comments(src: str) -> str:
    src = re.sub(r'/\*.*?\*/', lambda m: re.sub(r'[^\n]', ' ', m.group(0)), src, flags=re.S)
    src = re.sub(r'//[^\n]*', lambda m: ' ' * len(m.group(0)), src)
    return src

OPENER = re.compile(
    r'(?<![\w.])(Canvas|drawWithContent|drawBehind|graphicsLayer|onGloballyPositioned|onSizeChanged|'
    r'pointerInput|detectDragGesturesAfterLongPress|detectDragGestures|detectTapGestures|'
    r'detectVerticalDragGestures|detectHorizontalDragGestures|detectTransformGestures|'
    r'LaunchedEffect|SideEffect|DisposableEffect|remember|rememberSaveable|derivedStateOf|snapshotFlow|'
    r'onPreScroll|onPostScroll|onPreFling|onPostFling)\s*[( {]'
)
BAD = re.compile(r'MaterialTheme|stringResource|LocalDensity\.current|LocalContext\.current')


def scan_file(path: pathlib.Path):
    src = strip_comments(path.read_text(encoding='utf-8'))
    hits = []
    for m in OPENER.finditer(src):
        i = src.find('{', m.end() - 1)
        if i < 0:
            continue
        # 块起点必须紧跟在匹配之后（同一行或下一行），否则说明匹到的是函数名/声明而非调用
        if src[m.end():i].count('\n') > 1:
            continue
        depth, j = 0, i
        while j < len(src):
            if src[j] == '{':
                depth += 1
            elif src[j] == '}':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        for bm in BAD.finditer(src[i:j + 1]):
            line = src[:i + bm.start()].count('\n') + 1
            hits.append((line, m.group(1), bm.group(0)))
    return hits


def main():
    roots = [pathlib.Path(a) for a in sys.argv[1:]] or [pathlib.Path('android')]
    files = sorted({p for r in roots for p in r.rglob('*.kt')})
    total = 0
    for f in files:
        for line, opener, bad in scan_file(f):
            print(f'  ⚠ {f}:{line} {opener} 块内出现 {bad}（非组合上下文）')
            total += 1
    print(f'扫描 {len(files)} 个 .kt；越界 {total} 处' + ('  ✓ 全部通过' if total == 0 else '  ✗ 需修'))
    return 1 if total else 0


if __name__ == '__main__':
    sys.exit(main())
