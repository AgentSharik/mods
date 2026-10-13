#!/usr/bin/env python3
"""Сборка переведённого мода: замена ru_ru.json на месте внутри копии исходного JAR.

    python3 scripts/build_lang.py <src.jar> <out.jar> <assets/<modid>/lang/> <ru.json>

Правила сборки (зафиксированы 13.10.2026):
  * заменяется ТОЛЬКО ru_ru.json;
  * en_us.json и все прочие языки остаются нетронутыми;
  * оригинал не изменяется.
"""
import json, os, re, shutil, subprocess, sys, tempfile, zipfile

PH = re.compile(r'%[0-9]?\$?[sdf]|%[0-9]?\$?\$\w?|\{[0-9]\}|\$\{[a-zA-Z_.]+\}')


def report(ru, en, label):
    """Отчёт по переводу: покрытие, плейсхолдеры, §-коды, переносы, пробелы."""
    miss = [k for k in en if k not in ru]
    extra = [k for k in ru if k not in en]
    same = [k for k in en if ru.get(k) == en[k]]
    ph = [k for k in ru if sorted(PH.findall(en.get(k, ''))) != sorted(PH.findall(ru[k]))]
    sec = [k for k, v in ru.items() if '§' in v]
    nl = [k for k in ru if ru[k].count('\n') != en.get(k, '').count('\n')]
    ws = [k for k in ru if ru[k] != ru[k].strip() and en.get(k, '') == en.get(k, '').strip()]
    print(f'  [{label}] ключей en: {len(en)}, ru: {len(ru)}')
    print(f'  [{label}] пропущено: {len(miss)}, лишних: {len(extra)}, '
          f'равно английскому: {len(same)}')
    print(f'  [{label}] расхождение плейсхолдеров: {len(ph)} {ph[:5]}')
    print(f'  [{label}] строк с §: {len(sec)}, переносов расходится: {len(nl)} {nl[:5]}')
    print(f'  [{label}] лишние краевые пробелы: {len(ws)} {ws[:5]}')
    return not (miss or extra or ph)


def build(src, out, lang_dir, ru_path):
    ru = json.load(open(ru_path, encoding='utf-8'))
    tmp = tempfile.mkdtemp(prefix='build_lang_')
    work = os.path.join(tmp, 'work')
    os.makedirs(work)
    with zipfile.ZipFile(src) as z:
        z.extractall(work)

    target = os.path.join(work, lang_dir.strip('/'), 'ru_ru.json')
    if not os.path.exists(os.path.join(work, lang_dir.strip('/'), 'en_us.json')):
        sys.exit(f'нет en_us.json по пути {lang_dir} внутри {src}')
    en = json.load(open(os.path.join(work, lang_dir.strip('/'), 'en_us.json'), encoding='utf-8'))
    print(f'сборка: {os.path.basename(out)}')
    report(ru, en, 'перевод')

    json.dump(ru, open(target, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)

    if os.path.exists(out):
        os.remove(out)
    subprocess.run(['zip', '-q', '-r', '-X', out, '.'], cwd=work, check=True)

    with zipfile.ZipFile(out) as z:
        assert z.testzip() is None, 'архив повреждён'
        names = z.namelist()
        for n in names:
            if n.endswith('.json'):
                json.loads(z.read(n))
        inside = json.loads(z.read(os.path.join(lang_dir.strip('/'), 'ru_ru.json')))
    print(f'  в архиве: записей {len(names)}, ключей ru_ru {len(inside)}, testzip ok')

    print('  размер: %.2f МБ' % (os.path.getsize(out) / 1048576))
    print('  sha256:', subprocess.run(['sha256sum', out], capture_output=True,
                                      text=True).stdout.split()[0])
    shutil.rmtree(tmp, ignore_errors=True)
    print('готово:', out)


if __name__ == '__main__':
    if len(sys.argv) != 5:
        sys.exit(__doc__)
    build(*sys.argv[1:5])
