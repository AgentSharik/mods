#!/usr/bin/env python3
"""Пересчёт счётчиков README из фактического содержимого репозитория.

Единая методика (только готовые JAR):
- истина — файлы JAR в «Проекты/», а не ручная арифметика;
- готовые JAR StoneBlock 4: количество *.jar в «Проекты/StoneBlock 4»;
- готовые JAR по репозиторию: StoneBlock 4 + JAR в корне «Проекты/Отдельные моды»;
- ключи: суммарно по каждому JAR — количество ключей во всех lang/ru_ru.json,
  ключ считается один раз внутри JAR (файлы-дубли не раздувают цифру);
- моды, для которых JAR не собирался, в итоги не входят и отдельно не суммируются.

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
SNAPSHOT_JARS = 146  # готовых JAR по снимку релиза 1.0.0 (волны + пачка исправлений)

# Итоги по старой методике, которых не должно быть в README/RELEASE_CONTENTS.
FORBIDDEN = [
    '175 мод', 'всего 175', '153 мод',
    '168 готовых JAR + 7', '7 модов без языковых файлов',
    '54 400 английских', '55 295',
]


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
    sp_keys_by_jar = {os.path.basename(j): len(ru_keys(j)) for j in sp}
    sp_keys = sum(sp_keys_by_jar.values())
    return {
        'sb_jars': len(sb), 'sep_jars': len(sp), 'total_jars': len(sb) + len(sp),
        'sb_keys': sb_keys, 'sep_keys': sp_keys, 'total_keys': sb_keys + sp_keys,
        'sep_keys_by_jar': sp_keys_by_jar,
    }


def fmt(n):
    return f'{n:,}'.replace(',', ' ')


def apply_rules(text, st, fname):
    rules = []
    if fname == 'README.md':
        rules = [
            (r'(%D0%B3%D0%BE%D1%82%D0%BE%D0%B2%D0%BE-)\d+', r'\g<1>' + str(st['total_jars'])),
            (r'(%D0%BF%D0%B5%D1%80%D0%B5%D0%B2%D0%B5%D0%B4%D0%B5%D0%BD%D0%BE-)\d+',
             r'\g<1>' + str(st['total_keys'])),
            (r'\| Готовых JAR \| \*\*\d+\*\* \|', f"| Готовых JAR | **{st['sb_jars']}** |"),
            (r'\| Ключей переведено \| \*\*[\d ]+\*\* \|',
             f"| Ключей переведено | **{fmt(st['sb_keys'])}** |"),
        ]
        for name, count in sorted(st['sep_keys_by_jar'].items()):
            rules.append((
                r'\| `' + re.escape(name) + r'` \| [\d ]+ \|',
                f"| `{name}` | {fmt(count)} |",
            ))
    elif fname == 'RELEASE_CONTENTS.md':
        rules = [
            (r'\| Готовых JAR \| \*\*\d+\*\* \([^\n]*\) \|',
             f"| Готовых JAR | **{st['sb_jars']}** ({SNAPSHOT_JARS} по снимку релиза 1.0.0 "
             f"и {st['sb_jars'] - SNAPSHOT_JARS} дополнения после него; "
             f"считаются только готовые JAR) |"),
            (r'\| Ключей lang \| \*\*[\d ]+\*\* в русских файлах готовых JAR[^\n]*',
             f"| Ключей lang | **{fmt(st['sb_keys'])}** в русских файлах готовых JAR "
             f"(каждый ключ внутри одного JAR считается один раз) |"),
            (r'Итого после дополнений: \d+ готовых JAR в проекте StoneBlock 4 '
             r'\(\d+ исходных по снимку релиза \+ \d+ добавленных после него\), '
             r'[\d ]+ ключей в русских файлах готовых JAR\.',
             f"Итого после дополнений: {st['sb_jars']} готовых JAR в проекте StoneBlock 4 "
             f"({SNAPSHOT_JARS} исходных по снимку релиза + "
             f"{st['sb_jars'] - SNAPSHOT_JARS} добавленных после него), "
             f"{fmt(st['sb_keys'])} ключей в русских файлах готовых JAR."),
        ]
    out, changed = text, []
    for pat, rep in rules:
        new = re.sub(pat, rep, out)
        if new != out:
            changed.append(pat[:48])
        out = new
    return out, changed


def validate(text, st, fname):
    """Проверки, которые нельзя исправить автоподстановкой одного числа."""
    problems = []
    for bad in FORBIDDEN:
        if bad in text:
            problems.append(f'остался итог по старой методике: «{bad}»')
    if fname == 'README.md':
        if f"| Готовых JAR | **{st['sb_jars']}** |" not in text:
            problems.append('в разделе StoneBlock 4 не указано фактическое число готовых JAR')
        if f"| Ключей переведено | **{fmt(st['sb_keys'])}** |" not in text:
            problems.append('в разделе StoneBlock 4 не указано фактическое число ключей')
        for name, count in sorted(st['sep_keys_by_jar'].items()):
            if f"| `{name}` | {fmt(count)} |" not in text:
                problems.append(f'в разделе отдельных модов неверно указан JAR или число ключей: {name}')
    if fname == 'RELEASE_CONTENTS.md':
        missing = [os.path.basename(p) for p in jars(SB4)
                   if os.path.basename(p) not in text]
        if missing:
            problems.append(
                'в составе перевода не упомянуты готовые JAR: '
                + ', '.join(missing))
    return problems


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
        problems = validate(out, st, fname)
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
        for p in problems:
            rc = 1
            print(f'{fname}: ПРОТИВОРЕЧИЕ: {p}')
    sys.exit(rc)


if __name__ == '__main__':
    main()
