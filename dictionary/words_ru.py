# -*- coding: utf-8 -*-
"""Общий однословный словарь EN -> RU — последний рубеж перевода.

Сюда попадает всё, что не является названием блока: слова интерфейса,
обрывки предложений, названия предметов, прилагательные-украшения.
Одна запись здесь закрывает сразу сотни ключей в lang-файлах модов.

Значения — именительный падеж. Для прилагательных по умолчанию мужской род.
"""

WORDS = {
    'channel': 'Канал',
    'contents': 'Содержимое',
    'input': 'Вход',
    'output': 'Выход',
    'search': 'Поиск',
    'radius': 'Радиус',
    'title': 'Заголовок',
    'debug': 'Отладка',
    'loading': 'Загрузка',
    'preview': 'Предпросмотр',
    'mode': 'Режим',
    'type': 'Тип',
    'name': 'Название',
    'value': 'Значение',
    'amount': 'Количество',
    'time': 'Время',
    'delay': 'Задержка',
    'speed': 'Скорость',
    'level': 'Уровень',
    'distance': 'Дистанция',
    'height': 'Высота',
    'width': 'Ширина',
    'count': 'Счётчик',
    'color': 'Цвет',
    'size': 'Размер',
    'target': 'Цель',
    'source': 'Источник',
    'filter': 'Фильтр',
    'limit': 'Предел',
    'maximum': 'Максимум',
    'minimum': 'Минимум',
    'step': 'Шаг',
    'text': 'Текст',
    'toggle': 'Переключатель',
    'version': 'Версия',
    'weight': 'Вес',
    'player': 'Игрок',
    'item': 'Предмет',
    'energy': 'Энергия',
    'fluid': 'Жидкость',
    'settings': 'Настройки',
    'options': 'Параметры',
    'profile': 'Профиль',
    'slot': 'Слот',
    'cooldown': 'Перезарядка',
    'threshold': 'Порог',
    'multiplier': 'Множитель',
    'dimension': 'Измерение',
    'biome': 'Биом',
    'weather': 'Погода',
    'backup': 'Резервная копия',

    'save': 'Сохранить',
    'load': 'Загрузить',
    'edit': 'Изменить',
    'delete': 'Удалить',
    'close': 'Закрыть',
    'cancel': 'Отмена',
    'confirm': 'Подтвердить',
    'back': 'Назад',
    'next': 'Далее',
    'previous': 'Назад',
    'search': 'Поиск',
    'refresh': 'Обновить',
    'apply': 'Применить',
    'reset': 'Сбросить',
    'copy': 'Копировать',
    'paste': 'Вставить',
    'done': 'Готово',
    'yes': 'Да',
    'no': 'Нет',
    'open': 'Открыть',
    'new': 'Создать',

    'blood-spattered': 'забрызганный кровью',
    'blood-splatted': 'забрызганный кровью',
    'spattered': 'забрызганный',
    'splatted': 'забрызганный',
    'six-pack': 'упаковка по шесть',
    'whitelist': 'белый список',
    'blacklist': 'чёрный список',
    'overlay': 'оверлей',
    'auto': 'авто',
    'nearby': 'ближайший',
    'eating': 'поедающий',
    'space': 'космический',
    'furnace': 'печь',
    'crusher': 'дробилка',
    'striker': 'ударник',
    'transmitter': 'передатчик',
    'receiver': 'приёмник',
    'generator': 'генератор',
    'reactor': 'реактор',
    'battery': 'батарея',
    'cable': 'кабель',
    'wire': 'провод',
    'machine': 'машина',
    'motor': 'мотор',
    'pump': 'насос',


    "Zeon,": "Зеон,",
    "Zeon": "Зеон",
    "O'": "О'",
    "O": "О",
    "o'": "о'",
    "zeon,": "зеон,",
    "zeon": "зеон",
    # ── служебные слова (куски предложений) ──
    'a': 'a', 'an': 'a', 'the': 'the', 'of': 'of', 'to': 'to', 'in': 'in',
    'on': 'на', 'at': 'у', 'with': 'с', 'for': 'для', 'and': 'и', 'or': 'или',
    'from': 'из', 'by': 'через', 'as': 'как', 'if': 'если', 'is': 'есть',
    'are': 'есть', 'be': 'быть', 'not': 'нет', 'no': 'нет', 'do': 'делать',
    'does': 'делает', 'has': 'есть', 'have': 'есть', 'can': 'может',
    'will': 'будет', 'you': 'вы', 'your': 'ваш', 'my': 'мой', 'we': 'мы',
    'it': 'оно', 'its': 'его', 'they': 'они', 'them': 'их', 'their': 'их',
    'this': 'это', 'that': 'тот', 'these': 'эти', 'those': 'те',
    'some': 'некоторые', 'all': 'все', 'more': 'больше', 'less': 'меньше',
    'up': 'вверх', 'down': 'вниз', 'out': 'наружу', 'into': 'в',
    'over': 'поверх', 'under': 'под', 'with': 'с', 'without': 'без',
    'please': 'пожалуйста', 'get': 'получить', 'give': 'дайте',
    'put': 'положить', 'pull': 'тянуть', 'push': 'толкать', 'make': 'сделать',
    'makes': 'делает', 'made': 'сделанный', 'add': 'добавить',
    'remove': 'убрать', 'use': 'использовать', 'used': 'использованный',
    'enable': 'включить', 'enabled': 'включённый', 'disable': 'выключить',
    'disabled': 'выключенный', 'show': 'показать', 'hide': 'скрыть',
    'getting': 'получение', 'got': 'получил', 'set': 'установить',
    'reset': 'сброс', 'start': 'старт', 'stop': 'стоп', 'on_': 'вкл',

    # ── интерфейс ──
    'intro': 'введение', 'config': 'настройки', 'configuration': 'настройки',
    'settings': 'настройки', 'options': 'параметры', 'option': 'параметр',
    'guide': 'руководство', 'help': 'помощь', 'preview': 'предпросмотр',
    'toggle': 'переключатель', 'name': 'имя', 'version': 'версия',
    'update': 'обновление', 'updates': 'обновления', 'changelog': 'изменения',
    'credits': 'авторы', 'authors': 'авторы', 'author': 'автор',
    'achievements': 'достижения', 'recipe': 'рецепт', 'recipes': 'рецепты',
    'none': 'нет', 'yes': 'да', 'ok': 'ок', 'amount': 'количество',
    'max': 'макс.', 'min': 'мин.', 'mode': 'режим', 'sound': 'звук',
    'data': 'данные', 'nbt': 'NBT', 'tag': 'тег', 'tags': 'теги',
    'oredict': 'OreDict', 'sync': 'синхр.', 'server': 'сервер',
    'client': 'клиент', 'world': 'мир', 'worldgen': 'генерация мира',
    'saves': 'сохранения', 'slot': 'слот', 'slots': 'слоты',
    'inventory': 'инвентарь', 'screen': 'экран', 'menu': 'меню',
    'button': 'кнопка', 'back': 'назад', 'next': 'далее', 'close': 'закрыть',
    'cancel': 'отмена', 'confirm': 'подтвердить', 'edit': 'изменить',
    'left': 'левый', 'right': 'правый', 'center': 'центр', 'centre': 'центр',
    'north': 'север', 'south': 'юг', 'east': 'восток', 'west': 'запад',
    'up_': 'вверх', 'down_': 'вниз', 'forward': 'вперёд', 'side': 'бок',
    'top': 'верх', 'bottom': 'низ', 'first': 'первый', 'second': 'второй',
    'third': 'третий', 'fourth': 'четвёртый', 'last': 'последний',
    'next_': 'следующий', 'previous': 'предыдущий', 'mod': 'мод',
    'mods': 'моды', 'guis': 'интерфейс', 'tooltip': 'подсказка',
    'tooltips': 'подсказки', 'info': 'информация', 'information': 'информация',
    'infos': 'сведения', 'wiki': 'вики', 'website': 'сайт', 'video': 'видео',
    'discord': 'Discord', 'patreon': 'Patreon', 'github': 'GitHub',
    'donate': 'поддержать', 'support': 'поддержка', 'supported': 'поддерживается',

    # ── состояния и качества ──
    'on': 'вкл', 'off': 'выкл', 'active': 'активный', 'inactive': 'неактивный',
    'valid': 'верный', 'invalid': 'неверный', 'empty': 'пустой',
    'full': 'полный', 'half': 'половина', 'quarter': 'четверть',
    'default': 'по умолчанию', 'custom': 'свой', 'normal': 'обычный',
    'simple': 'простой', 'basic': 'базовый', 'advanced': 'продвинутый',
    'expert': 'экспертный', 'hard': 'сложный', 'easy': 'лёгкий',
    'true': 'истина', 'false': 'ложь', 'dead': 'мёртвый', 'alive': 'живой',
    'broken': 'сломанный', 'damaged': 'повреждённый', 'repaired': 'починенный',
    'corroded': 'ржавый', 'rusted': 'ржавый', 'rust': 'ржавчина',
    'worn': 'изношенный', 'old': 'старый', 'new': 'новый', 'ancient': 'древний',
    'modern': 'современный', 'classic': 'классический', 'legacy': 'устаревший',
    'special': 'особый', 'specific': 'определённый', 'generic': 'обычный',
    'partial': 'частичный', 'complete': 'полный', 'incomplete': 'неполный',
    'entering': 'ввод', 'unknown': 'неизвестно', 'random': 'случайный',

    # ── материалы и руды ──
    'iron': 'железный', 'gold': 'золотой', 'copper': 'медный',
    'bronze': 'бронзовый', 'brass': 'латунный', 'steel': 'стальной',
    'silver': 'серебряный', 'tin': 'оловянный', 'aluminum': 'алюминиевый',
    'lead': 'свинцовый', 'nickel': 'никелевый', 'platinum': 'платиновый',
    'cobalt': 'кобальтовый', 'bismuth': 'висмутовый', 'invar': 'инваровый',
    'electrum': 'электрумовый', 'diamatine': 'диаматин', 'thaumium': 'таумиум',
    'amber': 'янтарный', 'silver_': 'серебряный', 'alloy': 'сплав',
    'alloys': 'сплавы', 'synth': 'синтетический', 'void': 'пустотный',
    'voidstone': 'пустотный камень', 'enori': 'энори', 'restonia': 'рестония',
    'quarkal': 'кваркал', 'holystone': 'холистоун', 'vittle': 'виттл',
    'lavastone': 'лавастоун', 'waterstone': 'вотерстоун', 'grimstone': 'гримстоун',
    'birdstone': 'бердстоун', 'bloodstone': 'кровавик', 'amberstone': 'янтарь',
    'moonstone': 'лунный камень', 'sunstone': 'солнечный камень',
    'claystone': 'глиняный камень', 'redstone': 'редстоун',
    'zinc': 'цинковый', 'bismuth_': 'висмутовый', 'osmium': 'осмиевый',
    'uronium': 'урановый', 'thorium': 'тоориевый', 'titanium': 'титановый',

    # ── органика и природа ──
    'organic': 'органический', 'natural': 'натуральный', 'plant': 'растение',
    'plants': 'растения', 'seed': 'семя', 'seeds': 'семена',
    'leaf': 'лист', 'leaves': 'листья', 'log': 'бревно', 'wood': 'дерево',
    'wooden': 'деревянный', 'bark': 'кора', 'sapling': 'саженец',
    'flower': 'цветок', 'floral': 'цветочный', 'moss': 'мох', 'vine': 'лиана',
    'root': 'корень', 'roots': 'корни', 'soil': 'почва', 'dirt': 'земля',
    'clay': 'глина', 'sand': 'песок', 'gravel': 'гравий', 'rock': 'камень',
    'rocky': 'скалистый', 'muddy': 'грязный', 'dust': 'пыль',
    'mud': 'грязь', 'bone': 'кость', 'bones': 'кости', 'skull': 'череп',
    'skulls': 'черепа', 'skeleton': 'скелет', 'creeper': 'крипер',
    'zombie': 'зомби', 'spider': 'паук', 'enderman': 'эндермен',
    'piglin': 'пиглин', 'village': 'деревня', 'dungeon': 'подземелье',
    'tomb': 'гробница', 'ruins': 'руины', 'caves': 'пещеры',
    'water': 'вода', 'ice': 'лёд', 'lava': 'лава', 'fire': 'огонь',
    'flame': 'пламя', 'smoke': 'дым', 'steam': 'пар', 'fog': 'туман',
    'cloud': 'облако', 'sky': 'небо', 'star': 'звезда', 'stars': 'звёзды',
    'moon': 'луна', 'sun': 'солнце', 'light': 'свет', 'lights': 'светильники',
    'dark': 'тёмный', 'bright': 'яркий', 'glowing': 'светящийся',
    'shining': 'сияющий', 'dim': 'тусклый', 'neon': 'неон',
    'solar': 'солнечный', 'lunar': 'лунный', 'meteoric': 'метеоритный',
    'eclipse': 'затмение', 'aurora': 'северное сияние', 'halo': 'ореол',

    # ── размер, форма, узор ──
    'shape': 'форма', 'shaped': 'формованный', 'shapeless': 'бесформенный',
    'size': 'размер', 'small': 'малый', 'tiny': 'крошечный',
    'medium': 'средний', 'large': 'большой', 'giant': 'огромный',
    'mini': 'мини', 'mega': 'мега', 'miniature': 'миниатюрный',
    'round': 'круглый', 'square': 'квадратный', 'circular': 'круглый',
    'triangular': 'треугольный', 'hexagonal': 'шестиугольный',
    'rhombuses': 'ромбы', 'ovals': 'овалы', 'cones': 'конусы',
    'cubes': 'кубы', 'crystal': 'кристалл', 'crystals': 'кристаллы',
    'shard': 'осколок', 'shards': 'осколки', 'cluster': 'кластер',
    'clusters': 'кластеры', 'ring': 'кольцо', 'rings': 'кольца',
    'beads': 'бусины', 'lines': 'линии', 'dots': 'точки',
    'glyphs': 'глифы', 'hieroglyphs': 'иероглифы', 'symbols': 'символы',
    'pattern': 'узор', 'patterns': 'узоры', 'design': 'дизайн',
    'texture': 'текстура', 'detail': 'деталь', 'decor': 'декор',
    'ornate': 'нарядный', 'ornamental': 'декоративный', 'decorated': 'украшенный',
    'plain': 'простой', 'blank': 'пустой', 'mixed': 'смешанный',
    'alternating': 'чередующийся', 'rotated': 'повёрнутый',
    'rotary': 'вращающийся', 'symmetrical': 'симметричный',
    'asymmetrical': 'асимметричный', 'offset': 'смещённый',
    'shifted': 'сдвинутый', 'flipped': 'перевёрнутый', 'inverted': 'инвертированный',
    'mirrored': 'зеркальный', 'upside': 'вверх ногами', 'twisted': 'витой',
    'curved': 'изогнутый', 'bent': 'гнутый', 'spiral': 'спиральный',
    'radial': 'радиальный', 'concentric': 'концентрический',
    'dotted': 'точечный', 'spotted': 'пятнистый', 'stained': 'запятнанный',
    'mottled': 'пятнистый', 'speckled': 'крапчатый', 'flecked': 'крапчатый',
    'swirling': 'вихревой', 'wavy': 'волнистый', 'choppy': 'волнистый',
    'zigzag': 'зигзаг', 'woven': 'плетёный', 'weaved': 'плетёный',
    'knitted': 'вязаный', 'braided': 'плетёный', 'twisted_': 'витой',
    'checkered': 'шахматный', 'tinted': 'тонированный', 'colored': 'цветной',
    'coloured': 'цветной', 'dyed': 'крашеный', 'painted': 'крашеный',
    'polished': 'полированный', 'waxed': 'вощёный', 'oiled': 'масляный',
    'mossy': 'мшистый', 'grimy': 'грязный', 'faded': 'выцветший',
    'aged': 'состаренный', 'wavy_': 'волнистый', 'framed': 'обрамлённый',
    'bordered': 'окантованный', 'rimmed': 'окантованный', 'capped': 'с навершием',
    'topped': 'с верхом', 'bottomed': 'с низом', 'walled': 'с стенкой',

    # ── предметы и механизмы ──
    'item': 'предмет', 'items': 'предметы', 'block': 'блок',
    'blocks': 'блоки', 'machine': 'машина', 'machines': 'машины',
    'tool': 'инструмент', 'tools': 'инструменты', 'weapon': 'оружие',
    'armor': 'броня', 'helmet': 'шлем', 'chestplate': 'нагрудник',
    'boots': 'сапоги', 'shield': 'щит', 'sword': 'меч', 'axe': 'топор',
    'pickaxe': 'кирка', 'shovel': 'лопата', 'hoe': 'мотыга', 'hammer': 'молот',
    'wrench': 'гаечный ключ', 'rod': 'удочка', 'bow': 'лук',
    'arrow': 'стрела', 'music': 'музыка', 'disc': 'диск', 'book': 'книга',
    'books': 'книги', 'tome': 'том', 'tomes': 'тома', 'paper': 'бумага',
    'papers': 'бумаги', 'page': 'страница', 'map': 'карта', 'scroll': 'свиток',
    'sign': 'табличка', 'banner': 'баннер', 'banners': 'баннеры',
    'chest': 'сундук', 'chests': 'сундуки', 'crate': 'ящик',
    'crates': 'ящики', 'barrel': 'бочка', 'bucket': 'ведро', 'bottle': 'бутылка',
    'bottles': 'бутылки', 'bowl': 'миска', 'cup': 'чашка', 'cups': 'чашки',
    'jar': 'банка', 'jars': 'банки', 'plate': 'тарелка', 'plates': 'тарелки',
    'coins': 'монеты', 'coin': 'монета', 'ingot': 'слиток', 'ingots': 'слитки',
    'nugget': 'кусочек', 'dust_': 'пыль', 'gem': 'самоцвет', 'jewel': 'самоцвет',
    'ore': 'руда', 'mineral': 'минерал', 'battery': 'батарея',
    'batteries': 'батареи', 'energy': 'энергия', 'power': 'энергия',
    'fuel': 'топливо', 'rocket': 'ракета', 'jetpack': 'реактивный ранец',
    'generator': 'генератор', 'engine': 'двигатель', 'motor': 'мотор',
    'turbine': 'турбина', 'pipe': 'труба', 'pipes': 'трубы',
    'cable': 'кабель', 'cables': 'кабели', 'wire': 'провод',
    'battery_': 'батарея', 'reactor': 'реактор', 'furnace': 'печь',
    'smelter': 'плавильня', 'crusher': 'дробилка', 'grinder': 'измельчитель',
    'sorter': 'сортировщик', 'furnace_': 'печь', 'extractor': 'экстрактор',
    'assembler': 'сборщик', 'fabricator': 'изготовитель', 'alloyer': 'сплавщик',
    'press': 'пресс', 'lathe': 'токарный станок', 'centrifuge': 'центрифуга',
    'crystallizer': 'кристаллизатор', 'atomizer': 'атомайзер',
    'reconstructor': 'реконструктор', 'enrichment': 'обогащение',
    'infuser': 'инфузор', 'inscriber': 'гравировщик', 'smeltery': 'плавильня',
    'multiservo': 'мультисерво', 'servo': 'серво', 'piston': 'поршень',
    'gear': 'шестерня', 'gears': 'шестерни', 'axle': 'ось', 'shaft': 'вал',
    'shafts': 'валы', 'bearing': 'подшипник', 'screw': 'винт',
    'nut': 'гайка', 'bolt': 'болт', 'rivet': 'заклёпка', 'riveted': 'заклёпочный',
    'plate_': 'пластина', 'sheet': 'лист', 'sheets': 'листы',
    'mesh': 'сетка', 'grid': 'решётка', 'net': 'сеть', 'filter': 'фильтр',
    'panel': 'панель', 'panels': 'панели', 'wall': 'стена', 'walls': 'стены',
    'floor': 'пол', 'flooring': 'настил', 'ceiling': 'потолок',
    'roof': 'крыша', 'pillar': 'колонна', 'pillar_': 'колонна',
    'column': 'столб', 'beams': 'балки', 'beam': 'балка', 'bar_': 'брус',
    'fence': 'забор', 'gate': 'ворота', 'ladder': 'лестница',
    'stair': 'ступенька', 'stairs': 'ступеньки', 'slab': 'плита',
    'brick': 'кирпич', 'bricks': 'кирпичи', 'tile': 'плитка',
    'tiles': 'плитки', 'plank': 'доска', 'planks': 'доски',
    'wooden_': 'деревянный', 'masonry': 'кладка', 'mason': 'каменщик',
    'column_': 'столб', 'granite': 'гранит', 'diorite': 'диорит',
    'andesite': 'андезит', 'marble': 'мрамор', 'basalt': 'базальт',
    'sandstone': 'песчаник', 'obsidian': 'обсидиан', 'quartz': 'кварц',
    'amethyst': 'аметист', 'emerald': 'измудруд', 'diamond': 'алмаз',
    'lapis': 'лазурит', 'lazuli': 'лазурит', 'redstone': 'редстоун',
    'blackstone': 'чернит', 'terracotta': 'терракота', 'concrete': 'бетон',
    'netherrack': 'незерак', 'netherbrick': 'незерокирпич', 'endstone': 'эндстоун',
    'purpur': 'пурпур', 'prismarine': 'призмарин', 'sponge': 'губка',
    'slime': 'слизь', 'honey': 'мёд', 'wool': 'шерсть', 'felt': 'войлок',
    'candle': 'свеча', 'lamp': 'лампа', 'lamps': 'лампы', 'lantern': 'фонарь',
    'torch': 'факел', 'glowstone': 'светокамень', 'sea': 'море',
    'boat': 'лодка', 'minecart': 'вагонетка', 'cart': 'тележка',
    'furnace2': 'печь', 'seed2': 'семя', 'sap': 'сок',

    # ── живность и существа ──
    'man': 'человек', 'player': 'игрок', 'monster': 'монстр',
    'monsters': 'монстры', 'beast': 'зверь', 'beasts': 'звери',
    'animal': 'животное', 'animals': 'животные', 'pet': 'питомец',
    'bat': 'летучая мышь', 'bats': 'летучие мыши', 'worm': 'червь',
    'worms': 'черви', 'cat': 'кот', 'dog': 'собака', 'fish': 'рыба',
    'bird': 'птица', 'fox': 'лиса', 'wolf': 'волк', 'horse': 'лошадь',
    'llama': 'лама', 'crow': 'ворона', 'owl': 'сова', 'horus': 'Хор',
    'bat_': 'летучая мышь', 'goblin': 'гоблин', 'mummy': 'мумия',
    'phantom': 'фантом', 'wither': 'иссушитель', 'dragon': 'дракон',

    # ── еда и зелья ──
    'food': 'еда', 'foodstuff': 'продукты', 'bread': 'хлеб',
    'wheat': 'пшеница', 'flax': 'лён', 'carrot': 'морковь',
    'potato': 'картофель', 'apple': 'яблоко', 'berries': 'ягоды',
    'berry': 'ягода', 'soup': 'суп', 'jam': 'варенье', 'jams': 'варенье',
    'coffee': 'кофе', 'tea': 'чай', 'wine': 'вино', 'ale': 'эль',
    'beer': 'пиво', 'juice': 'сок', 'milk': 'молоко', 'cheese': 'сыр',
    'meat': 'мясо', 'fish_': 'рыба', 'rice': 'рис', 'bean': 'фасоль',
    'beans': 'фасоль', 'jellybean': 'джеллибин', 'molten': 'расплавленный',
    'crystalized': 'кристаллизованный', 'crystallized': 'кристаллизованный',
    'glazed': 'глазурованный', 'sugary': 'сахарный', 'spicy': 'острый',
    'salty': 'солёный', 'fresh': 'свежий', 'rotten': 'гнилой',
    'poison': 'яд', 'poisonous': 'ядовитый', 'toxic': 'токсичный',

    # ── время, погода, свойства ──
    'time': 'время', 'day': 'день', 'night': 'ночь', 'dawn': 'рассвет',
    'dusk': 'сумерки', 'noon': 'полдень', 'morning': 'утро',
    'evening': 'вечер', 'season': 'сезон', 'spring': 'весна',
    'summer': 'лето', 'autumn': 'осень', 'winter': 'зима',
    'weather': 'погода', 'rain': 'дождь', 'snow': 'снег', 'wind': 'ветер',
    'storm': 'шторм', 'temperature': 'температура', 'heat': 'нагрев',
    'cold': 'холод', 'cool': 'прохладный', 'frozen': 'замороженный',
    'melted': 'растаявший', 'hot': 'горячий', 'warm': 'тёплый',
    'cold_': 'холодный', 'windy': 'ветреный', 'stormy': 'штормовой',
    'rainy': 'дождливый', 'sunny': 'солнечный', 'cloudy': 'облачный',

    # ── числа-качественные ──
    'single': 'одиночный', 'double': 'двойной', 'triple': 'тройной',
    'quadruple': 'четверной', 'quintuple': 'пятерной', 'sextuple': 'шестерной',
    'dozen': 'дюжина', 'many': 'много', 'few': 'несколько', 'couple': 'пара',
    'various': 'различный', 'several': 'несколько', 'numerous': 'многочисленный',
    'countless': 'бесчисленный', 'endless': 'бесконечный',
    'infinite': 'бесконечный', 'finite': 'конечный',

    # ── абстракции ──
    'energy_': 'энергия', 'life': 'жизнь', 'death': 'смерть',
    'soul': 'душа', 'souls': 'души', 'spirit': 'дух', 'ghost': 'призрак',
    'dream': 'мечта', 'hope': 'надежда', 'luck': 'удача', 'fortune': 'богатство',
    'greed': 'жадность', 'guts': 'храбрость', 'honor': 'честь',
    'courage': 'смелость', 'wisdom': 'мудрость', 'power_': 'сила',
    'knowledge': 'знание', 'experience': 'опыт', 'level': 'уровень',
    'tier': 'ранг', 'rank': 'ранг', 'grade': 'класс', 'class': 'класс',
    'master': 'мастер', 'lord': 'лорд', 'king': 'король', 'queen': 'королева',
    'knight': 'рыцарь', 'prince': 'принц', 'princess': 'принцесса',
    'temple': 'храм', 'shrine': 'святилище', 'altar': 'алтарь',
    'sanctum': 'святилище', 'lab': 'лаборатория', 'laboratory': 'лаборатория',
    'museum': 'музей', 'library': 'библиотека', 'academy': 'академия',
    'school': 'школа', 'market': 'рынок', 'shop': 'магазин',
    'guild': 'гильдия', 'clan': 'клан', 'faction': 'фракция',
    'treasure': 'сокровище', 'treasures': 'сокровища', 'relic': 'реликвия',
    'relics': 'реликвии', 'artifact': 'артефакт', 'gem2': 'самоцвет',
    'legend': 'легенда', 'myth': 'миф', 'tale': 'повесть',
    'story': 'история', 'chronicle': 'хроника', 'journal': 'журнал',
    'letter': 'письмо', 'message': 'сообщение', 'note': 'заметка',
    'token': 'жетон', 'trophy': 'трофей', 'medal': 'медаль',
    'emblem': 'эмблема', 'sigil': 'сигил', 'rune': 'руна', 'runes': 'руны',
    'symbol': 'символ', 'seal': 'печать', 'stamp': 'штамп',
    'mark': 'метка', 'brand': 'клеймо', 'label': 'ярлык',
    'warning': 'предупреждение', 'danger': 'опасно', 'hazard': 'опасность',
    'caution': 'осторожно', 'alarm': 'сигнал тревоги', 'alert': 'тревога',
    'safe': 'безопасный', 'secure': 'защищённый', 'sealed': 'запечатанный',
    'locked': 'запертый', 'unlocked': 'открытый', 'closed': 'закрытый',
    'open': 'открытый', 'shut': 'закрытый',

    # ── события ──
    'break': 'ломать', 'breaking': 'разрушение', 'place': 'установка',
    'placing': 'установка', 'punch': 'удар', 'shoot': 'выстрел',
    'attack': 'атака', 'defend': 'защита', 'defense': 'оборона',
    'explosion': 'взрыв', 'blast': 'взрыв', 'burn': 'гореть',
    'shatter': 'разбиться', 'crack': 'трещина', 'cracks': 'трещины',
    'fracture': 'излом', 'rip': 'разорвать', 'tear': 'разрыв',
    'hit': 'попадание', 'hit2': 'попадание', 'damaging': 'урон',
    'heal': 'лечение', 'healing': 'лечение', 'revive': 'воскрешение',
    'summon': 'призыв', 'summoning': 'призыв', 'teleport': 'телепорт',
    'travel': 'путешествие', 'portal': 'портал', 'gate_': 'ворота',
    'spawn': 'спавн', 'spawner': 'спавнер', 'despawn': 'деспавн',
    'drop': 'дроп', 'drops': 'дроп', 'loot': 'лут', 'treasure_': 'сокровище',
    'harvest': 'сбор урожая', 'growth': 'рост', 'grow': 'расти',
    'plant2': 'посадка', 'till': 'вспашка', 'tilled': 'вспаханный',
    'tilling': 'вспашка', 'sow': 'сеять', 'compost': 'компост',
    'ferment': 'брожение', 'distill': 'дистилляция', 'brew': 'варка',
    'smelt': 'плавка', 'craft2': 'крафт', 'forge_': 'ковка',
    'enchant': 'зачарование', 'disenchant': 'снятие чар',
    'grind': 'измельчение', 'crush': 'дробление', 'compress': 'сжатие',
    'split': 'разделение', 'merge': 'объединение', 'duplicate': 'дублирование',
    'copy': 'копия', 'rename': 'переименовать', 'sort': 'сортировка',
    'process': 'обработка', 'scan': 'сканирование', 'probe': 'зонд',
    'detect': 'обнаружение', 'track': 'отслеживание', 'log2': 'журнал',
    'record': 'запись', 'report': 'отчёт', 'notify': 'уведомление',
    'warn': 'предупреждать', 'error': 'ошибка', 'success': 'успех',
    'failure': 'неудача', 'complete2': 'готово', 'finish': 'завершить',
    'start2': 'начать', 'stop2': 'остановить', 'pause': 'пауза',
    'resume': 'продолжить', 'retry': 'повторить', 'skip': 'пропустить',
    'choose': 'выбрать', 'select': 'выбрать', 'apply': 'применить',
    'install': 'установить', 'uninstall': 'удалить', 'download': 'скачать',
    'upload': 'загрузить', 'import': 'импорт', 'export': 'экспорт',

    # ── качества, которые часто встречаются как украшение ──
    'braced': 'укреплённый', 'studded': 'с заклёпками', 'ribbed': 'ребристый',
    'grooved': 'бороздчатый', 'fluted': 'рифлёный', 'corrugated': 'волнистый',
    'beveled': 'фасочный', 'chamfered': 'фасочный', 'engraved': 'гравированный',
    'carved': 'вырезанный', 'etched': 'вытравленный', 'scribed': 'исписанный',
    'scribed2': 'исписанный', 'inset': 'утопленный', 'raised': 'выступающий',
    'recessed': 'утопленный', 'embossed': 'тиснёный', 'emboss': 'тиснение',
    'inlaid': 'инкрустированный', 'inlay': 'инкрустация', 'encrusted': 'инкрустированный',
    'jeweled': 'самоцветный', 'gilded': 'позолоченный', 'plated': 'облицованный',
    'coated': 'покрытый', 'lacquered': 'лакированный', 'varnished': 'лакированный',
    'polished2': 'полированный', 'glossy': 'глянцевый', 'matte': 'матовый',
    'rough2': 'шершавый', 'slick': 'скользкий', 'sticky': 'липкий',
    'slippery': 'скользкий', 'bumpy': 'бугристый', 'smooth2': 'гладкий',
    'sharp': 'острый', 'blunt': 'тупой', 'thick': 'толстый', 'thin': 'тонкий',
    'narrow': 'узкий', 'broad': 'широкий', 'tight': 'тугой', 'loose': 'свободный',
    'heavy': 'тяжёлый', 'light2': 'лёгкий', 'dense': 'плотный', 'solid': 'плотный',
    'empty2': 'пустой', 'light3': 'лёгкий',

    # ── прочее ──
    'new_': 'новый', 'old_': 'старый', 'young': 'молодой', 'great': 'великий',
    'small_': 'малый', 'little': 'маленький', 'tiny_': 'крошечный',
    'huge': 'огромный', 'massive': 'массивный', 'mini_': 'мини',
    'chaotic': 'хаотичный', 'chaos': 'хаос', 'order': 'порядок',
    'law': 'закон', 'nature': 'природа', 'art': 'искусство',
    'music_': 'музыка', 'dance': 'танец', 'song': 'песня',
    'game': 'игра', 'play': 'играть', 'win': 'победа', 'lose': 'поражение',
    'luck2': 'удача', 'fate': 'судьба', 'karma': 'карма',
    'secret': 'тайный', 'hidden': 'скрытый', 'private': 'частный',
    'public': 'общественный', 'royal': 'королевский', 'imperial': 'имперский',
    'imperialistic': 'имперский', 'ancient2': 'древний',
    'rebelli': 'мятежный', 'rebellious': 'мятежный', 'rebel': 'мятежник',
    'war': 'война', 'battle': 'битва', 'peace': 'мир', 'victory': 'победа',
    'defiance': 'вызов', 'rebellion': 'восстание', 'uprising': 'восстание',
    'freedom': 'свобода', 'liberty': 'свобода', 'sorrow': 'печаль',
    'grief': 'горе', 'joy': 'радость', 'anger': 'гнев', 'rage': 'ярость',
    'fear': 'страх', 'terror': 'ужас', 'mad': 'сумасшедший',
    'insane': 'безумный', 'crazy': 'безумный', 'nasty': 'противный',
    'ugly': 'уродливый', 'safe2': 'безопасный', 'ugliness': 'уродство',
    'mysterious': 'загадочный', 'mystery': 'тайна', 'strange': 'странный',
    'odd': 'необычный', 'weird': 'странный', 'curious': 'любопытный',
    'cute': 'милый', 'pretty': 'красивый', 'beautiful': 'красивый',
    'ugly2': 'уродливый', 'handsome': 'красивый', 'lovely': 'прекрасный',
    'nice': 'приятный', 'good': 'хороший', 'bad': 'плохой', 'great2': 'отличный',
    'best': 'лучший', 'worst': 'худший', 'better': 'лучше', 'worse': 'хуже',
    'happy': 'счастливый', 'sad': 'грустный', 'angry': 'злой',
    'lonely': 'одинокий', 'loved': 'любимый', 'loved2': 'любимый',
    'hate': 'ненависть', 'love': 'любовь', 'heart': 'сердце',
    'soul2': 'душа', 'dream2': 'мечта', 'hope2': 'надежда',
    'fear2': 'страх', 'joy2': 'радость', 'peace2': 'мир',

    # ── названия игровых режимов и систем (Applied Energistics и т.п.) ──
    'me': 'ME', 'minecraft': 'Minecraft', 'forge': 'Forge',
    'neoforge': 'NeoForge', 'fabric': 'Fabric', 'quilt': 'Quilt',
    'craft': 'крафт', 'smeltery': 'плавильня', 'multiservo': 'мультисерво',
    'chamber': 'камера', 'attuned': 'настроенный', 'cable2': 'кабель',
    'interface': 'интерфейс', 'terminal': 'терминал', 'drive': 'диск',
    'p2p': 'P2P', 'quantum': 'квантовый', 'quantumlinked': 'квантовая связь',
    'me2': 'ME', 'inscriber2': 'гравировщик', 'formation': 'формация',
    'press2': 'пресс', 'reaction': 'реакция', 'reactor2': 'реактор',
    'entropy': 'энтропия', 'vibrational': 'вибрационный', 'matter': 'материя',
    'matter2': 'материя', 'quantum2': 'квантовый', 'item2': 'предмет',
    'fluids': 'жидкости', 'fluid': 'жидкость', 'gas': 'газ',
    'bacteria': 'бактерия', 'cell2': 'клетка', 'cells': 'клетки',
    'bios': 'био', 'biological': 'биологический', 'organic2': 'органический',
    'gas2': 'газ', 'molecular': 'молекулярный', 'molecule': 'молекула',
    'moleculizer': 'молекуляйзер', 'recombobulizer': 'рекомбобуляйзер',
    'recombobulator': 'рекомбобулятор', 'sifter': 'грохот',
    'duplicator': 'дубликатор', 'duper': 'дупликатор', 'void2': 'пустота',
    'ender': 'эндер', 'enderium': 'эндериум', 'endium': 'эндиум',
    'vibrant': 'вибрирующий', 'awakened': 'пробуждённый', 'matured': 'зрелый',
    'somnium': 'сомниум', 'greentech': 'зелёная техника', 'lux': 'люкс',
    'crude2': 'сырой', 'refined': 'очищенный', 'synthetic': 'синтетический',
    'stoneage': 'каменный век', 'potion': 'зелье', 'potions': 'зелья',
    'essence': 'эссенция', 'elixir': 'эликсир', 'juice2': 'сок',
    'essence2': 'эссенция', 'dust3': 'пыль', 'shard2': 'осколок',
}

