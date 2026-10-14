#!/usr/bin/env python3
"""Патчер зашитых в код строк Axiom.

Часть пользовательского текста Axiom отсутствует в lang-файле: мастер первого
запуска (AxiomIntroductionScreen), советы при входе и серверные уведомления
(AxiomClient, ClientEvents, AxiomServer). Скрипт заменяет Utf8-константы
константного пула классов на русские строки; структура классов не меняется.
Логи и технические исключения не трогаются.

Использование: python3 patch_axiom_hardcoded.py <класс> [<класс> ...]
(файлы перезаписываются на месте)
"""
import struct
import sys

REPL = {
    # --- AxiomIntroductionScreen: мастер первого запуска ---
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
    # --- AxiomClient: советы при входе ---
    'The Editor UI keybind has conflicts: ': 'У сочетания клавиш UI редактора есть конфликты: ',
    'Tip: Access the Axiom config by running /axiomconfig': 'Совет: открыть настройки Axiom можно командой /axiomconfig',
    'Tip: Access the Axiom context menu by holding [': 'Совет: контекстное меню Axiom открывается удержанием [',
    'Tip: Access the Axiom Editor UI by pressing [': 'Совет: UI редактора Axiom открывается нажатием [',
    # --- ClientEvents: уведомления игроку ---
    'Please update the server to Axiom 6.x+': 'Обновите сервер до Axiom 6.x+',
    "[Axiom] This server has Axiom, but your client doesn't support multiplayer.": '[Axiom] На этом сервере есть Axiom, но ваш клиент не поддерживает мультиплеер.',
    '[Axiom] If you are building on this server for commercial purposes, please support the mod by purchasing a ': '[Axiom] Если вы строите на этом сервере в коммерческих целях, поддержите мод, приобретя ',
    'Commercial License': 'коммерческую лицензию',
    '[Axiom] Otherwise, you can request a whitelist through the ': '[Axiom] В противном случае вы можете запросить доступ через ',
    'Discord Server': 'сервер Discord',
    'Enabled showing all invisible blocks': 'Показ всех невидимых блоков включён',
    'Disabling showing all invisible blocks': 'Показ всех невидимых блоков отключён',
    'The server has disallowed the use of the editor': 'Сервер запретил использование редактора',
    'Axiom is temporarily unavailable:\n\n\x01': 'Axiom временно недоступен:\n\n\x01',
    'New Axiom version available: ': 'Доступна новая версия Axiom: ',
    'Please report this!': 'Сообщите об этом!',
    'Axiom is temporarily unavailable': 'Axiom временно недоступен',
    "Server doesn't support Axiom. Axiom needs to be installed on both the client and server. Make sure it is installed on the server": 'Сервер не поддерживает Axiom. Axiom должен быть установлен и на клиенте, и на сервере. Убедитесь, что он установлен на сервере',
    "Server hasn't sent hello. Make sure that the Axiom jar on the server is up-to-date!": 'Сервер не прислал приветствие. Убедитесь, что JAR Axiom на сервере актуальной версии!',
    'Cheats are disabled on this world. Try /axiomenablecheats': 'Читы отключены в этом мире. Попробуйте /axiomenablecheats',
    "The server hasn't given you permission to use Axiom. Do you have OP?": 'Сервер не дал вам разрешение на использование Axiom. У вас есть OP?',
    'This command is only available in Singleplayer': 'Эта команда доступна только в одиночной игре',
    'Cheats are already enabled': 'Читы уже включены',
    'Cheats are now enabled. You may need to relog for Axiom to activate': 'Читы включены. Возможно, потребуется перезайти, чтобы Axiom активировался',
    'Set second position to': 'Вторая позиция установлена на',
    'Set first position to': 'Первая позиция установлена на',
    "You seem to be using Wurst. Make sure you don't have VanillaSpoof enabled": 'Похоже, вы используете Wurst. Убедитесь, что у вас не включён VanillaSpoof',
    'Axiom should be enabled': 'Axiom должен быть включён',
    # --- AxiomServer: причины отключения, видимые игроку ---
    'Axiom: Sent split buffer that was too large': 'Axiom: отправлен слишком большой разделённый буфер',
    'Axiom: Sent packet that was unregistered': 'Axiom: отправлен незарегистрированный пакет',
    'Axiom: Sent packet was too large': 'Axiom: отправлен слишком большой пакет',
    'You are sending updates too fast!': 'Вы отправляете обновления слишком быстро!',
    'Cheats are not enabled on this world. Run /axiomenablecheats to enable them': 'Читы не включены в этом мире. Выполните /axiomenablecheats, чтобы включить их',
    "You don't have permission to use Axiom. Give yourself /op or axiom.default (if using a permission plugin)": 'У вас нет разрешения на использование Axiom. Выдайте себе /op или axiom.default (если используете плагин разрешений)',
    # --- SwitchHotbarScreen: экран панелей со скриншота из игры ---
    'Switch Toolbar': 'Смена панели инструментов',
    'Axiom Update Available!': 'Доступно обновление Axiom!',
    'This capability has been disabled by the server': 'Эта возможность отключена сервером',
    'Page \x01': 'Страница \x01',
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
    print(f'  replaced {replaced} strings')
    return bytes(out)


# Отдельный guard, применяется только к LocalizationLoader: Axiom скачивает
# локализации Weblate и накладывает их поверх ru_ru.json из JAR, из-за чего
# уже переведённые строки в игре снова видны по-английски. Проверка языка там
# записана как startsWith("en"); замена константы "en" -> "ru" только в этом
# классе отключает скачивание и кэш Weblate именно для русского языка.
LOCALIZATION_GUARD = {'en': 'ru'}


def patch_localization_loader(data: bytes) -> bytes:
    global REPL
    saved = REPL
    try:
        REPL = LOCALIZATION_GUARD
        return patch(data)
    finally:
        REPL = saved


if __name__ == '__main__':
    for path in sys.argv[1:]:
        print(path)
        with open(path, 'rb') as f:
            data = f.read()
        patched = (patch_localization_loader(data)
                   if 'LocalizationLoader' in path else patch(data))
        with open(path, 'wb') as f:
            f.write(patched)
