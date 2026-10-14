#!/usr/bin/env python3
"""Пересчёт счётчиков README из фактического содержимого репозитория.

Что считается (истина — файлы в Проекты/, а не ручные числа):
- готовые JAR: количество *.jar в «Проекты/StoneBlock 4» и «Проекты/Отдельные моды»;
- ключи: суммарно по каждому JAR — количество ключей во всех lang/ru_ru.json,
  ключ считается один раз внутри JAR (файлы-дубли не раздувают цифру).

Использование:
  python3 scripts/update_readme_stats.py --check   # только проверка, код 1 при расхождении
  python3 scripts/update_readme_stats.py --write   # переписать счётчики в README и RELEASE_CONTENTS
"""
import glob, json, os, re, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SB4 = os.path.join(ROOT, 'Проекты', 'StoneBlock 4')
SEP = os.path.join(ROOT, 'Проекты', 'Отдельные моды')
README = os.path.join(ROOT, 'README.md')
RELEASE = os.path.join(SB4, 'документация', 'RELEASE_CONTENTS.md')
NO_LANG_MODS = 7  # модов сборки без языковых файлов: в проекте JAR для них нет

def jars(d):
    return sorted(glob.glob(os.path.join(d, '*.jar')))

def ru_keys(jar):
    keys = set()
    with zipfile.ZipFile(jar) as z:
        for name in z.namelist():
            if name.endswith('/lang/ru_ru.json'):
                try:
                    keys |= set(json.loads(z.read(name)))
                except Exception:
                    pass
    return keys

def stats():
    sb, sp = jars(SB4), jars(SEP)
    sb_keys = sum(len(ru_keys(j)) for j in sb)
    sp_keys = sum(len(ru_keys(j)) for j in sp)
    return {
        'sb_jars': len(sb), 'sep_jars': len(sp), 'total_jars': len(sb) + len(sp),
        'sb_keys': sb_keys, 'sep_keys': sp_keys, 'total_keys': sb_keys + sp_keys,
        'sb_mods': len(sb) + NO_LANG_MODS,
    }

def fmt(n):
    return f'{n:,}'.replace(',', ' ')

def apply_rules(text, st, fname):
    rules = []
    if fname == 'README.md':
        rules = [
            (r'(Модов%20готово-)\d+', r'\g<1>' + str(st['total_jars'])),
            (r'(Ключей%20переведено-)[\d%20]+', lambda m: m.group(1) + fmt(st['total_keys']).replace(' ', '%20')),
            (r'168 готовых JAR \+ 7 модов без языковых файлов \(всего 175 модов\), 55 295 ключей',
             f"{st['sb_jars']} готовых JAR + 7 модов без языковых файлов (всего {st['sb_mods']} модов), {fmt(st['total_keys'])} ключей"),
            (r'175 модов \(168 готовых JAR и 7 модов без языковых файлов\)',
             f"{st['sb_mods']} модов ({st['sb_jars']} готовых JAR и 7 модов без языковых файлов)"),
            (r'\| Готовых JAR \| \*\*\d+\*\* \|', f"| Готовых JAR | **{st['sb_jars']}** |"),
            (r'\| Ключей переведено \| \*\*[\d ]+\*\* \|', f"| Ключей переведено | **{fmt(st['sb_keys'])}** |"),
            (r'отсюда 168 JAR и 175 модов в сводке\.', f"отсюда {st['sb_jars']} JAR и {st['sb_mods']} модов в сводке."),
            (r'\(все 153 мода, гайды, полнота, качество\)', f"(все {st['sb_mods']} мода, гайды, полнота, качество)"),
            (r'\| `Axiom-6\.1\.3-for-MC1\.21\.1\.jar` \| 1 234 \|',
             f"| `Axiom-6.1.3-for-MC1.21.1.jar` | {fmt(st['sep_keys'])} |"),
        ]
    elif fname == 'RELEASE_CONTENTS.md':
        rules = [
            (r'168 готовых JAR в проекте StoneBlock 4 \(146 исходных \+\s*22 добавленных\), 55 295 учтённых ключей \(52 206 \+ 3 089\)\.',
             f"{st['sb_jars']} готовых JAR в проекте StoneBlock 4 (146 исходных по снимку релиза + 22 добавленных после него), {fmt(st['sb_keys'])} ключей в русских файлах готовых JAR."),
        ]
    out, changed = text, []
    for pat, rep in rules:
        new = re.sub(pat, rep, out)
        if new != out:
            changed.append(pat[:48])
        out = new
    return out, changed

def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else '--check'
    st = stats()
    print(f"Факт: JAR StoneBlock 4 = {st['sb_jars']}, отдельных = {st['sep_jars']}, всего = {st['total_jars']}")
    print(f"Русских ключей: StoneBlock 4 = {st['sb_keys']}, отдельные = {st['sep_keys']}, всего = {st['total_keys']}")
    rc = 0
    for path in (README, RELEASE):
        fname = os.path.basename(path)
        with open(path, encoding='utf-8') as f:
            src = f.read()
        out, changed = apply_rules(src, st, fname)
        if mode == '--write':
            if changed:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(out)
                print(f'{fname}: обновлено правил: {len(changed)}')
            else:
                print(f'{fname}: без изменений')
        else:
            if changed:
                rc = 1
                print(f'{fname}: УСТАРЕЛО, не совпадает правил: {len(changed)}')
            else:
                print(f'{fname}: счётчики совпадают с фактом')
    sys.exit(rc)

if __name__ == '__main__':
    main()