# ── НЕ ПЕРЕВОДИТЬ: технические термины, значения конфигов,
#    названия технологий и слова, для которых русского аналога нет.
#    В русских сборках их принято оставлять латиницей.
KEEP = {
    'shift',
    'ctrl',
    'alt',
    'tab',
    'enter',
    'esc',
    'left',
    'right',
    'up',
    'down',
    'space',
    'click',
    'wheel',
    # логические значения — в конфигах всегда латиницей
    'true', 'false', 'null', 'none', 'yes', 'no', 'on', 'off', 'nan',
    'true.', 'false.', 'default', 'custom', 'auto', 'never', 'always',
    # технические слова интерфейса и конфигов
    'config', 'settings', 'option', 'options', 'toggle', 'checkbox',
    'screen', 'editor', 'overlay', 'level', 'culling', 'log', 'profile',
    'preset', 'key', 'value', 'input', 'output', 'menu_', 'panel_',
    'accessibility', 'fullscreen', 'vsync', 'fov', 'gui_', 'uid', 'uuid_',
    'slider', 'tab', 'button', 'label', 'field', 'default', 'keybind',
    'keybinds', 'advanced', 'experimental', 'gui', 'ui', 'hud', 'tooltip',
    'debug', 'log', 'logs', 'configurability', 'modid', 'mod_id', 'lang',
    'locale', 'save', 'load', 'import', 'export', 'reset', 'apply', 'done',
    'cancel', 'close', 'back', 'next', 'prev', 'open', 'refresh', 'search',
    'filter', 'sort', 'view', 'hide', 'show', 'toggle', 'enable', 'disable',
    'enabled', 'disabled', 'active', 'inactive', 'beta', 'alpha', 'dev',
    'experimental', 'wip', 'todo', 'unstable', 'deprecated', 'legacy_',
    # форматы, идентификаторы, единицы
    'nbt', 'uuid', 'id', 'json', 'xml', 'csv', 'txt', 'yml', 'yaml',
    'api', 'url', 'uri', 'http', 'https', 'www', 'com', 'net', 'org',
    'fps', 'tps', 'ms', 'kb', 'mb', 'gb', 'tb', 'pb', 'hz', 'khz', 'mhz',
    'ghz', 'cpu', 'gpu', 'ram', 'ssd', 'hdd', 'usb', 'hdmi', 'rgb', 'rgba',
    'xp', 'hp', 'mp', 'sp', 'npc', 'mob', 'entity', 'entities', 'blockid',
    'meta', 'metadata', 'nbt_type', 'int', 'float', 'double', 'boolean',
    'string', 'list', 'dict', 'array', 'byte', 'short', 'long', 'char',
    'ru_ru', 'en_us', 'en_us.json', 'ru_ru.json', 'translate',
    # моды, загрузчики, технологии
    'minecraft', 'forge', 'neoforge', 'fabric', 'quilt', 'optifine',
    'iris', 'sodium', 'java', 'linux', 'windows', 'macos', 'android',
    'steam', 'discord', 'github', 'patreon', 'youtube', 'twitch', 'reddit',
    'curseforge', 'modrinth', 'curseforge.com', 'modrinth.com',
    'vanilla', 'bukkit', 'spigot', 'paper', 'folia', 'velocity', 'forge_',
    # игровые режимы и системные термины
    'lan', 'pvp', 'pve', 'fps_', 'crash', 'debug_', 'tick', 'tps_',
    # команды и синтаксис
    'cmd', 'command', 'argument', 'args', 'flag', 'flags', 'permission',
    'permissions', 'whitelist', 'blacklist', 'ops', 'op', 'deop',
    # прочее без русского аналога в контексте модов
    'ctm', 'ae2', 'aot', 'aiot', 'cf', 'cp', 'esd', 'ctrl', 'npc_',
    'wip_', 'me', 'meb', 'xray', 'ore_dict', 'oredictionary', 'datapack',
    'data_pack', 'worldgen_', 'biome', 'biomes', 'dimension', 'dimensions',
    'portal', 'redstone_', 'sign_', 'xp_', 'lucky', 'unlucky', 'nether_',
    'end_', 'overworld', 'pocket', 'pockets', 'geode', 'geodes', 'copper_',
    'dripstone', 'mangrove', 'sniffer', 'camel', 'armadillo', 'breeze',
    'trial', 'vault', 'ominous', 'trial_', 'bottle_', 'wind_charge',
    'mace', 'heavy_core', 'copper_bulb', 'copper_Chest', 'crafter',
    'bamboo', 'bamboo_', 'cherry', 'piglin_', 'skulk', 'sculk', 'warden',
    'frog', 'tadpole', 'cod', 'salmon', 'tropics', 'snowy', 'stony',
}


