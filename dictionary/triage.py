# -*- coding: utf-8 -*-
"""Триаж модпака: у каждого мода смотрим lang-файлы и считаем,
насколько он уже русский.

Категории:
  RU_FULL  — есть ru_ru.json и он реально русский (трен��ать НЕЛЬЗЯ)
  RU_PART  — ru_ru.json есть, но часть строк осталась английской (дозаполнять)
  NO_RU    — русского нет вовсе (переводить с нуля)
  NO_LANG  — lang-файлов нет (библиотеки, служебные — не трогать)

Запуск: python3 triage.py
"""
import zipfile, json, re, pathlib, csv, sys

MODS = pathlib.Path('/home/user/dl/pack/mods')
OUT = pathlib.Path('/home/user/dl/triage.csv')

CYR = re.compile(r'[А-Яа-яЁё]')
LAT_WORD = re.compile(r'[A-Za-z]{3,}')


def analyze(jar):
    """-> (category, keys, cyr, untranslated_examples)"""
    try:
        z = zipfile.ZipFile(jar)
    except Exception:
        return 'BAD', 0, 0, []
    ru_names, en_vals = [], []
    for n in z.namelist():
        low = n.lower()
        if low.endswith('.json') and '/lang/' in low:
            try:
                d = json.loads(z.read(n).decode('utf-8'))
            except Exception:
                continue
            if not isinstance(d, dict):
                continue
            if low.endswith('ru_ru.json'):
                ru_names.append((n, d))
            elif low.endswith('en_us.json'):
                en_vals.append((n, d))
    # считаем строки в ru_ru с кириллицей и без
    total = cyr = 0
    missing = []
    en_map = {}
    for _, d in en_vals:
        for k, v in d.items():
            if isinstance(v, str):
                en_map.setdefault(k, v)
    for _, d in ru_names:
        for k, v in d.items():
            if not isinstance(v, str) or not v.strip():
                continue
            total += 1
            if CYR.search(v):
                cyr += 1
            else:
                src = en_map.get(k, v)
                if LAT_WORD.search(src):
                    missing.append((k, src, v))
    if not ru_names:
        return ('NO_LANG' if not en_vals else 'NO_RU'), len(en_map), 0, missing
    if not total:
        return 'RU_FULL', 0, 0, []
    if cyr == total:
        return 'RU_FULL', total, cyr, []
    return 'RU_PART', total, cyr, missing


def main():
    rows = []
    for jar in sorted(MODS.glob('*.jar')):
        cat, keys, cyr, missing = analyze(jar)
        rows.append((jar.name, cat, keys, cyr, len(missing)))
        print(f'{cat:8s} {jar.name[:58]:58s} ключей {keys:5d} кириллица {cyr:5d}')
    with open(OUT, 'w', encoding='utf-8', newline='') as f:
        w = csv.writer(f)
        w.writerow(['jar', 'category', 'keys', 'cyr', 'untranslated'])
        w.writerows(rows)
    from collections import Counter
    c = Counter(r[1] for r in rows)
    print('\n' + '=' * 50)
    for k, v in c.most_common():
        print(f'  {k:8s} {v}')
    print(f'\nвсего модов: {len(rows)}  -> {OUT}')


if __name__ == '__main__':
    main()
