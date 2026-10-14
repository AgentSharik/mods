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


# --- Подписи редактора ImGui (окна инструментов и меню) ---
# Сканер байт-кода отобрал строки, реально передаваемые в вызовы ImGui;
# здесь только отображаемые подписи. Не включены: служебные ID виджетов
# и окон (##/### и CamelCase-идентификаторы), демо-строки Style Editor,
# примеры выражений масок и Lua-кода, имена шрифтов, расширения файлов,
# секции ini-раскладки и URL. У подписей с ##/### переведена только
# видимая часть, ID после ## сохранён байт-в-байт.
REPL_IMGUI = {
    ' (disabled)': ' (отключено)',
    '*** Only includes entities within render distance ***': '*** Учитываются только сущности в пределах дальности прорисовки ***',
    'Absolute': 'Абсолютно',
    'Actions': 'Действия',
    'Add Clipboard': 'Добавить буфер обмена',
    'Add Position': 'Добавить позицию',
    'Add Row': 'Добавить строку',
    'Add Translation': 'Добавить смещение',
    "Adds fluid around the edge of blocks to make it look like it's flowing over the edge": 'Добавляет жидкость по краям блоков, чтобы казалось, что она перетекает через край',
    'Adjusts even diameters (eg. 10) to be centered at the lower corner': 'Центрирует чётные диаметры (например, 10) по нижнему углу',
    'Air Block Delay (Seconds)': 'Задержка для блоков воздуха (секунды)',
    'All inputs must be true for the output to be true': 'Выход истинен, только если истинны все входы',
    'Allows for rotation by angles less than 90 degrees': 'Разрешает поворот на углы меньше 90 градусов',
    'Allows using decimal numbers (eg. 10.5) in various size sliders': 'Разрешает десятичные числа (например, 10.5) в ползунках размеров',
    'An error occurred while executing the script': 'При выполнении скрипта произошла ошибка',
    'Angle': 'Угол',
    'Animated Rebuild': 'Анимированная перестройка',
    'Animated Rebuild...': 'Анимированная перестройка...',
    'Apply Gravity': 'Применить гравитацию',
    'Apply Tool Mask': 'Применять маску инструмента',
    'Applying this operation to an extremely large area is likely to crash the server.': 'Применение этой операции к чрезвычайно большой области, скорее всего, обрушит сервер.',
    "Are you sure? If you clear all annotations in this world, it won't be possible to recover them!": 'Вы уверены? Если удалить все аннотации в этом мире, восстановить их будет невозможно!',
    'Arguments': 'Аргументы',
    'Automatic history persistence is a Commercial License feature. Click for more information': 'Автоматическое сохранение истории — функция коммерческой лицензии. Нажмите для подробностей',
    'Axis': 'Ось',
    'Block': 'Блок',
    'Block Version': 'Версия блоков',
    'BlockState': 'Состояние блока',
    'Blueprint Files': 'Файлы чертежей',
    'Both': 'Оба',
    'Box': 'Рамка',
    'Box Select': 'Выделение области',
    'Browse Server Heightmaps': 'Открыть серверные карты высот',
    'Browser': 'Браузер',
    'Brush Studio': 'Студия кистей',
    'Cancel': 'Отмена',
    'Cancel Export': 'Отменить экспорт',
    'Capsule': 'Капсула',
    'Category': 'Категория',
    'Center': 'Центр',
    'Center Even Diameters': 'Центрировать чётные диаметры',
    'Checks if any block horizontally touching the target position matches': 'Проверяет, совпадает ли любой блок, касающийся целевой позиции по горизонтали',
    'Checks if any block in a radius from the target position matches': 'Проверяет, совпадает ли любой блок в радиусе от целевой позиции',
    'Checks if any block touching the target position matches': 'Проверяет, совпадает ли любой блок, касающийся целевой позиции',
    'Checks if the target position has direct access to the sky': 'Проверяет, есть ли у целевой позиции прямой доступ к небу',
    'Checks if the target position is inside the selection': 'Проверяет, находится ли целевая позиция внутри выделения',
    'Checks if the target position is on the surface': 'Проверяет, находится ли целевая позиция на поверхности',
    'Checks the active tool mask when pasting into the world': 'Проверять активную маску инструмента при вставке в мир',
    'Circle': 'Круг',
    'Clear (Delete)': 'Очистить (удалить)',
    'Clear ALL Annotations': 'Удалить ВСЕ аннотации',
    'Clear Leak Path': 'Очистить путь утечки',
    'Click to show': 'Нажмите, чтобы показать',
    'Clipboard is empty': 'Буфер обмена пуст',
    'Close': 'Закрыть',
    'Color': 'Цвет',
    'Comma Separated Values': 'Значения через запятую',
    'Compares a constant value to the target y position': 'Сравнивает заданное значение с координатой Y целевой позиции',
    'Compares a constant value to the vertical angle of the surface': 'Сравнивает заданное значение с вертикальным углом поверхности',
    'Convex Hull': 'Выпуклая оболочка',
    'Copy Air': 'Копировать воздух',
    'Copy Current Layout to Clipboard': 'Скопировать текущую раскладку в буфер обмена',
    'Copy Entities': 'Копировать сущности',
    'Corners': 'Углы',
    'Count': 'Кол-во',
    'Cube': 'Куб',
    'Custom': 'Пользовательская',
    'Custom (Clipboard)': 'Пользовательская (буфер обмена)',
    'Custom Angle': 'Свой угол',
    'Custom Positions': 'Свои позиции',
    'Cut: Copy to clipboard': 'Вырезать: скопировать в буфер обмена',
    'Cylinder': 'Цилиндр',
    'Decimal Sizes': 'Десятичные размеры',
    'Default': 'По умолчанию',
    'Degrees': 'Градусы',
    'Degrees X': 'Градусы X',
    'Degrees Y': 'Градусы Y',
    'Degrees Z': 'Градусы Z',
    'Destination': 'Назначение',
    'Detect Leaks': 'Искать утечки',
    'Different Block Delay (Seconds)': 'Задержка для других блоков (секунды)',
    'Distance###Distance': 'Расстояние###Distance',
    'Do Animated Rebuild': 'Выполнить анимированную перестройку',
    'Do this for all conflicts': 'Применить ко всем конфликтам',
    'Do this for all unknown blocks': 'Применить ко всем неизвестным блокам',
    "Don't show this tip again": 'Больше не показывать этот совет',
    'Down': 'Вниз',
    'Downloading blueprint...': 'Загрузка чертежа...',
    'Dynamics': 'Динамика',
    'East': 'Восток',
    'End Radius': 'Конечный радиус',
    'Equidistant': 'С равными промежутками',
    'Examples': 'Примеры',
    'Export as CSV': 'Экспорт в CSV',
    'Export Minecraft Structure NBT': 'Экспорт структуры Minecraft (NBT)',
    'Export Schematic': 'Экспорт схемы',
    'Exporting to old versions is a Commercial License feature': 'Экспорт в старые версии — функция коммерческой лицензии',
    'Exporting...': 'Экспорт...',
    'Exporting...###ExportPrompt': 'Экспорт...###ExportPrompt',
    'Factor': 'Коэффициент',
    'Fill Edges': 'Заполнить края',
    'Fill gaps replaces all unreachable air blocks inside your build with your active block': '«Заполнить пустоты»: заменяет все недостижимые блоки воздуха внутри постройки активным блоком',
    'Fill unreachable replaces all unreachable non-air blocks inside your build with your active block': '«Заполнить недостижимое»: заменяет все недостижимые блоки (кроме воздуха) внутри постройки активным блоком',
    'Filter Selected Blocks': 'Фильтр выбранных блоков',
    'Finish Early': 'Завершить досрочно',
    'Finish Lines': 'Завершить линии',
    'Fix Lighting/Data': 'Исправить освещение/данные',
    'Fixed': 'Фиксированный',
    'Flip (X)': 'Отразить (X)',
    'Flip (Y)': 'Отразить (Y)',
    'Flip (Z)': 'Отразить (Z)',
    'Flip X': 'Отразить X',
    'Flip Y': 'Отразить Y',
    'Flip Z': 'Отразить Z',
    'Flip...': 'Отразить...',
    'Flow Length': 'Длина течения',
    'Fluid Simulation': 'Симуляция жидкости',
    'Force write anyways (dangerous!)': 'Всё равно записать (опасно!)',
    'Freehand': 'От руки',
    'From': 'От',
    'Gradually rebuilds a selection block-by-block. This feature is intended for content creators making video showcases of a build': 'Постепенно перестраивает выделение блок за блоком. Функция предназначена для авторов видео, показывающих постройку',
    'Grass surface': 'Поверхность с травой',
    'Hide': 'Скрыть',
    'Hollow replaces all unreachable blocks inside your build with air': '«Сделать полым»: заменяет все недостижимые блоки внутри постройки воздухом',
    'Horizontal': 'Горизонтально',
    'Horizontal Multiplier': 'Горизонтальный множитель',
    'Horizontal Sheets': 'Горизонтальные слои',
    "I know what I'm doing!": 'Я знаю, что делаю!',
    "I'm sure! Delete them": 'Да, уверен! Удалить',
    'If any input is true, then the output is true': 'Если хоть один вход истинен, выход истинен',
    'If possible, please take a backup of the world or region before applying this operation.': 'По возможности сделайте резервную копию мира или региона перед применением этой операции.',
    'Image URL': 'URL изображения',
    'Infinite Sources': 'Бесконечные источники',
    'Install': 'Установить',
    'Instant Place': 'Мгновенная установка',
    'Instead of placing blocks, will show the shortest path leading to the edge of the world. Useful for finding gaps inside a build leading to the outside': 'Вместо установки блоков показывает кратчайший путь к краю мира. Полезно для поиска проходов изнутри постройки наружу',
    'Intensity': 'Интенсивность',
    'Introduction': 'Введение',
    'Invalid soil blocks': 'Недопустимые блоки почвы',
    'Inverts the input; the input must be false for the output to be true': 'Инвертирует вход: вход должен быть ложен, чтобы выход был истинен',
    'Lava': 'Лава',
    'Line Width': 'Ширина линии',
    'Lines': 'Линии',
    'Load a selection region from a file': 'Загрузить область выделения из файла',
    'Load Layout from Clipboard': 'Загрузить раскладку из буфера обмена',
    'Load...': 'Загрузить...',
    'Locate change': 'Найти изменение',
    'Lower edge': 'Нижний край',
    'Lua Script Brush (Hover for help)': 'Кисть на Lua-скрипте (наведите для справки)',
    'Lua Script Mask (Hover for help)': 'Маска на Lua-скрипте (наведите для справки)',
    'Magic Select': 'Волшебное выделение',
    'Main': 'Главный',
    'Mask': 'Маска',
    'Mask/selection affects both blueprint positioning and final block placement': 'Маска/выделение влияет и на позицию чертежей, и на итоговую установку блоков',
    'Mask/selection only affects final block placement': 'Маска/выделение влияет только на итоговую установку блоков',
    "Mask/selection only affects where the blueprints can be positioned, e.g. masking 'block = grass_block' will make it so that the stamp tool can only place on grass blocks": 'Маска/выделение влияет только на то, где можно разместить чертежи; например, маска «block = grass_block» позволит инструменту штампа ставить только на блоки травы',
    'Masking': 'Маскирование',
    'Masks only apply to tools while this window is open, docked or minimised. Closing this window will result in the mask deactivating': 'Маски действуют на инструменты, только пока это окно открыто, закреплено или свёрнуто. Закрытие окна отключит маску',
    'Matches all non-air blocks': 'Совпадает со всеми блоками, кроме воздуха',
    'Matches all solid blocks': 'Совпадает со всеми твёрдыми блоками',
    'Matches blocks that have the same material (eg. cobblestone includes Cobblestone, Cobblestone Stairs, Cobblestone Walls, etc.)': 'Совпадает с блоками того же материала (например, булыжник включает булыжник, булыжные ступени, булыжные стены и т. д.)',
    'Matches the biome at the target position': 'Совпадает с биомом в целевой позиции',
    'Matches the block above the target position': 'Совпадает с блоком над целевой позицией',
    'Matches the block at the target position': 'Совпадает с блоком в целевой позиции',
    'Matches the block below the target position': 'Совпадает с блоком под целевой позицией',
    'Matches the same blockstate, checking block properties': 'Совпадает с тем же состоянием блока с проверкой свойств',
    'Max Delay (Seconds)': 'Макс. задержка (секунды)',
    'Merge Blocks': 'Объединять блоки',
    'Mode': 'Режим',
    'Move Selection': 'Переместить выделение',
    'Multiple blocks': 'Несколько блоков',
    'No active selection': 'Нет активного выделения',
    'No text annotations found :(': 'Текстовые аннотации не найдены :(',
    'North': 'Север',
    'Octahedron': 'Октаэдр',
    'Offset': 'Смещение',
    'Offsets the target position, affecting all masks inside': 'Смещает целевую позицию, влияя на все маски внутри',
    'Only propagates water sideways if the block below is solid': 'Распространяет воду в стороны, только если блок ниже твёрдый',
    'Opacity': 'Непрозрачность',
    'Open Source Licenses': 'Лицензии открытого ПО',
    'Open Source Licenses###OpenSourceLicenses': 'Лицензии открытого ПО###OpenSourceLicenses',
    'Options': 'Опции',
    'Other': 'Прочее',
    'Parameters': 'Параметры',
    'Paste Copy': 'Вставить копию',
    'Paste Entities': 'Вставить сущности',
    'Paste and Select': 'Вставить и выбрать',
    'Pick Block Drag': 'Выбор блока перетаскиванием',
    'Place Structure Blocks to specify the starting points of the animation. If no Structure Blocks are present then the center is used instead': 'Поставьте структурные блоки, чтобы задать точки начала анимации. Если структурных блоков нет, используется центр',
    'Placement does not contain any Air blocks\nIf you wanted to copy air, tick "Copy Air" in the selection window': 'Размещение не содержит блоков воздуха\nЕсли нужно копировать воздух, отметьте «Копировать воздух» в окне выделения',
    'Placement does not contain any entities\nIf you wanted to copy entities, tick "Copy Entities" in the selection window': 'Размещение не содержит сущностей\nЕсли нужно копировать сущности, отметьте «Копировать сущности» в окне выделения',
    'Player Position': 'Позиция игрока',
    'Presets': 'Пресеты',
    'Preview': 'Предпросмотр',
    'Properties': 'Свойства',
    'Quality': 'Качество',
    'Random Extra Delay (Seconds)': 'Случайная доп. задержка (секунды)',
    'Raycast Fluids': 'Трассировка жидкостей',
    'Redo to this point': 'Повторить до этого момента',
    'Redoing': 'Повтор…',
    'Relative': 'Относительно',
    'Relative - Offsets blocks by multiples of the selection size\nAbsolute - Offsets blocks by exact coordinates': 'Относительно — смещение блоков кратно размеру выделения\nАбсолютно — смещение блоков по точным координатам',
    'Remove': 'Убрать',
    'Remove Fluids Without Sources': 'Убрать жидкости без источников',
    "Removes extraneous fluids at the end of the operation if it isn't connected to a source block": 'В конце операции убирает лишнюю жидкость, не соединённую с блоком-источником',
    'Replace List': 'Список замен',
    'Replace Solid Blocks': 'Заменять твёрдые блоки',
    'Replace with Air': 'Заменить воздухом',
    'Replace with other block': 'Заменить другим блоком',
    'Replace with Substitute': 'Заменить заменителем',
    'Require Support To Flow': 'Требовать опору для течения',
    'Restore Default Layout': 'Восстановить раскладку по умолчанию',
    'Revert Change##RevertWarning': 'Откатить изменение##RevertWarning',
    'Revert without undo': 'Откатить без отмены',
    'Revolve': 'Оборот',
    'Rotate Clockwise (X)': 'Повернуть по часовой (X)',
    'Rotate Clockwise (Y)': 'Повернуть по часовой (Y)',
    'Rotate Clockwise (Z)': 'Повернуть по часовой (Z)',
    'Rotate Copies': 'Повернуть копии',
    'Rotate Y': 'Повернуть Y',
    'Rows': 'Строки',
    'Same Block Delay (Seconds)': 'Задержка для тех же блоков (секунды)',
    'Save selection region to a file. Can be used in external programs, or loaded through the Load... button below': 'Сохранить область выделения в файл. Можно использовать во внешних программах или загрузить кнопкой «Загрузить...» ниже',
    'Save...': 'Сохранить...',
    'Scale': 'Масштаб',
    'Schematic Files': 'Файлы схем',
    'Script': 'Скрипт',
    'Scripting': 'Скрипты',
    'Sculpt Draw': 'Скульптурное рисование',
    'Search': 'Поиск',
    'Segments': 'Сегменты',
    'Selection': 'Выделение',
    'Selection is empty': 'Выделение пусто',
    'Selection Options': 'Параметры выделения',
    'Server Blueprints': 'Серверные чертежи',
    'Server Blueprints are a Commercial License feature. Click for more information': 'Серверные чертежи — функция коммерческой лицензии. Нажмите для подробностей',
    "Server doesn't support (or doesn't allow) requesting entity data": 'Сервер не поддерживает (или не разрешает) запрос данных сущностей',
    "Server doesn't support axiom:tick_blocks": 'Сервер не поддерживает axiom:tick_blocks',
    'Server has disallowed exporting': 'Сервер запретил экспорт',
    'Server has disallowed importing': 'Сервер запретил импорт',
    'Server has disallowed saving blueprints': 'Сервер запретил сохранение чертежей',
    'Server has disallowed the use of Editor views': 'Сервер запретил использование видов редактора',
    'Server has not given you permission to clear all annotations': 'Сервер не дал разрешение удалить все аннотации',
    "Server hasn't given you permission to upload blueprints": 'Сервер не дал разрешение загружать чертежи',
    "Server hasn't given you permission to use this tool": 'Сервер не дал разрешение использовать этот инструмент',
    'Server Heightmaps##ChooseServerHeightmap': 'Серверные карты высот##ChooseServerHeightmap',
    "Set Pos1 to the player's current position": 'Задать первую точку на позиции игрока',
    "Set Pos2 to the player's current position": 'Задать вторую точку на позиции игрока',
    'Set the compatibility version for Blocks. Incompatible newer blocks will be highlighted with a red background': 'Задать версию совместимости блоков. Несовместимые более новые блоки будут подсвечены красным фоном',
    'Set##SetPos1': 'Задать##SetPos1',
    'Set##SetPos2': 'Задать##SetPos2',
    'Shadow': 'Тень',
    'Shape': 'Фигура',
    'Shoreline': 'Береговая линия',
    'Simplex noise': 'Симплекс-шум',
    'Single (Deferred)': 'Одиночная (отложенная)',
    'Single (Immediate)': 'Одиночная (немедленная)',
    'Single block##SingleBlockMask': 'Один блок##SingleBlockMask',
    'Single block##SingleBlockScript': 'Один блок##SingleBlockScript',
    'Skip Tutorial': 'Пропустить обучение',
    'Slope': 'Уклон',
    'Snap to ground': 'Привязка к земле',
    'Snow': 'Снег',
    'Source': 'Источник',
    'Source & Destination': 'Источник и назначение',
    'South': 'Юг',
    'Spike': 'Шип',
    'Start Delay (Seconds)': 'Начальная задержка (секунды)',
    'Start Radius': 'Начальный радиус',
    'Starting Point': 'Точка начала',
    'Store Current Layout as Default': 'Сохранить текущую раскладку как стандартную',
    'Substitute': 'Заменитель',
    'Suffix must start with ###': 'Суффикс должен начинаться с ###',
    'Sun Angle': 'Угол солнца',
    'Taper': 'Сужение',
    'Text': 'Текст',
    'Texture': 'Текстура',
    'The server has disallowed the use of blueprints': 'Сервер запретил использование чертежей',
    'Thickness': 'Толщина',
    "This function will revert the history change without undoing to that point. It will create a new history element. This is an advanced feature and should not be used unless you know what you're doing!": 'Эта функция откатит изменение истории, не отменяя до той точки. Будет создан новый элемент истории. Это продвинутая функция, не используйте её, если не уверены в своих действиях!',
    'This is likely an issue with Axiom.': 'Похоже, это проблема Axiom.',
    'This operation cannot be undone because it causes side-effects: water flowing, sand blocks converting into falling sand, etc.': 'Эту операцию нельзя отменить: у неё есть побочные эффекты — течение воды, превращение песка в падающий песок и т. д.',
    'THIS OPERATION CANNOT BE UNDONE!': 'ЭТУ ОПЕРАЦИЮ НЕЛЬЗЯ ОТМЕНИТЬ!',
    'Tip: You can use Ctrl+Click to select multiple blocks for replacement': 'Совет: с помощью Ctrl+клик можно выбрать несколько блоков для замены',
    'To': 'К',
    'Tools': 'Инструменты',
    'Top 3-4 blocks': 'Верхние 3–4 блока',
    'Total X': 'Всего X',
    'Total Y': 'Всего Y',
    'Total Z': 'Всего Z',
    'Translate Copies': 'Сместить копии',
    'Turns the initial region into infinite sources, making them able to propagate water without being drained': 'Превращает начальную область в бесконечные источники: вода распространяется, не истощаясь',
    'Twist': 'Скручивание',
    'Type': 'Тип',
    'Unconfirm': 'Снять подтверждение',
    'Undo to this point': 'Отменить до этого момента',
    'Undo/Redo Warning!##HistoryUndoWarning': 'Предупреждение отмены/повтора!##HistoryUndoWarning',
    'Undoing': 'Отмена…',
    'Unknown': 'Неизвестно',
    'Unnamed Heightmap': 'Безымянная карта высот',
    'Up': 'Вверх',
    'Update': 'Обновить',
    'Upper edge': 'Верхний край',
    'Use Viewport Center for Arcball': 'Центр окна просмотра для arcball',
    'Version': 'Версия',
    'Vertical': 'Вертикально',
    'Vertical Multiplier': 'Вертикальный множитель',
    'Very Random': 'Полностью случайно',
    'View All Text Annotations': 'Показать все текстовые аннотации',
    'Views': 'Виды',
    'Voronoi edges noise': 'Шум краёв Вороного',
    'Warning: ': 'Предупреждение: ',
    'Water': 'Вода',
    'West': 'Запад',
    'You have been warned!': 'Вы предупреждены!',
    'not a function': 'не функция',
    '⚠ Annotations in Multiplayer is a Commercial License feature. Click for more information': '⚠ Аннотации в мультиплеере — функция коммерческой лицензии. Нажмите для подробностей',
    '⚠ Ensure you create a backup of your build before using this operation on it': '⚠ Обязательно сделайте резервную копию постройки перед применением этой операции',
    '⚠ History is not being saved': '⚠ История не сохраняется',
    '⚠ Maximum outline box size reached. Boxes with a volume of >1,000,000 are disallowed for performance reasons': '⚠ Достигнут максимальный размер рамки. Рамки объёмом больше 1 000 000 запрещены ради производительности',
    '⚠ Missing data for current Minecraft version. Version exporting is unavailable.': '⚠ Нет данных для текущей версии Minecraft. Экспорт по версиям недоступен.',
    '⚠ Missing selection': '⚠ Нет выделения',
    '⚠ The server does not allow creating annotations': '⚠ Сервер не разрешает создавать аннотации',
    'Split': 'Разделить',
    'Width': 'Ширина',
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


# Классы, где совпавшие литералы не являются подписями: CustomImGuiImplGl3 —
# имя атрибута шейдера ('Color'), StyleEditorWindow — демо-виджеты редактора
# стилей ImGui. Их классы патчером ImGui не обрабатываются.
IMGUI_EXCLUDED_CLASSES = {'CustomImGuiImplGl3', 'CustomImGuiImplGlfw',
                          'StyleEditorWindow'}


def patch_imgui(data: bytes) -> bytes:
    global REPL
    saved = REPL
    try:
        REPL = {**saved, **REPL_IMGUI}
        return patch(data)
    finally:
        REPL = saved


if __name__ == '__main__':
    for path in sys.argv[1:]:
        print(path)
        with open(path, 'rb') as f:
            data = f.read()
        if 'LocalizationLoader' in path:
            patched = patch_localization_loader(data)
        elif (any(seg in path for seg in ('/editor/', '/tools/', '/windows/',
                                          '/global_mask/'))
              and not any(name in path for name in IMGUI_EXCLUDED_CLASSES)):
            patched = patch_imgui(data)
        else:
            patched = patch(data)
        with open(path, 'wb') as f:
            f.write(patched)