def keep(word):
    """Нужно ли оставить слово латиницей?"""
    w = word.strip().strip('.,!?;:()"\'').lower()
    return w in KEEP or w.replace('-', '_') in KEEP




# ── добор по фактическому списку неизвестных (см. unknown_words.txt) ──
WORDS.update({
    'vertical': 'Вертикальный', 'eastern': 'Восточный', 'western': 'Западный',
    'northern': 'Северный', 'southern': 'Южный', 'concave': 'Вогнутый',
    'convexed': 'Выпуклый', 'capped': 'навершием', 'decor': 'Декор',
    'greek': 'греческий', 'plain': 'гладкий', 'gilded': 'золотой',
    'framed': 'в оправе', 'topped': 'с крышей', 'brick_topped': 'кирпичная крыша',
    'half_weathered': 'Полувыветренный', 'weathered': 'выветренный',
    'engineer_s': 'Инженера', 'traveler_s': 'Путешественника',
    'author_s': 'Автора', 'botanist_s': 'Ботаника', 'mod_s': 'Мода',
    'item_s': 'Предмета', 'meta_s': 'Метаданных', 'bat_s': 'Крыла',
    'bootytoast_s': 'БуттиТоста', 'direwolf_s': 'Дайреволфа',
    'thaumaturge_s': 'Тауматурга', 'modifier': 'Модификатор',
    'slate': 'Сланец', 'weight': 'Вес', 'click': 'Нажмите',
    'download': 'Скачать', 'changelog': 'список изменений', 'browser': 'браузере',
    'gives': 'Даёт', 'in': 'В', 'resistant': 'Устойчив', 'relayed': 'Передано',
    'zoomer': 'Зум', 'zoom': 'Зум', 'doublin': 'Удвоение', 'up': 'Вверх',
    'fluids': 'Жидкости', 'items': 'Предметы', 'tank': 'Бак', 'sneaky': 'Подкрадываясь',
    'bookworm': 'Книжный червь', 'bringer': 'Несущий', 'munchdew': 'Манчдью',
    'bzzzzrrrrt': 'Бззззрррт', 'us': 'US', 'concaved': 'Вогнутый',
    'crystallized_oil': 'Кристаллизованная нефть', 'empowered_oil': 'Усиленная нефть',
    'luminous': 'Светящийся', 'shadow': 'тень', 'gloom': 'мрак',
})

