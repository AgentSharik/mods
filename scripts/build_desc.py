#!/usr/bin/env python3
"""Перевод описания мода, у которого нет lang-файлов.

    python3 scripts/build_desc.py <src.jar> <out.jar> "<старое описание>" "<новое>"

Ищет старое описание во всех текстовых файлах внутри JAR (на практике это
META-INF/neoforge.mods.toml) и заменяет его на новое. Оригинал не изменяется.
"""
import os, shutil, subprocess, sys, tempfile, zipfile

TEXT = ('.toml', '.json', '.txt', '.cfg', '.properties', '.mcmeta')


def build(src, out, old, new):
    src = os.path.abspath(src)
    out = os.path.abspath(out)
    tmp = tempfile.mkdtemp(prefix='build_desc_')
    work = os.path.join(tmp, 'work')
    os.makedirs(work)
    with zipfile.ZipFile(src) as z:
        z.extractall(work)

    hits = 0
    for root, _, files in os.walk(work):
        for f in files:
            p = os.path.join(root, f)
            if not f.endswith(TEXT):
                continue
            try:
                s = open(p, encoding='utf-8').read()
            except (UnicodeDecodeError, OSError):
                continue
            if old in s:
                open(p, 'w', encoding='utf-8').write(s.replace(old, new))
                hits += 1
                print('  заменено в:', os.path.relpath(p, work))
    if not hits:
        sys.exit('описание не найдено ни в одном текстовом файле — сборка отменена')

    if os.path.exists(out):
        os.remove(out)
    subprocess.run(['zip', '-q', '-r', '-X', out, '.'], cwd=work, check=True)
    with zipfile.ZipFile(out) as z:
        assert z.testzip() is None
        print(f'  в архиве записей: {len(z.namelist())}, testzip ok'
              + (f', вхождений нового описания: '
                 f'{z.read("META-INF/neoforge.mods.toml").decode("utf-8").count(new)}'
                 if 'META-INF/neoforge.mods.toml' in z.namelist() else ''))
    shutil.rmtree(tmp, ignore_errors=True)
    print('готово:', out)


if __name__ == '__main__':
    if len(sys.argv) != 5:
        sys.exit(__doc__)
    build(*sys.argv[1:5])
