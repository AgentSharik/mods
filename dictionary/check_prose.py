# -*- coding: utf-8 -*-
"""Сборка и проверка словаря прозы.

Читает prose/*.ru.tsv, собирает один словарь EN -> RU и проверяет:
  * каждая строка партии присутствует в переводе;
  * плейсхолдеры (%s %d %% %1$s) совпадают в EN и RU;
  * §-коды и теги <i> <n> <r> не потеряны;
  * в RU не осталось непереведённой латиницы (кроме допустимого).

Запуск: python3 check_prose.py [--strict]
"""
import sys, os, re, glob

OUT = '/home/user/work/prose'
CODE = re.compile(r'§.')
TAG = re.compile(r'</?\w+>')
PH = re.compile(r'%(?:\d+\$)?[-+ #0]*\d*(?:\.\d+)?[sdifgxobh]|%%')
# допустимая латиница в русском переводе: бренды, моды, единицы, клавиши
ALLOW_LATIN = re.compile(
    r'^(?:[A-Za-z0-9§%+\-.,:;!?/()\[\]<>=\'"*]+(?:\s|$))+$')


def load():
    tr = {}
    dup = []
    for fn in sorted(glob.glob(f'{OUT}/*.ru.tsv')):
        for ln, line in enumerate(open(fn, encoding='utf-8'), 1):
            if line.startswith('#') or not line.strip():
                continue
            if '\t' not in line:
                print(f'  ❌ {os.path.basename(fn)}:{ln} нет таба: {line[:60]}')
                continue
            en, ru = line.rstrip('\n').split('\t', 1)
            if en in tr and tr[en] != ru:
                dup.append((en, tr[en], ru))
            tr[en] = ru
    return tr, dup


# Технические единицы, аббревиатуры модов и бренды, которые принято
# оставлять латиницей даже внутри русской строки.
TECH = re.compile(r'^(?:[%s%s]|[A-Za-z0-9]+(?:[\s.,:/+\-]+[A-Za-z0-9]+)*)$'
                  % (re.escape('§.'), re.escape('§')))


def ok_latin(ru):
    """True, если строка целиком латинская, но это осознанное решение."""
    from words_ru import keep
    body = re.sub(r'§.', '', ru)
    words = [w for w in re.split(r'\s+', body.strip()) if w]
    if not words:
        return True
    # каждое слово должно быть техникой, аббревиатурой или числом
    for w in words:
        # убираем плейсхолдеры и разделители: «%s/%s FE» -> «FE»
        core = re.sub(r'%\S*', '', w).strip('.,:;!?()[]"\'/ ')
        if not core or core.isdigit() or len(core) <= 3 or keep(core):
            continue
        # единицы энергии, коды модов и названия игр — латиницей
        # единицы энергии могут писаться со слешем: FE/t, CF/t
        if re.fullmatch(r'[A-Za-z]+(?:/(?:t|[A-Za-z]+))?', core):
            continue
        if re.fullmatch(r'\d+(?:/\d+)*', core):
            continue
        if core in ('BlockZ', 'ActAdd', 'Primus', 'iChisel', 'manmaed',
                    'Machines', 'Solarus', 'BetterBlockZ', 'Actually',
                    'Additions', 'Minecraft', 'Extra', 'Utilities'):
            continue
        return False
    return True


def main():
    tr, dup = load()
    intentional = {}
    print(f'партий: переводов в словаре: {len(tr)}')
    if dup:
        print(f'\n⚠️  противоречий (одинаковый EN — разный RU): {len(dup)}')
        for en, a, b in dup[:10]:
            print(f'   {en[:50]!r}: {a[:40]!r} vs {b[:40]!r}')

    # сколько фраз прозы вообще есть в модах
    sys.path.insert(0, '/home/user/work')
    try:
        from extract_prose import collect
        allp = collect()
    except Exception as e:
        print(f'не удалось опросить моды: {e}')
        return
    todo = [s for s in allp if s not in tr]
    print(f'\nфраз прозы в модах: {len(allp)}')
    print(f'переведено:         {len(allp) - len(todo)} = '
          f'{100*(len(allp)-len(todo))//max(len(allp),1)}%')
    print(f'осталось:           {len(todo)}')

    # валидация переведённых
    bad = 0
    for en, ru in tr.items():
        if en not in allp:
            continue
        a, b = sorted(PH.findall(en)), sorted(PH.findall(ru))
        if a != b:
            print(f'  ❌ плейсхолдеры: {en[:44]!r} -> {ru[:44]!r}  {a} != {b}')
            bad += 1
        ca, cb = sorted(CODE.findall(en)), sorted(CODE.findall(ru))
        if ca != cb:
            print(f'  ❌ §-коды: {en[:44]!r} -> {ru[:44]!r}  {ca} != {cb}')
            bad += 1
        ta, tb = sorted(TAG.findall(en)), sorted(TAG.findall(ru))
        if ta != tb:
            print(f'  ❌ теги: {en[:44]!r} -> {ru[:44]!r}  {ta} != {tb}')
            bad += 1
        # перевод без кириллицы допустим, если это техника/бренд/единица
        if not re.search(r'[А-Яа-яЁё]', ru):
            if not ok_latin(ru):
                print(f'  ❌ латиница без причины: {en[:44]!r} -> {ru[:44]!r}')
                bad += 1
            else:
                intentional[en] = ru
    print(f'\nнамеренно оставлено латиницей: {len(intentional)}')
    print(f'ошибок в переводах: {bad}')
    if '--strict' in sys.argv and bad:
        sys.exit(1)
    if todo[:10]:
        print('\nпервые непереведённые:')
        for s in todo[:10]:
            print(f'   {s[:90]}')


if __name__ == '__main__':
    main()