# ── Транслитерация выдуманных имён ──
# В русских сборках Minecraft такие имена принято переводить
# транслитерацией — так же, как в самой игре: Ти нделкоф, Сертус, Крип.
# Это держит перевод «внутри мира Майна», а не в мире переводчика.
_DIGRAPH = [
    ('sch', 'ш'), ('shch', 'щ'), ('tch', 'ч'), ('ch', 'ч'),
    ('sh', 'ш'), ('th', 'т'), ('ph', 'ф'), ('kh', 'х'),
    ('ck', 'к'), ('ts', 'ц'), ('dz', 'дз'), ('zh', 'ж'),
    ('ee', 'и'), ('oo', 'у'), ('ou', 'ау'), ('oa', 'оу'),
    ('ai', 'эй'), ('ay', 'ей'), ('ei', 'ей'), ('ey', 'ей'),
    ('ie', 'и'), ('gh', 'г'), ('wh', 'в'), ('ck', 'к'),
]
_MAP = {
    'a': 'а', 'b': 'б', 'c': 'к', 'd': 'д', 'e': 'е', 'f': 'ф', 'g': 'г',
    'h': 'х', 'i': 'и', 'j': 'дж', 'k': 'к', 'l': 'л', 'm': 'м', 'n': 'н',
    'o': 'о', 'p': 'п', 'q': 'к', 'r': 'р', 's': 'с', 't': 'т', 'u': 'у',
    'v': 'в', 'w': 'в', 'x': 'кс', 'y': 'й', 'z': 'з',
}


