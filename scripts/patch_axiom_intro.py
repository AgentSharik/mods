#!/usr/bin/env python3
"""Патчер вводного мастера Axiom (AxiomIntroductionScreen).

Все строки мастера первого запуска зашиты литералами в классе и отсутствуют
в lang-файле, поэтому ru_ru.json их не покрывает. Скрипт заменяет Utf8-константы
константного пула на русские строки (структура класса не меняется).

Использование: python3 patch_axiom_intro.py <исходный класс> <выходной класс>
"""
import struct
import sys

REPL = {
    'Welcome to Axiom': 'Добро пожаловать в Axiom',
    'Builder Mode': 'Режим строительства',
    'Editor Mode': 'Режим редактора',
    'It seems like this is your first time...\n\n': 'Похоже, вы пользуетесь Axiom впервые...\n\n',
    'To access the ': 'Чтобы открыть ',
    ' menu, hold ': ', удерживайте ',
    'To open/close the ': 'Чтобы открыть или закрыть ',
    ', press ': ', нажмите ',
    'To view this introduction again, run ': 'Чтобы снова открыть это введение, выполните ',
    "To view Axiom's web documentation, run ": 'Веб-документация Axiom: команда ',
    "To open Axiom's config documentation, run ": 'Документация по настройкам Axiom: команда ',
    'Would you like to run through a quick introduction?': 'Пройти быстрое обучение?',
    'Builder Tools': 'Строительные инструменты',
    'These tools are simple and are intended to ease structural/small scale building where use of the more powerful ':
        'Эти инструменты просты и предназначены для лёгкого строительства конструкций и небольших построек, где более мощный ',
    ' is too slow\n\n': ' слишком медленный\n\n',
    'To start using the tools, scroll to the 10th slot or press ': 'Чтобы начать работу с инструментами, прокрутите до 10-го слота или нажмите ',
    'To swap to a different tool, hold ': 'Чтобы сменить инструмент, удерживайте ',
    ' and scroll': ' и прокручивайте колесо мыши',
    'Would you like to try Enhanced Flight?\n': 'Хотите попробовать улучшенный полёт?\n',
    'Enhanced Flight changes flight direction to be 3D and removes momentum\n': 'Улучшенный полёт делает направление полёта объёмным и убирает инерцию\n',
    'You can always adjust the flight settings in the ': 'Изменить настройки полёта позже можно в панели инструментов — ',
    ' toolbox later': '',
    'Vanilla': 'Обычный',
    'Enhanced': 'Улучшенный',
    ' default keybinds\n\n': ': стандартные сочетания клавиш\n\n',
    'Would you like to use the default Axiom keybinds or Blender-like keybinds?\n': 'Какие сочетания клавиш использовать: стандартные Axiom или в стиле Blender?\n',
    'You can always adjust the keybinds later in the ': 'Сочетания клавиш всегда можно изменить позже (',
    ' through Help > Keybinds': '): меню «Справка» → «Клавиши»',
    'Axiom Default': 'Стандартные Axiom',
    'Blender-like': 'В стиле Blender',
    "That's it for this short introduction!": 'Короткое введение на этом окончено!',
    'I hope you enjoy using Axiom\n\n': 'Надеюсь, вам понравится работать в Axiom\n\n',
}


def patch(data: bytes) -> bytes:
    cp_count = struct.unpack('>H', data[8:10])[0]
    out = bytearray(data[:10])
    pos = 10
    i = 1
    replaced = 0
    while i < cp_count:
        tag = data[pos]
        if tag == 1:
            ln = struct.unpack('>H', data[pos + 1:pos + 3])[0]
            raw = data[pos + 3:pos + 3 + ln]
            try:
                txt = raw.decode('utf-8')
            except UnicodeDecodeError:
                txt = None
            if txt in REPL:
                nb = REPL[txt].encode('utf-8')
                out += b'\x01' + struct.pack('>H', len(nb)) + nb
                replaced += 1
            else:
                out += data[pos:pos + 3 + ln]
            pos += 3 + ln
        elif tag in (7, 8, 16, 19, 20):
            out += data[pos:pos + 3]; pos += 3
        elif tag == 15:
            out += data[pos:pos + 4]; pos += 4
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            out += data[pos:pos + 5]; pos += 5
        elif tag in (5, 6):
            out += data[pos:pos + 9]; pos += 9; i += 1
        else:
            raise ValueError(f'unknown tag {tag}')
        i += 1
    out += data[pos:]
    print(f'replaced {replaced} strings')
    return bytes(out)


if __name__ == '__main__':
    src, dst = sys.argv[1], sys.argv[2]
    with open(src, 'rb') as f:
        patched = patch(f.read())
    with open(dst, 'wb') as f:
        f.write(patched)
