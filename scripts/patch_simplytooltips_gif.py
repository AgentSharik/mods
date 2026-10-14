#!/usr/bin/env python3
"""Патчер зашитых в код сообщений записи GIF в SimplyTooltips.

Сообщения чата при записи подсказки в GIF заданы через Component.literal(...)
в TooltipGifRecorder и отсутствуют в lang-файле, поэтому переводятся заменой
Utf8-констант классов. Префикс [Simply Tooltips] — имя мода, не переводится.

Использование: patch_simplytooltips_gif.py <класс> [<класс> ...]
(файлы перезаписываются на месте)
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from patch_class_string import patch

REPL = {
    '[Simply Tooltips] GIF capture is already in progress.':
        '[Simply Tooltips] Запись GIF уже идёт.',
    '[Simply Tooltips] No tooltip is currently visible to capture.':
        '[Simply Tooltips] Сейчас нет видимой подсказки для записи.',
    '[Simply Tooltips] Recording tooltip GIF (6.0s @ 20 FPS)...':
        '[Simply Tooltips] Записывается GIF подсказки (6 секунд, 20 кадров/с)...',
    '[Simply Tooltips] Capture cancelled: tooltip is no longer visible.':
        '[Simply Tooltips] Запись отменена: подсказка больше не видна.',
    '[Simply Tooltips] Failed to capture tooltip frame.':
        '[Simply Tooltips] Не удалось снять кадр подсказки.',
    '[Simply Tooltips] GIF capture finished, but no frames were recorded.':
        '[Simply Tooltips] Запись GIF завершена, но кадры не записаны.',
    '[Simply Tooltips] Saved looping tooltip GIF. Folder: ':
        '[Simply Tooltips] Зацикленный GIF подсказки сохранён. Папка: ',
    '[Simply Tooltips] Failed to save tooltip GIF: \x01':
        '[Simply Tooltips] Не удалось сохранить GIF подсказки: \x01',
    'No frames available for GIF output':
        'Нет кадров для записи GIF',
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