def translit(word):
    """English -> Russian транслитерация выдуманного имени.

    Zeno -> Зено,  Zoea -> Зоэа,  Kryp -> Крип,  Lave -> Лав.
    Возвращает None, если в слове нет ни одной гласной латиницы —
    значит это не имя, а аббревиатура (AIOT, CTM, AE2).
    """
    w = word.strip()
    if not w or not any(c.lower() in 'aeiouy' for c in w):
        return None
    if not w.replace("'", '').replace('-', '').isalpha():
        return None
    # Аббревиатуры и коды (AIOT, CTM, AE2, NBT) оставляем как есть
    if w.isupper():
        return None
    out, i = [], 0
    lw = w.lower()
    while i < len(lw):
        for d, ru in _DIGRAPH:
            if lw.startswith(d, i):
                out.append(ru)
                i += len(d)
                break
        else:
            c = lw[i]
            if c == 'y':
                # y после согласной читается как «и» (Kryp -> Крип),
                # после гласной и в конце — как «й» (Boy -> Бой)
                prev = lw[i - 1] if i else ''
                out.append('й' if (i == len(lw) - 1 or prev in 'aeiou') else 'и')
                i += 1
                continue
            ru = _MAP.get(c)
            if ru is None:
                if out:
                    return None
                i += 1
                continue
            out.append(ru)
            i += 1
    s = ''.join(out)
    return s[0].upper() + s[1:] if s else None



