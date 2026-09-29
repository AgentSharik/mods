# -*- coding: utf-8 -*-
"""Сборка переведённых модов.

Для каждого JAR из /home/user/dl/pack/mods:
  * lang/en_us.json  ->  перевод движком + словарём прозы
  * кладёт ru_ru.json (основной) и дублирует в en_us.json
  * прочие языки удаляются
  * всё остальное содержимое JAR сохраняется байт в байт
  * resource-пути и ID не меняются

Запуск:
  python3 build_mp.py                 # собрать все моды в папке
  python3 build_mp.py chisel rechiseled   # только указанные
  python3 build_mp.py --report        # только отчёт без сборки
"""
import sys, os, re, json, glob, zipfile, shutil, collections

sys.path.insert(0, '/home/user/work')
import mp_tr as M
from mpclassify import is_name, strip_service
from extract_prose import collect as _collect_prose

SRC = '/home/user/dl/pack/mods'
OUT = '/home/user/work/out'
CODE = re.compile(r'§.')
TAG = re.compile(r'</?\w+>')
PH = re.compile(r'%(?:\d+\$)?[-+ #0]*\d*(?:\.\d+)?[sdifgxobh]|%%')

_prose = None


def prose_dict():
    """Словарь прозы из prose/*.ru.tsv (глобальный кэш)."""
    global _prose
    if _prose is not None:
        return _prose
    d = {}
    for fn in sorted(glob.glob('/home/user/work/prose/*.ru.tsv')):
        for line in open(fn, encoding='utf-8'):
            if line.startswith('#') or not line.strip() or '\t' not in line:
                continue
            en, ru = line.rstrip('\n').split('\t', 1)
            d[en] = ru
    _prose = d
    return d


def looks_translatable(v):
    """Строка содержит слова, которые имеет смысл переводить."""
    if not v or not v.strip():
        return False
    if re.match(r'^[A-Z0-9#§%\-\.\+_ ]+$', v):
        return False          # код цвета/формата
    return True


def translate_value(v, stats):
    """Переводит одно значение lang-файла."""
    if not looks_translatable(v):
        return v, False
    p = prose_dict()
    if v in p:
        return p[v], True
    if is_name(v):
        ru, ok = M.translate_name(v)
        return ru, ok
    # не имя и не в словаре прозы — пробуем движок, но помечаем как сомнительное
    ru, ok = M.translate_name(v)
    if re.search(r'[А-Яа-яЁё]', ru):
        return ru, ok
    return v, False


def build(path, report_only=False):
    name = os.path.basename(path)
    z = zipfile.ZipFile(path)
    names = z.namelist()
    langs = [n for n in names if re.match(r'assets/[^/]+/lang/.+\.json$', n)]

    src = None
    for n in langs:
        if n.endswith('/en_us.json'):
            src = n
            break
    if src is None:
        print(f'  ⏭  {name[:46]:<46} нет en_us.json')
        return None

    modid = src.split('/')[1]
    try:
        en = json.loads(z.read(src).decode('utf-8'))
    except Exception as _e:
        # часть модов кладёт битый JSON — такой мод просто пропускаем
        print(f'  ⚠️  {name[:46]:<46} битый en_us.json — пропускаю')
        return None
    if not isinstance(en, dict):
        print(f'  ⚠️  {name[:46]:<46} en_us.json не объект — пропускаю')
        return None

    stats = collections.Counter()
    out = {}
    for k, v in en.items():
        if not isinstance(v, str):
            out[k] = v
            stats['nonstr'] += 1
            continue
        ru, ok = translate_value(v, stats)
        out[k] = ru
        stats['total'] += 1
        if ru != v:
            stats['changed'] += 1
        if ok:
            stats['good'] += 1
        elif is_name(v):
            stats['name_hard'] += 1
        else:
            stats['prose_left'] += 1

    pct = 100 * stats['good'] // max(stats['total'], 1)
    print(f'  {"📊" if report_only else "🔧"} {name[:46]:<46} '
          f'{stats["good"]:>5}/{stats["total"]:<5} = {pct:>3}%  '
          f'(имен с пробелами: {stats["name_hard"]}, проза: {stats["prose_left"]})')
    if report_only:
        return stats

    os.makedirs(OUT, exist_ok=True)
    dst = os.path.join(OUT, name)
    tmp = dst + '.tmp'
    with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as o:
        dropped = set()
        for item in z.infolist():
            n = item.filename
            # прочие языки удаляем
            if re.match(r'assets/[^/]+/lang/.+\.json$', n):
                if n.endswith('/en_us.json'):
                    o.writestr(item, json.dumps(out, ensure_ascii=False,
                                                indent=0, sort_keys=True)
                               .encode('utf-8'))
                elif n.endswith('/ru_ru.json'):
                    o.writestr(item, json.dumps(out, ensure_ascii=False,
                                                indent=0, sort_keys=True)
                               .encode('utf-8'))
                else:
                    dropped.add(n)
                continue
            o.writestr(item, z.read(n))
        # если ru_ru.json не было — создаём
        if f'assets/{modid}/lang/ru_ru.json' not in {i.filename for i in z.infolist()}:
            o.writestr(f'assets/{modid}/lang/ru_ru.json',
                       json.dumps(out, ensure_ascii=False, indent=0,
                                  sort_keys=True).encode('utf-8'))
    z.close()
    shutil.move(tmp, dst)
    print(f'      -> {dst}  ({os.path.getsize(dst)//1024} КБ, '
          f'языков было {len(langs)}, удалено {len(dropped)-1})')
    return stats


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    report_only = '--report' in sys.argv
    paths = sorted(glob.glob(os.path.join(SRC, '*.jar')))
    if args:
        paths = [p for p in paths if any(a in p for a in args)]
    if not paths:
        print('моды не найдены — выполните: python3 getpack.py get <подстрока>')
        return
    print(f'сборка {len(paths)} мод(ей)\n')
    total = collections.Counter()
    for p in paths:
        s = build(p, report_only)
        if s:
            total.update(s)
    t = max(total['total'], 1)
    print(f'\nИТОГО: {total["good"]}/{t} = {100*total["good"]//t}%  |  '
          f'изменено строк: {total["changed"]}  |  '
          f'имён с пробелами: {total["name_hard"]}  |  '
          f'непереведённой прозы: {total["prose_left"]}')


if __name__ == '__main__':
    main()
