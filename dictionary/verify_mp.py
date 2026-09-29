# -*- coding: utf-8 -*-
"""Проверка собранных модов перед публикацией.

Проверяет:
  * JAR открывается, структура не сломана;
  * есть ru_ru.json и en_us.json с одинаковым содержимым;
  * прочих языков не осталось;
  * кириллица корректна, нет символа замены U+FFFD;
  * сколько значений осталось без кириллицы (кроме техники и кодов).

Запуск: python3 verify_mp.py
"""
import sys, os, re, json, glob, zipfile

sys.path.insert(0, '/home/user/work')
from words_ru import keep

OUT = '/home/user/work/out'
FFFD = '�'
LATIN = re.compile(r'[A-Za-z]{3,}')
CODE = re.compile(r'§.')


def main():
    paths = sorted(glob.glob(os.path.join(OUT, '*.jar')))
    if not paths:
        print('нет собранных модов в', OUT)
        return 1
    problems = 0
    tot = cyr = no_cyr = 0
    for p in paths:
        name = os.path.basename(p)
        try:
            z = zipfile.ZipFile(p)
            bad = z.testzip()
            if bad:
                print(f'  ❌ {name}: повреждён элемент {bad}')
                problems += 1
                continue
        except Exception as e:
            print(f'  ❌ {name}: не открывается — {e}')
            problems += 1
            continue

        langs = [n for n in z.namelist()
                 if re.match(r'assets/[^/]+/lang/.+\.json$', n)]
        modid = langs[0].split('/')[1] if langs else None
        ru = en = None
        others = []
        for n in langs:
            if n.endswith('/ru_ru.json'):
                ru = json.loads(z.read(n).decode('utf-8'))
            elif n.endswith('/en_us.json'):
                en = json.loads(z.read(n).decode('utf-8'))
            else:
                others.append(n)

        issues = []
        if ru is None:
            issues.append('нет ru_ru.json')
        if en is None:
            issues.append('нет en_us.json')
        if others:
            issues.append(f'остались языки: {[o.split("/")[-1] for o in others]}')
        if ru is not None and en is not None and ru != en:
            issues.append('ru_ru и en_us различаются')
        if issues:
            print(f'  ❌ {name[:46]:<46} {issues}')
            problems += 1
            continue

        n_ru = len(ru)
        c = sum(1 for v in ru.values() if isinstance(v, str) and re.search(r'[А-Яа-яЁё]', v))
        f = sum(1 for v in ru.values() if isinstance(v, str) and FFFD in v)
        if f:
            print(f'  ❌ {name[:46]:<46} символ замены U+FFFD в {f} значениях')
            problems += 1
        tot += n_ru
        cyr += c
        no_cyr += n_ru - c
        print(f'  ✅ {name[:46]:<46} {n_ru:>5} ключей, с кириллицей {c:>5} '
              f'({100*c//max(n_ru,1):>3}%), без кириллицы {n_ru-c:>4}')
        z.close()
    print(f'\nИТОГО: {tot} ключей, с кириллицей {cyr} = {100*cyr//max(tot,1)}%, '
          f'без кириллицы {no_cyr}')
    print(f'проблем со структурой: {problems}')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