def _expand_punct(d):
    """Добавляет варианты ключей без прилипшей пунктуации.

    'engineer\'s' -> 'engineer', 'weight:' -> 'weight', 'modifier:' -> 'modifier'.
    Нужно, потому что в lang-файлах знаки часто приклеены к слову.
    """
    extra = {}
    for k, v in d.items():
        b = k.strip('.,:;!?()[]"\'')
        if b and b != k and b not in d:
            extra[b] = v
    d.update(extra)
    return d


def _stripped_variants(s):
    """Для множества (KEEP) — варианты без пунктуации."""
    out = set()
    for k in s:
        b = k.strip('.,:;!?()[]"\'')
        if b and b != k:
            out.add(b)
    return out


_expand_punct(WORDS)
KEEP.update(_stripped_variants(KEEP))

# притяжательные с апострофом — ключи должны совпадать с lang-файлом
WORDS.update({
    "engineer's": 'Инженера', "traveler's": 'Путешественника',
    "author's": 'Автора', "botanist's": 'Ботаника', "mod's": 'Мода',
    "item's": 'Предмета', "meta's": 'Метаданных', "bat's": 'Крыла',
    "bootytoast's": 'БуттиТоста', "direwolf's": 'Дайреволфа',
    "thaumaturge's": 'Тауматурга', "player's": 'Игрока', "farmer's": 'Фермера',
    "reactor's": 'Реактора', "operator's": 'Оператора', "admin's": 'Админа',
    "o'": 'О', "hammer's": 'Молота', "smith's": 'Кузнеца',
})

