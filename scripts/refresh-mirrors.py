#!/usr/bin/env python3
"""按 scripts/check-template.py 的 MIRRORS 表把 android 权威源码刷进 素材库 镜像。

MIRRORS 元组顺序是 (素材库镜像路径, android 权威路径)——方向容易记反，
反向复制会用旧镜像冲掉权威源码，所以统一走这个脚本，别手写 copyfile。
"""
import pathlib, re, shutil, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
block = re.search(r'MIRRORS\s*=\s*\[(.*?)\n\]',
                  (ROOT / 'scripts/check-template.py').read_text(encoding='utf-8'), re.S).group(1)
PAIRS = re.findall(r'\("([^"]+)",\s*"([^"]+)"\)', block)

n = 0
for mirror, canon in PAIRS:
    pm, pc = ROOT / mirror, ROOT / canon
    if not (pm.exists() and pc.exists()):
        print(f'  [缺失] {mirror} / {canon}'); continue
    if pm.read_bytes() != pc.read_bytes():
        shutil.copyfile(pc, pm)          # android 权威 → 素材库 镜像
        n += 1
        if '-q' not in sys.argv:
            print('  重刷', mirror)
print(f'共 {n} 组（表内 {len(PAIRS)} 组）')
