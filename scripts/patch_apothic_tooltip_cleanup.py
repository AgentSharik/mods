#!/usr/bin/env python3
"""Патчер зашитых в код строк подсказок Apothic Tooltip Cleanup.

Мод не имеет lang-ключей (en_us.json пуст), а часть видимых строк подсказок
Apotheosis задана литералами Component.literal(...) в классах, поэтому
перевод выполняется заменой Utf8-констант constant pool:

- FitsInRemover.fitsInHeaderBullet() собирает заголовок секции самоцвета
  литералом "Fits in:" вместо ключа text.apotheosis.socketable_into —
  именно эта строка оставалась английской на скриншоте из игры при русских
  категориях. Заменяется на «Подходит для:» (значение того же ключа в
  ru_ru Apotheosis). Строка-конкатенация "Fits in: %s" правится так же.
- TooltipMatcher сравнивает заголовок с тем же литералом; чтобы распознавание
  секции не ломалось в русском клиенте, литерал заменяется синхронно.
- AltExpandHandler добавляет подсказку "Hold Alt for full details".
- EmptySocketMerger и SocketCompactor собирают строку "◇ x<N> empty sockets".

Групповые имена категорий внутри FitsInRemover (Armor, Weapons, ...) — это
внутренние ключи группировки и значения конфига HIDDEN_GEM_CATEGORIES, они
игроку не показываются (на экране — переводимые названия категорий
Apotheosis) и не трогаются. Фрагменты " levels to existing " в stripExisting
срезают английский служебный текст бонусов Apotheosis до перевода и тоже не
являются видимыми строками этого мода.

Использование: patch_apothic_tooltip_cleanup.py <класс> [<класс> ...]
(файлы перезаписываются на месте)
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from patch_class_string import patch

REPL = {
    'Fits in:': 'Подходит для:',
    'Fits in: \x01': 'Подходит для: \x01',
    'Hold Alt for full details': 'Удерживайте Alt, чтобы увидеть подробности',
    '◇ x\x01 empty sockets': '◇ x\x01 пустых гнёзд',
}

if __name__ == '__main__':
    for path in sys.argv[1:]:
        print(path)
        data = open(path, 'rb').read()
        total = 0
        for old, new in REPL.items():
            try:
                data = patch(data, old, new)
                total += 1
            except AssertionError:
                pass  # строка есть не в каждом из классов
        print(f'  replaced {total} strings')
        open(path, 'wb').write(data)