# слова, для которых есть нормальный русский эквивалент —
# транслитерация здесь неуместна
WORDS.update({
    'goggles': 'Очки', 'sack': 'Сумка', 'unlocalized': 'Нелокализованное',
    'unlocalised': 'Нелокализованное', 'invisibility': 'Невидимость',
    'speed': 'Скорость', 'augment': 'Аугмент', 'range': 'Дальность',
    'reconstructor': 'Реконструктор', 'empowerer': 'Усилитель',
    'feeder': 'Кормушка', 'enerminator': 'Разрядитель', 'enervator': 'Разрядитель',
    'energizer': 'Энергизатор', 'solidifier': 'Опытозатвердитель',
    'solidifer': 'Опытозатвердитель', 'distributor': 'Распределитель',
    'interface': 'Интерфейс', 'redstoneface': 'Редстоунлицо',
    'liquiface': 'Лицо Жидкости', 'energyface': 'Лицо Энергии',
    'phantomface': 'Фантомлицо', 'hud': 'HUD', 'bucket': 'Ведро',
    'playercrate': 'Ящик игрока', 'upgrade': 'Улучшение', 'blockz': 'BlockZ',
    'hovercraft': 'Ховеркрафт', 'cannon': 'Пушка', 'chest': 'Сундук',
    'pouch': 'Подсумок', 'backpack': 'Рюкзак', 'toolkit': 'Набор',
    'camouflage': 'Камуфляж', 'camo': 'Камуфляж', 'scope': 'Прицел',
    'magazine': 'Магазин', 'stock': 'Ложа', 'barrel2': 'Ствол',
    'trigger': 'Спусковой крючок', 'bipod': 'Сошки', 'silencer': 'Глушитель',
    'laser': 'Лазер', 'wrecking': 'Тяжёлый', 'atomic': 'Атомарный',
})

# составные узоры целиком — чтобы не разваливались на части
WORDS.update({
    'greek-capped': 'греческий навершием', 'plain-capped': 'гладкий навершием',
    'decor-capped': 'декор навершием', 'convexed-capped': 'выпуклый навершием',
    'brick-topped': 'кирпичная крыша', 'gold-framed': 'золотая оправа',
    'half-weathered': 'полувыветренный', 'small-concaved': 'малый вогнутый',
    'zeon,': 'Зеон,', 'zeno,': 'Зено,', 'zoea,': 'Зоэа,', 'zome,': 'Зоме,',
    'korp,': 'Корп,', 'luxe,': 'Луксе,', 'mint,': 'Мята,',
    'i': 'I', 'ii': 'II', 'iii': 'III', 'iv': 'IV', 'v': 'V',
})
# римские цифры, единицы и служебные знаки — латиницей
KEEP.update({'i', 'ii', 'iii', 'iv', 'v', 'vi', 'vii', 'viii', 'ix', 'x',
             'iii!)', 'cf/t', 'fe/t', "o'", 'us', 'ie', 'ii)'})
