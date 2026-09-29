# -*- coding: utf-8 -*-
"""Разделение строк из .class на пользовательские (GUI/конфиг) и служебные (лог)."""
import glob, re, sys, zipfile, collections
sys.path.insert(0, '/home/user/work')
import class_rw

GUI = re.compile(r'(gui|screen|menu|config|tooltip|hud|widget|tooltip)', re.I)
SENT = re.compile(r'^[A-Z][A-Za-z0-9 ,\'"\-\(\)\.:/%\+]{12,}$')
WORD = re.compile(r'\b(the|and|of|to|is|you|for|with|that|this|not|are|from|your|can|will|when|into|on|in|it|a|or|be|at)\b', re.I)
NOISE = re.compile(r'^(java|javax|Ljava|Lorg|\(|\)|<|Code|LineNumberTable|SourceFile|LocalVariableTable|this|Lkotlin|invokedynamic|lambda)')

if __name__ == '__main__':
    ui, log = collections.defaultdict(set), collections.defaultdict(set)
    for p in sorted(glob.glob('out_party1/*.jar') + glob.glob('out_party3/*.jar')):
        jar = p.split('/')[-1]
        z = zipfile.ZipFile(p)
        for n in z.namelist():
            if not n.endswith('.class'):
                continue
            try:
                strs = list(class_rw.pool_strings(z.read(n)))
            except Exception:
                continue
            for s in strs:
                s = s.strip()
                if len(s) < 14 or not SENT.match(s) or NOISE.match(s):
                    continue
                if not WORD.search(s):
                    continue
                (ui if GUI.search(n) else log)[jar].add(s)
    tot_ui = sum(len(v) for v in ui.values())
    tot_log = sum(len(v) for v in log.values())
    print(f'строк в GUI/конфиг-классах: {tot_ui}')
    print(f'строк в остальных классах:   {tot_log}\n')
    print('=' * 25, 'GUI / КОНФИГ (подлежат переводу)', '=' * 25)
    for jar, s in sorted(ui.items(), key=lambda x: -len(x[1])):
        print(f'\n### {jar} — {len(s)}')
        for c in sorted(s):
            print('   ', c)
    print('\n' + '=' * 25, 'ПРОЧЕЕ (лог/ошибки — латиницей)', '=' * 25)
    for jar, s in sorted(log.items(), key=lambda x: -len(x[1])):
        print(f'\n### {jar} — {len(s)}')
        for c in sorted(s):
            print('   ', c)
