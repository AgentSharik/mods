# -*- coding: utf-8 -*-
"""
Защитный языковой ресурспак StoneBlock4-RU-Protect.zip.

Зачем: перевод внутри JAR мода умирает при любом обновлении мода/сборки
(лаунчер заменяет JAR оригиналом автора, где русский частичный или отсутствует).
Этот скрипт собирает ВСЕ assets/*/lang/ru_ru.json из готовых JAR проекта
в один ресурспак. Ресурспак в игре стоит ВЫШЕ ресурсов модов и слиянием
по ключам перекрывает их lang-файлы: уже переведённые строки не могут
откатиться на английский, даже если JAR мода заменён оригиналом.

Запуск: python3 scripts/build_langpack.py
Результат: Проекты/StoneBlock 4/документация/release/StoneBlock4-RU-Protect.zip
Пересобирать при каждой волне (после добавления/обновления JAR).
"""
import zipfile, json, os, sys, glob

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JARS = sorted(glob.glob(os.path.join(ROOT, 'Проекты', 'StoneBlock 4', '*.jar')))
OUT = os.path.join(ROOT, 'Проекты', 'StoneBlock 4', 'документация', 'release', 'StoneBlock4-RU-Protect.zip')

merged = {}   # путь -> {ключ: значение}
origin = {}   # путь -> jar
conflicts = []
for jar in JARS:
    try:
        z = zipfile.ZipFile(jar)
    except Exception:
        continue
    for n in z.namelist():
        if n.startswith('assets/') and n.endswith('/lang/ru_ru.json'):
            try:
                data = json.loads(z.read(n))
            except Exception:
                print(f'ВНИМАНИЕ: битый ru_ru.json в {os.path.basename(jar)}: {n}')
                continue
            if n not in merged:
                merged[n] = {}
                origin[n] = os.path.basename(jar)
            for k, v in data.items():
                if k in merged[n] and merged[n][k] != v:
                    conflicts.append((n, k, origin[n], os.path.basename(jar)))
                merged[n][k] = v

os.makedirs(os.path.dirname(OUT), exist_ok=True)
mcmeta = {
    'pack': {
        'pack_format': 34,
        'description': 'StoneBlock 4 RU Protect: резервный русский для всех модов пакета. Включать ПОВЕРХ остальных ресурспаков.',
    }
}
with zipfile.ZipFile(OUT, 'w', zipfile.ZIP_DEFLATED) as z:
    z.writestr('pack.mcmeta', json.dumps(mcmeta, ensure_ascii=False, indent=2))
    total = 0
    for path in sorted(merged):
        z.writestr(path, json.dumps(merged[path], ensure_ascii=False, indent=1))
        total += len(merged[path])
print(f'Готово: {OUT}')
print(f'  JAR обработано: {len(JARS)}, lang-файлов: {len(merged)}, ключей: {total}')
if conflicts:
    print(f'  конфликтов ключей между JAR: {len(conflicts)} (победил последний по алфавиту JAR)')
    for c in conflicts[:5]:
        print('   ', c)
