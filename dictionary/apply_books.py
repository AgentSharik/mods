# -*- coding: utf-8 -*-
"""Применение book_ru.BOOK к JAR со страницами Patchouli."""
import glob, json, os, re, sys, zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import book_extract as B
import book_ru

CY = re.compile(r'[А-Яа-яЁё]')


def build_map_for(jar_path, table):
    """table: {(modid, относительный_путь, поле): текст} → {(путь, поле): текст} для этого JAR."""
    res = B.collect([jar_path])
    got = res.get(jar_path, [])
    out = {}
    for fname, field, txt in got:
        parts = fname.split('patchouli_books/')
        if len(parts) != 2:
            continue
        rest = parts[1]
        seg = rest.split('/')
        # 1) отбрасываем сегмент локали (en_us / zh_cn …)
        if len(seg) > 1 and re.match(r'^[a-z]{2}_[a-z]{2}$', seg[1]):
            seg.pop(1)
        # 2) отбрасываем имя книги (book_of_trees, laseriobook, manual, worn_notebook …)
        if len(seg) > 2:
            seg.pop(0)
        rest = '/'.join(seg)
        # ищем мод по префиксу имени JAR
        for mod in ('ars_ocultas', 'bonsaitrees4', 'buildinggadgets2', 'laserio',
                    'rftoolsbuilder', 'starbunclemania'):
            if mod in os.path.basename(jar_path):
                if (mod, rest, field) in table:
                    out[(fname, field)] = table[(mod, rest, field)]
                break
    return out, len(got)


if __name__ == '__main__':
    table = book_ru.BOOK
    total_changed = total_left = 0
    for p in sorted(glob.glob('out_party1/*.jar') + glob.glob('out_party3/*.jar')):
        m, n_got = build_map_for(p, table)
        if not m:
            continue
        ch = B.apply_jar(p, m)
        total_changed += ch
        print(f'{os.path.basename(p):46s} строк в книге {n_got:4d} → переведено {ch}')
    print('всего изменено полей:', total_changed)
    # повторная проверка
    print('\n── что осталось на английском ──')
    for p in sorted(glob.glob('out_party1/*.jar') + glob.glob('out_party3/*.jar')):
        res = B.collect([p]).get(p, [])
        # lang-ссылки вида mod.key — это уже переведённые ключи, не считаем
        left = [t for _, _, t in res if not re.fullmatch(r'[\w.]+', t)]
        if left:
            print(f'  {os.path.basename(p):46s} {len(left)}')
            for t in left[:4]:
                print('     ', t[:100])
