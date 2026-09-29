# -*- coding: utf-8 -*-
"""Ручные исправления партий 1 и 3. Проверено против ОРИГИНАЛЬНОГО en_us из модпака.

Здесь не калька, а нормальный русский: терминология — по ванильному ru_ru.json
(«Печь», «Плита», «Ступенька», «Редстоун», «Опыт»), бренды мода оставлены как есть
(Allthemodium, Vibranium, Unobtainium — транслитерация по правилам мода).
"""

FIX = {}

# ─────────────────────────── Iron Furnaces ───────────────────────────
# Термины: Augment → «Улучшение», Blasting → «Переплавка», Smoking → «Копчение»,
# Cooktime → «Время переплавки». Тиры печей — по материалу.
_TIER = {
    'stone': 'каменная', 'copper': 'медная', 'iron': 'железная', 'silver': 'серебряная',
    'gold': 'золотая', 'diamond': 'алмазная', 'emerald': 'изумрудная',
    'crystal': 'кристальная', 'obsidian': 'обсидиановая', 'netherite': 'незеритовая',
    'allthemodium': 'аллфемиумная', 'vibranium': 'вибраниумная',
    'unobtainium': 'унобтаниумная',
}
# English tier word -> русский порядок слов для «X to Y Furnace Upgrade»
_EN_TIER = dict(zip(_TIER, ['каменная', 'медная', 'железная', 'серебряная', 'золотая',
                            'алмазная', 'изумрудная', 'кристальная', 'обсидиановая',
                            'незеритовая', 'аллфемиумная', 'вибраниумная', 'унобтаниумная']))
_EN2TIER = {k: v for k, v in [
    ('Stone', 'каменная'), ('Copper', 'медная'), ('Iron', 'железная'),
    ('Silver', 'серебряная'), ('Gold', 'золотая'), ('Diamond', 'алмазная'),
    ('Emerald', 'изумрудная'), ('Crystal', 'кристальная'),
    ('Obsidian', 'обсидиановая'), ('Netherite', 'незеритовая'),
    ('Allthemodium', 'аллфемиумная'), ('Vibranium', 'вибраниумная'),
    ('Unobtainium', 'унобтаниумная')]}


def _ironfurnaces():
    d = {}
    d['itemGroup.ironfurnaces'] = 'Железные печи'
    # ключ '_comment' без namespace нельзя класть в общий словарь:
    # он протекает в любой мод, где есть такой ключ (Psi, ironchest, …)
    for key, ru in [
        ('iron', 'Железная печь'), ('gold', 'Золотая печь'),
        ('diamond', 'Алмазная печь'), ('emerald', 'Изумрудная печь'),
        ('obsidian', 'Обсидиановая печь'), ('crystal', 'Кристальная печь'),
        ('netherite', 'Незеритовая печь'), ('copper', 'Медная печь'),
        ('silver', 'Серебряная печь'), ('allthemodium', 'Печь из аллфемиума'),
        ('vibranium', 'Печь из вибраниума'), ('unobtainium', 'Печь из унобтаниума'),
    ]:
        d[f'block.ironfurnaces.{key}_furnace'] = ru
        d[f'container.ironfurnaces.{key}_furnace'] = ru
    d['block.ironfurnaces.million_furnace'] = 'Радужная печь'
    d['container.ironfurnaces.million_furnace'] = 'Радужная печь'
    d['block.ironfurnaces.heater'] = 'Беспроводной передатчик тепла'
    d['container.ironfurnaces.wireless_energy_heater'] = 'Беспроводной нагреватель'
    d['item.ironfurnaces.item_heater'] = 'Беспроводной приёмник тепла'

    # «X to Y Furnace Upgrade» — сохраняем порядок «из чего → во что»
    up = {
        'iron': ('Stone', 'Iron'), 'gold': ('Iron', 'Gold'),
        'diamond': ('Gold', 'Diamond'), 'emerald': ('Diamond', 'Emerald'),
        'obsidian': ('Emerald', 'Obsidian'), 'crystal': ('Diamond', 'Crystal'),
        'obsidian2': ('Crystal', 'Obsidian'), 'netherite': ('Obsidian', 'Netherite'),
        'copper': ('Stone', 'Copper'), 'silver': ('Copper', 'Silver'),
        'silver2': ('Iron', 'Silver'), 'iron2': ('Copper', 'Iron'),
        'gold2': ('Silver', 'Gold'),
        'allthemodium': ('Netherite', 'Allthemodium'),
        'vibranium': ('Allthemodium', 'Vibranium'),
        'unobtainium': ('Vibranium', 'Unobtainium'),
    }
    for k, (a, b) in up.items():
        d[f'item.ironfurnaces.upgrade_{k}'] = (
            f'Улучшение печи: {_EN2TIER[a]} → {_EN2TIER[b]}')

    d['item.ironfurnaces.augment_speed'] = 'Улучшение: скорость'
    d['item.ironfurnaces.augment_fuel'] = 'Улучшение: эффективность топлива'
    d['item.ironfurnaces.augment_factory'] = 'Улучшение: завод'
    d['item.ironfurnaces.augment_generator'] = 'Улучшение: генератор'
    d['item.ironfurnaces.augment_smoking'] = 'Улучшение: копчение'
    d['item.ironfurnaces.augment_blasting'] = 'Улучшение: переплавка'
    d['item.ironfurnaces.gui'] = 'Улучшение: настройка инвентаря'
    d['item.ironfurnaces.item_spooky'] = 'Печь-спукалатор'
    d['item.ironfurnaces.item_xmas'] = 'Упаковочная бумага для печи'
    d['item.ironfurnaces.item_copy'] = 'Линкер настроек печи'
    d['item.ironfurnaces.rainbow_core'] = 'Радужное ядро'
    d['item.ironfurnaces.rainbow_plating'] = 'Радужная обшивка'
    d['item.ironfurnaces.rainbow_coal'] = 'Радужный уголь'

    d['ironfurnaces.jei_category_regular'] = 'Генератор | обычная'
    d['ironfurnaces.jei_category_blasting'] = 'Генератор | переплавка'
    d['ironfurnaces.jei_category_smoking'] = 'Генератор | копчение'

    g = {
        'cooktime': 'Время переплавки',
        'gui_pro': '+Добавляет настраиваемые стороны для передачи предметов.',
        'gui_open_furnace': 'Печь', 'gui_open': 'Открыть настройки',
        'gui_close': 'Закрыть настройки', 'gui_auto_input': 'Автоподача',
        'gui_auto_output': 'Автовывод', 'gui_top': 'Верх', 'gui_bottom': 'Низ',
        'gui_front': 'Перед', 'gui_back': 'Зад', 'gui_left': 'Левая',
        'gui_right': 'Правая', 'gui_none': 'Нет', 'gui_input': 'Вход',
        'gui_output': 'Выход', 'gui_input_output': 'Вход/Выход',
        'gui_fuel': 'Вход топлива/Выброс', 'gui_reset': 'Сбросить всё',
        'gui_redstone_ignored': 'Игнорировать редстоун',
        'gui_redstone_low': 'Низкий сигнал',
        'gui_redstone_high': 'Высокий сигнал',
        'gui_redstone_comparator': 'Компаратор',
        'gui_redstone_comparator_sub': 'Компаратор с вычитанием',
        'gui_hold_shift': 'Удерживайте ', 'gui_shift_more_options':
        ' для дополнительных настроек',
        'hold': 'Удерживайте ',
        'upgrade_right_click': 'Shift+ПКМ для улучшения',
        'xmas_right_click': 'ПКМ по печи, чтобы упаковать её как подарок!',
        'xmas1': 'Но ведь упаковка не загорится?',
        'xmas2': 'Shift+ПКМ по печи открывает подарок',
        'spooky_right_click': 'ПКМ по печи, чтобы засупооить её',
        'spooky1': '3spooky5me',
        'spooky2': 'Shift+ПКМ по печи снимает все супооения',
        'heater': 'Привязан к: ', 'heaterX': 'X: ', 'heaterY': 'Y: ', 'heaterZ': 'Z: ',
        'heater_not_bound': 'Ещё не привязан к источнику энергии!',
        'heater_tip': 'Поместите в слот топлива печи',
        'heater_tip1': 'Работает только начиная с железного тира и выше',
        'heater_block': 'Свяжите с беспроводным приёмником тепла '
                        '(связывание — приёмник ставится в передатчик тепла)',
        'heater_block1': 'Подайте на этот блок Forge Energy, и он будет питать все '
                         'связанные беспроводные приёмники тепла',
        'augment_right_click': 'ПКМ, чтобы вставить улучшение',
        'augment_slot': 'Слот улучшения',
        'augment_speed_pro': '+Вдвое сокращает время переплавки всех рецептов.',
        'augment_speed_con': '−Расходует вдвое больше топлива.',
        'augment_smoking': 'Подходит только для рецептов копчения.',
        'augment_blasting': 'Подходит только для рецептов переплавки.',
        'augment_xp': 'Опыт, полученный за переплавку, сливается в '
                      'ближайшие резервуары с жидкостью.',
        'augment_xp_1': 'Опыт, полученный за переплавку, выдаётся игроку, '
                        'поставившему печь.',
        'augment_xp_switch': 'Shift+ПКМ по воздуху переключает режимы.',
        'augment_fuel_pro': '+Топливо нагревает печь вдвое сильнее.',
        'augment_fuel_con': '−Замедляет переплавку всех рецептов на 25 %.',
        'augment_factory_pro': '+Превращает печь в завод.',
        'augment_factory_con': '−Использует энергию вместо топлива.',
        'augment_generator_pro': '+Жар печи вырабатывает энергию.',
        'augment_generator_con': '−Печь больше не может переплавлять предметы.',
        'augment_redstone_right_click': 'ПКМ по воздуху меняет режим',
        'augment_redstone_control': 'Режим управления редстоуном: печь НЕ переплавляет '
                                    'при подаче редстоун-сигнала',
        'augment_redstone_control_inverse': 'Обратный режим управления редстоуном: '
                                            'печь переплавляет ТОЛЬКО при подаче '
                                            'редстоун-сигнала',
        'augment_redstone_comparator': 'Режим компаратора: работает как встроенный '
                                       'компаратор и выдаёт сигнал со всех сторон',
        'augment_redstone_comparator_subtract': 'Режим компаратора с вычитанием: как '
                                                 'режим компаратора, но сигнал можно '
                                                 'вычитать; сила вычитания настраивается '
                                                 'в интерфейсе печи',
        'augment_redstone_msg_mode0': 'Режим управления редстоуном',
        'augment_redstone_msg_mode1': 'Обратный режим управления редстоуном',
        'augment_redstone_msg_mode2': 'Режим компаратора',
        'augment_redstone_msg_mode3': 'Режим компаратора с вычитанием',
        'gui_open_augments': 'Улучшения',
        'for_details': ' для подробностей.',
        'rainbow_blowup': 'Что-то подсказывает мне взорвать её...',
        'rainbow_gen1': 'Будет вырабатывать',
        'rainbow_gen2': 'RF/тик, если все остальные печи тоже вырабатывают энергию',
        'rainbow_gen3': 'Для выработки энергии одна печь каждого тира должна быть '
                        'активным генератором и не иметь полный буфер',
        'rainbow_gen4': 'Отдаёт энергию в любую сторону, настроенную на выход, '
                        'не пополняя внутренний буфер',
        'rainbow_gen5': 'Радужный генератор может быть только один',
    }
    for k, v in g.items():
        d[f'tooltip.ironfurnaces.{k}'] = v
    d['ironfurnaces.update.buttonOptions'] = \
        'ПКМ: список изменений, Shift+ПКМ: скачать! (в браузере)'
    d['ironfurnaces.update.speech'] = (
        '[{"text":"Доступно обновление для "},'
        '{"text":"Железных печей ","color":"dark_green"},'
        '{"text":"!","color":"none"}]')
    d['ironfurnaces.update.version'] = (
        '[{"text":"Текущая версия: "},{"text":"%s","color":"dark_red"},'
        '{"text":", новая версия: ","color":"none"},'
        '{"text":"%s","color":"dark_green"}]')
    d['ironfurnaces.update.buttons'] = (
        '[{"text":"["},{"text":"ПКМ — список изменений","color":"green",'
        '"clickEvent":{"action":"open_url","value":"%s"}},'
        '{"text":"] [","color":"none"},'
        '{"text":"ПКМ — скачать","color":"green",'
        '"clickEvent":{"action":"open_url","value":"%s"}},'
        '{"text":"]","color":"none"}]')
    d['ironfurnaces.update.failed'] = (
        '[{"text":"Проверка обновления "},'
        '{"text":"Железных печей ","color":"dark_green"},'
        '{"text":"не удалась! Подробности в логе.","color":"red"}]')
    return d


FIX.update(_ironfurnaces())


# ─────────────────────── Mob Grinding Utils ───────────────────────
# Термины: Mob Fan → «Вентилятор для мобов», Mob Masher → «Мясорубка для мобов»,
# Entity Spawner → «Спавнер сущностей», Swab → «Мазок», Looting → «Везение» (ванилль).
FIX.update({
 'itemGroup.mob_grinding_utils': 'Мобильные фермы',
 'block.mob_grinding_utils.fan': 'Вентилятор для мобов',
 'block.mob_grinding_utils.saw': 'Мясорубка для мобов',
 'fakeplayer.mob_masher': 'Мясорубка для мобов',
 'block.mob_grinding_utils.entity_conveyor': 'Конвейер для сущностей',
 'block.mob_grinding_utils.entity_spawner': 'Спавнер сущностей',
 'item.mob_grinding_utils.mob_swab': 'Мазок',
 'item.mob_grinding_utils.mob_swab_used': 'Использованный мазок',
 'item.mob_grinding_utils.fan_upgrade_width': 'Улучшение вентилятора: ширина',
 'item.mob_grinding_utils.fan_upgrade_height': 'Улучшение вентилятора: высота',
 'item.mob_grinding_utils.fan_upgrade_speed': 'Улучшение вентилятора: дальность',
 'item.mob_grinding_utils.spawner_upgrade_width': 'Улучшение спавнера: ширина',
 'item.mob_grinding_utils.spawner_upgrade_height': 'Улучшение спавнера: высота',
 'item.mob_grinding_utils.saw_upgrade_sharpness': 'Улучшение мясорубки: острота',
 'item.mob_grinding_utils.saw_upgrade_looting': 'Улучшение мясорубки: везение',
 'item.mob_grinding_utils.saw_upgrade_fire': 'Улучшение мясорубки: огонь',
 'item.mob_grinding_utils.saw_upgrade_smite': 'Улучшение мясорубки: кара',
 'item.mob_grinding_utils.saw_upgrade_arthropod': 'Улучшение мясорубки: членистоногие',
 'item.mob_grinding_utils.saw_upgrade_beheading': 'Улучшение мясорубки: обезглавливание',
 'block.mob_grinding_utils.fan.tooltip_1': 'Отталкивает мобов в направлении взгляда.',
 'block.mob_grinding_utils.fan.tooltip_2': 'Можно улучшить улучшениями вентилятора.',
 'block.mob_grinding_utils.fan.tooltip_3': 'Область действия переключается в интерфейсе.',
 'block.mob_grinding_utils.saw.tooltip_1': 'Продвинутый модульный измельчитель мобов.',
 'block.mob_grinding_utils.saw.tooltip_2': 'Можно улучшить улучшениями мясорубки.',
 'block.mob_grinding_utils.spikes.tooltip_1': 'Простые шипы — также роняют опыт с мобов.',
 'block.mob_grinding_utils.entity_spawner.tooltip_2': 'В верхний слот кладётся яйцо моба.',
 'block.mob_grinding_utils.entity_spawner.tooltip_5': 'Область действия переключается в интерфейсе.',
 'block.mob_grinding_utils.entity_spawner.tooltip_6': 'Позицию можно сместить в интерфейсе.',
 'block.mob_grinding_utils.absorption_hopper.tooltip_2': 'Можно настроить выдачу в любую сторону.',
 'block.mob_grinding_utils.absorption_hopper.tooltip_3': 'Жидкости вставляются в баки, предметы — в инвентари.',
 'block.mob_grinding_utils.absorption_hopper.tooltip_5': 'Область действия переключается в интерфейсе.',
 'block.mob_grinding_utils.absorption_hopper.tooltip_6': 'Позицию можно сместить в интерфейсе.',
 'block.mob_grinding_utils.ender_inhibitor_off.tooltip_1': 'Запрещает телепортацию сущностей в радиусе 8 блоков.',
 'block.mob_grinding_utils.ender_inhibitor_off.tooltip_2': 'Можно установить на ЛЮБУю сторону твёрдого блока.',
 'block.mob_grinding_utils.ender_inhibitor_off.tooltip_3': 'ПКМ — отключить/включить.',
 'block.mob_grinding_utils.ender_inhibitor_on.tooltip_1': 'Запрещает телепортацию сущностей в радиусе 8 блоков.',
 'block.mob_grinding_utils.ender_inhibitor_on.tooltip_2': 'Можно установить на ЛЮБУЮ сторону твёрдого блока.',
 'block.mob_grinding_utils.ender_inhibitor_on.tooltip_3': 'ПКМ — отключить/включить.',
 'block.mob_grinding_utils.delightful_dirt.tooltip_1': 'Увеличивает число появления мирных мобов.',
 'block.mob_grinding_utils.delightful_dirt.tooltip_3': 'Уровень света ниже 10 останавливает появление мирных мобов.',
 'block.mob_grinding_utils.dreadful_dirt.tooltip_1': 'Увеличивает число появления враждебных мобов.',
 'block.mob_grinding_utils.dreadful_dirt.tooltip_3': 'Уровень света 5 и выше останавливает появление враждебных мобов.',
 'block.mob_grinding_utils.jumbo_tank.jei.info': 'Увеличенная версия резервуара сингулярности: вмещает 1024 ведра любой жидкости.',
 'block.mob_grinding_utils.xpsolidifier.jei.info': 'Превращает опыт из жидкости в съедобное желе с помощью форм.',
 'block.mob_grinding_utils.tinted_glass.jei.info': 'Позволяет видеть содержимое мобильной фермы, не пропуская внутрь свет.',
 'block.mob_grinding_utils.delightful_dirt.jei.info': 'Не работает в океанских и прочих биомах, где не появляются мирные мобы.',
 'tooltip.mobswab_1': "Примените к мобу, чтобы собрать «ДНК».",
 'tooltip.chickenfeed_2': 'Одноразовое применение к курице...',
 'tooltip.solid_xp2': 'Всего опыта в стопке: ',
 'mob_grinding_utils.jei.any_experience': 'Подходит для любой жидкости: forge:experience',
})


# ─────────────── Mekanism Ponders (гайды реакторов) ───────────────
# Термины Mekanism: Fission Reactor → «Реактор деления», Fusion Reactor → «Реактор
# синтеза», Chemicals → «Химикаты», Coolant → «Хладагент», Hohlraum → «Хольраум».
FIX.update({
 'mekanism_ponders.ponder.creating_fission_reactor.text_1':
   'Реактор деления — многоблочная конструкция, перерабатывающая делящееся топливо в ядерные отходы.',
 'mekanism_ponders.ponder.creating_fission_reactor.text_2':
   'Это кубоидальная конструкция размером от 3x4x3 до 18x18x18; каркас собирается из оболочек реактора деления.',
 'mekanism_ponders.ponder.creating_fission_reactor.text_3':
   'Внутренние секции каркаса можно заменить стеклом реактора, портами реактора деления или логическими адаптерами.',
 'mekanism_ponders.ponder.creating_fission_reactor.text_5':
   'Охлаждение реактора ухудшается, если стержни управления соприкасаются друг с другом.',
 'mekanism_ponders.ponder.cooling_fission_reactor.text_1':
   'Реактор деления можно охлаждать двумя способами.',
 'mekanism_ponders.ponder.cooling_fission_reactor.text_2':
   '1. Водяное охлаждение\n\nВодяное охлаждение реактора даёт пар. На каждую мБ/т скорости сжигания реактор расходует 20 000 мБ/т воды.\n\nПар можно использовать в турбине для получения энергии, а также перерабатывать обратно в воду насыщающими конденсаторами.',
 'mekanism_ponders.ponder.cooling_fission_reactor.text_4':
   'Этот способ расходует 200 000 мБ/т натрия на каждую мБ/т скорости сжигания, зато даёт вдвое большую охлаждающую способность, чем вода.\n\nЭто позволяет реактору работать на удвоенной скорости сжигания.',
 'mekanism_ponders.ponder.running_fission_reactor.text_3':
   'Работая, реактор выделяет тепло и расходует хладагент для охлаждения, попутно производя пар или перегретый натрий.',
 'mekanism_ponders.ponder.running_fission_reactor.text_5':
   "После 100%% каждый тик есть шанс взрыва реактора, пока он выше 100%%.",
 'mekanism_ponders.ponder.configuring_fission_reactor.text_10':
   'У реактора деления есть интерфейс: он открывается кликом по любому месту готовой конструкции.',
 'mekanism_ponders.ponder.configuring_fission_reactor.text_13':
   'На вкладке «Статистика реактора деления» видно:\n- статистику тепла\n- статистику топлива\n- а также позволяет задать текущую скорость сжигания.',
 'mekanism_ponders.ponder.configuring_fission_reactor.text_3':
   'Чтобы переключать режимы, возьмите конфигуратор и Shift+ПКМ по порту.',
 'mekanism_ponders.ponder.configuring_fission_reactor.text_4':
   'Только вход:\n\nПорт будет принимать исключительно жидкости и химикаты.',
 'mekanism_ponders.ponder.configuring_fission_reactor.text_7':
   'Логический адаптер реактора деления управляет реактором редстоун-сигналом\nили, наоборот, выдаёт сигнал по состоянию реактора.\n\nЭто помогает создавать аварийные защитные схемы.',
 'mekanism_ponders.ponder.configuring_fission_reactor.text_9':
   'Логический адаптер переведён в режим «Недостаточно топлива».\n\nПодаётся редстоун-сигнал.',
 'mekanism_ponders.ponder.constructing_fusion_reactor.text_1':
   'Это основа реактора синтеза.\nОна собрана из рам реактора синтеза.',
 'mekanism_ponders.ponder.configuring_fusion_reactor.text_1':
   'Режим выдачи порта реактора синтеза настраивается конфигуратором.\n\nТак порт переключается между режимами §4выдача§r и §aприём§r.',
 'mekanism_ponders.ponder.configuring_fusion_reactor.text_2':
   'В режиме §aприёма§r реактор принимает §lхимикаты§r, §lжидкости§r и §lтепло§r.',
 'mekanism_ponders.ponder.configuring_fusion_reactor.text_3':
   'В режиме §4выдачи§r реактор выдаёт §lхимикаты§r, §lтепло§r и §lэнергию§r.',
 'mekanism_ponders.ponder.configuring_fusion_reactor.text_4':
   'Логический порт реактора синтеза умеет выдавать редстоун-сигнал по состоянию реактора.',
 'mekanism_ponders.ponder.configuring_fusion_reactor.text_7':
   'Кроме того, он показывает разную статистику и имеет слот для §lхольраума§r.',
 'mekanism_ponders.ponder.starting_fusion_reactor.text_1':
   'Чтобы запустить реактор, в контроллер реактора синтеза должен находиться §lхольраум§r, заполненный §5ДТ-топливом§r.',
 'mekanism_ponders.ponder.starting_fusion_reactor.text_2':
   'Реактор запустится, достигнув температуры воспламенения.\n\nЭту температуру можно получить с помощью §lматрицы фокусировки лазера§r.',
 'mekanism_ponders.ponder.starting_fusion_reactor.text_3':
   'Матрица фокусировки лазера поглощает лазерные лучи и повышает температуру реактора.',
 'mekanism_ponders.ponder.starting_fusion_reactor.text_6':
   'Если переключить обнаружение редстоуна в «ОБЫЧНЫЙ», лазерный усилитель будет выстреливать накопленной энергией только по получении редстоун-сигнала.',
 'mekanism_ponders.ponder.starting_fusion_reactor.text_8':
   'Реактор запущен.',
 'mekanism_ponders.ponder.fueling_fusion_reactor.text_2':
   '1.\nС помощью §cдейтерия§r и §aтрития§r реактор может достичь суммарной максимальной скорости подачи §698§rмБ/т.\nТребуется по §649§rмБ/т каждого химиката.',
 'mekanism_ponders.ponder.fueling_fusion_reactor.text_4':
   'Однако при использовании §5ДТ-топлива§r реактор игнорирует заданную скорость подачи и всегда пытается расходовать §61000§rмБ/т.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_1':
   'Термоэлектрический котёл — многоблочная конструкция, производящая большое количество пара.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_2':
   'Он может кипятить воду в пар либо охлаждать перегретый натрий обратно до натрия, расходуя воду и попутно производя пар.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_7':
   'Слой с элементами перегрева должен отделяться слоем из распределителей давления.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_11':
   'Экран статистики котла показывает разные сведения о термоэлектрическом котле, включая его производительность по пару.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_12':
   'У клапана котла есть 3 конфигурации; переключаются они конфигуратором на самом клапане.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_13':
   '1. Только вход\n\nКлапан принимает только жидкости, химикаты и тепло.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_14':
   '2. Выдача пара\n\nКлапан выдаёт только пар.',
 'mekanism_ponders.ponder.creating_thermoelectric_boiler.text_15':
   '3. Выдача хладагента\n\nКлапан выдаёт только хладагент из своего внутреннего бака.',
 'mekanism_ponders.ponder.creating_dynamic_tank.text_1':
   'Динамический бак — многоблочная конструкция для хранения большого количества жидкостей или химикатов.',
 'mekanism_ponders.ponder.creating_dynamic_tank.text_7':
   'Чем больше конструкция, тем выше её ёмкость.\nЁмкости различаются для жидкостей и химикатов: у химикатов они больше.',
 'mekanism_ponders.ponder.creating_dynamic_tank.text_8':
   'ПКМ в любом месте динамического бака открывает его интерфейс, где видно общее количество хранимой жидкости или химиката и полную ёмкость бака.',
 'mekanism_ponders.ponder.creating_dynamic_tank.text_9':
   'Кроме того, там можно наполнять ёмкости из интерфейса и настроить, должен ли бак только наполняться, только сливаться или делать и то и другое.',
 'mekanism_ponders.ponder.creating_dynamic_tank.text_10':
   'Для вставки и извлечения жидкостей или химикатов используется динамический клапан.',
 'mekanism_ponders.ponder.creating_dynamic_tank.text_12':
   'Чтобы извлекать жидкости или химикаты, передатчик нужно настроить на забор из клапана.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_1':
   'Индукционная матрица — многоблочная конструкция для хранения больших объёмов энергии.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_3':
   'Это кубоидальная конструкция размером от 3x3x3 до 18x18x18',
 'mekanism_ponders.ponder.creating_induction_matrix.text_6':
   'Чтобы индукционная матрица накапливала энергию, ей нужна индукционная ячейка любого тира.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_8':
   'Для вставки и извлечения энергии из индукционной матрицы нужен индукционный поставщик.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_10':
   'У индукционной матрицы есть интерфейс: он открывается кликом по любому месту готовой конструкции.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_11':
   'В интерфейсе видны §6текущий запас энергии§r, §6полная ёмкость§r, а также количество энергии, §6подаваемой§r и §6выдаваемой§r.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_12':
   'Режим передачи индукционного порта настраивается конфигуратором.\n\nТак порт переключается между режимами §4выдача§r и §aприём§r.',
 'mekanism_ponders.ponder.creating_induction_matrix.text_13':
   'В режиме §aприёма§r он способен получать энергию из любого источника.',
 'mekanism_ponders.ponder.creating_industrial_turbine.text_1':
   'Промышленная турбина — многоблочная конструкция, вырабатывающая большое количество энергии из пара.',
 'mekanism_ponders.ponder.creating_industrial_turbine.text_4':
   'Турбине нужны роторы турбины в центральном столбе конструкции, а сверху — вращающийся узел.',
 'mekanism_ponders.ponder.creating_industrial_turbine.text_5':
   'На роторы турбины нужны лопасти турбины: их ставят ПКМ, но они не должны превышать внутреннюю ширину конструкции.',
 'mekanism_ponders.ponder.creating_industrial_turbine.text_6':
   'Внутренний слой вращающегося узла должен быть заполнен распределителями давления.',
 'mekanism_ponders.ponder.creating_industrial_turbine.text_7':
   'Электромагнитные катушки ставятся поверх вращающегося узла и должны соединяться друг с другом.\n\nКаждая катушка позволяет добавить 4 лопасти турбины.',
 'mekanism_ponders.ponder.creating_industrial_turbine.text_9':
   'Внутреннюю конструкцию начиная со слоя вращающегося узла можно заменять отводами турбины — это повышает расход пара.',
 'mekanism_ponders.ponder.running_industrial_turbine.text_1':
   'Промышленной турбине нужен пар для выработки энергии.',
 'mekanism_ponders.ponder.running_industrial_turbine.text_5':
   'Главное меню показывает:\n- внутренний бак с паром\n- выработку энергии\n- количество пара, подаваемое каждый тик\n- полную ёмкость по пару\n- максимальную скорость переработки пара.',
 'mekanism_ponders.ponder.running_industrial_turbine.text_8':
   'Меню статистики турбины показывает максимальную выработку энергии, выдачу воды и статистику конструкции.',
 'mekanism_ponders.ponder.creating_sps.text_1':
   'Сверхкритический фазовый сдвигатель (СПС) — многоблочная конструкция, превращающая полоний в антиматерию за счёт большого количества энергии.',
 'mekanism_ponders.ponder.creating_sps.text_3':
   'Внутренние секции СПС можно заменить конструкционным стеклом, стеклом реактора или портами СПС.',
 'mekanism_ponders.ponder.creating_sps.text_5':
   'Для переработки полония СПС нужен порт в центре одной из сторон, а на внутренней стороне порта — усиленная катушка.',
 'mekanism_ponders.ponder.creating_sps.text_6':
   'Каждый порт принимает до 400 MFE/т, что даёт 1 мБ антиматерии в тик;\nСПС в целом ограничен 2 мБ антиматерии в тик.',
 'mekanism_ponders.ponder.creating_sps.text_7':
   'СПС всё равно израсходует всю поданную энергию, даже если ему не хватает полония для максимальной скорости переработки антиматерии.',
 'mekanism_ponders.ponder.creating_sps.text_8':
   'Порты СПС используются для подачи энергии и полония.',
 'mekanism_ponders.ponder.creating_sps.text_9':
   'Чтобы извлекать антиматерию, режим порта СПС нужно переключить на «Выдачу».',
 'mekanism_ponders.ponder.creating_sps.text_10':
   'ПКМ в любом месте СПС открывает его интерфейс, где видно:\n- состояние СПС\n- ввод энергии,\n- скорость переработки,\n- прогресс,\n- а также два внутренних бака с полонием и антиматерией.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_1':
   'Термическая выпарная установка — многоблочная конструкция, превращающая §6воду в рассол§r и §6рассол в§r §6литий§r.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_6':
   'Для работы термической выпарной установке нужен §6нагрев§r, причём больше тепла ещё и §6ускоряет§r §6переработку§r.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_7':
   'Тепло можно подавать снаружи через клапаны термического выпаривания.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_8':
   'Чем §6крупнее§r многоблочная конструкция, тем больше §6ёмкость внутренних§r §6баков§r и §6максимальная скорость§r §6переработки§r.\nПри этом растёт и требуемый нагрев.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_9':
   'Любой передатчик жидкости можно подключить к клапану термического выпаривания для подачи жидкости.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_10':
   'Чтобы извлекать жидкость, передатчик нужно настроить на забор из клапана термического выпаривания.',
 'mekanism_ponders.ponder.creating_thermal_evaporation_plant.text_12':
   'Кроме того, там есть слоты для ёмкостей с жидкостью — ведер или баков, — чтобы забирать жидкость из внутренних баков.',
})


# ──────────────────────── BrandonsCore ────────────────────────
FIX.update({
 'item_info.brandonscore.shift_for_details': 'Удерживайте %sShift%s для подробностей',
 'mod_gui.brandonscore.entity_filter': 'Фильтр сущностей',
 'mod_gui.brandonscore.entity_filter.and_group.false': 'Сущность принимается, если совпадает с ЛЮБЫМ из фильтров этой группы.',
 'mod_gui.brandonscore.entity_filter.and_group.true': 'Сущность принимается, если совпадает со ВСЕМИ фильтрами этой группы.',
 'mod_gui.brandonscore.entity_filter.entity_type': 'Фильтр по типу сущности',
 'mod_gui.brandonscore.entity_filter.entity_type.false': 'Исключить сущность',
 'mod_gui.brandonscore.entity_type.false': 'Исключить сущность',
 'mod_gui.brandonscore.entity_filter.entity_type.find': 'Выбрать из списка сущностей.',
 'mod_gui.brandonscore.entity_filter.entity_type.suggestion': '(Введите здесь имя сущности из реестра)',
 'mod_gui.brandonscore.entity_filter.entity_type.true': 'Включить сущность',
 'mod_gui.brandonscore.entity_filter.entity_type.unknown': 'Неизвестное или неверное имя сущности',
 'mod_gui.brandonscore.entity_filter.item.fuzzy.info': 'Неточное совпадение игнорирует прочность и NBT.\nТочное совпадение распознаёт только полностью идентичный стак предметов.',
 'mod_gui.brandonscore.entity_filter.non_ageable.info': 'Опция «Включить нестареющих» считает любого моба, который не стареет, взрослым.',
 'mod_gui.brandonscore.entity_filter.player.false': 'Исключить игрока(ов)',
 'mod_gui.brandonscore.entity_filter.player.info': 'Этот фильтр действует на всех игроков,\nесли не введено конкретное имя игрока.',
 'mod_gui.brandonscore.entity_filter.player.true': 'Включить игрока(ов)',
 'mod_gui.brandonscore.entity_filter.tamable.info': 'Действует на приручаемых мобов, не принадлежащих игроку.',
 'struct_build.brandonscore.missing_required': 'Ошибка постройки: не удалось завершить структуру! Отсутствуют обязательные блоки: «%s».',
})

# ──────────────────────────── Kiwi ────────────────────────────
FIX.update({
 'commands.kiwi.reload.success': 'Конфигурация %s перезагружена.',
 'kiwi.config.debug.tagsPerPage.desc': 'Сколько тегов показывать на странице в расширенных подсказках',
 'msg.kiwi.no_dependencies': '{1}: модулю §e{2}§r нужны включённые модули: §e{3}',
 'tip.kiwi.debug_tooltip': '==========\nСпасибо, что пользуетесь Kiwi и другими модами Snownee!\nОтладочные подсказки тегов включены. %s — отключить.\nУдерживайте Alt для списка тегов. Пока теги показаны, нажимайте Alt / Ctrl+Alt для перехода между страницами.\n==========',
 'tip.kiwi.debug_tooltip.success': 'Отладочные подсказки отключены. Их можно снова включить в настройках.',
 'main_screen.btn.anchors.disabled': 'Якоря отключены!',
 'tip.inv_button': 'Кнопку инвентаря можно переместить: зажмите ПКМ над кнопкой и перетащите её',
 'tip.set_anchors': 'Нажмите любую из кнопок якорей (с номерами), затем кнопку с маркером (справа), чтобы задать положение якоря',
 'tip.modify_anchors': "Задав якоря, вы сможете в любой момент переместить их кнопкой с маркером.",
 'tip.modify_anchors_2': 'После задания положения можно изменить название и радиус якорей',
})

# ──────────────────────────── Lychee ────────────────────────────
FIX.update({
 'contextual.lychee.entity_health': 'Здоровье сущности равно: %s',
 'contextual.lychee.entity_health.not': 'Здоровье сущности не равно: %s',
 'contextual.lychee.location.can_see_sky.not': 'Не видит небо',
 'contextual.lychee.or': 'Верно при выполнении любого:',
 'dimension.minecraft.the_end': 'Край',
 'dimension.minecraft.the_nether': 'Нижний мир',
 'modmenu.descriptionTranslation.lychee': 'Крафт по данным прямо в игровом мире',
 'postAction.lychee.explode.destroy_with_decay': 'Взрыв (с разрушением блоков по затуханию)',
 'postAction.lychee.explode.keep': 'Взрыв (без урона по местности)',
 'recipeType.lychee.entity_ticking': 'Обновление сущностей',
 'tag.entity_type.lychee.lightning_fire_immune': 'Сущности, неуязвимые к огню молнии',
 'tag.entity_type.lychee.lightning_immune': 'Сущности, неуязвимые к молнии',
})

# ──────────────────────────── NBTac ────────────────────────────
FIX.update({
 'nbtac.options.field.short_boolean.desc': 'Предлагает 1b/0b вместо true/false для логических значений. Не применяется к «сырому» формату JSON, где требуется именно true/false.',
 'nbtac.options.field.string_quotation_marks.desc': 'Ставит значения строкового типа в кавычки.\ntrue — Всегда\nfalse — При необходимости',
 'nbtac.options.field.default_quotation_mark.desc': 'Задаёт тип кавычек по умолчанию.\nsingle — [\' ]\ndouble — [" ]\nНа версиях 1.21.4 и старше не влияет на текстовые компоненты JSON.',
 'nbtac.options.field.json_string_suggestion.desc': 'Задаёт подсказку открывающей кавычки для текстовых компонентов JSON.\nnone — Нет\nrecommended — Как "default_for_strings" на 1.20.4 и новее, как "legacy_separated" на старых.\ndefault_for_strings — использует тип кавычки из настройки «Тип кавычек по умолчанию».\nlegacy_separated — [\' ]\nlegacy_merged — [\'] — как legacy_separated, но без пробела.\nlegacy_backslash — ["\\"].',
 'nbtac.options.field.show_tag_hints.desc': 'Показывает рядом с именем тега подсказки о нём (например, его тип или источник) в списке подсказок.',
 'nbtac.options.field.update_on_cursor_movement.desc': 'Обновляет подсказки при перемещении курсора.\nЭто касается всех подсказок команд, не только тех, что даёт NBT Autocomplete.\nНастройка возвращает поведение до Minecraft 26.1; значение false на старых версиях ничего не меняет.',
 'nbtac.options.field.placing_of_irrelevant.desc': 'Задаёт расположение нерелевантных подсказок.\nВозможные значения: normal, at_the_bottom, hidden\nВсе значения кроме "normal" требуют включения пользовательской сортировки.',
 'nbtac.options.field.unknown_item_components.desc': 'Задаёт релевантность неизвестных компонентов предметов по умолчанию.\nВ основном касается компонентов из модов.\nЕсли вы видите такие компоненты в ванилле (установлен только NBTac), сообщите об этом как об ошибке.\nrelevant_by_default — неизвестные компоненты считаются релевантными по умолчанию.\nrelevant_within_namespace — неизвестные компоненты релевантны, только если их ID в том же пространстве имён, что и ID предмета.\nirrelevant_by_default — неизвестные компоненты считаются нерелевантными по умолчанию.',
 'nbtac.options.field.vanilla_ids_sorting': 'Сортировка ванильных ID',
 'nbtac.options.field.vanilla_ids_sorting.desc': 'Задаёт способ сортировки ванильных ID (пространство "minecraft:") относительно неванильных ID (из модов). Касается только подсказок NBT Autocomplete. Настройка тесно связана с «Скрывать пространство Minecraft в строках».\n0 — Без пользовательской сортировки — при скрытом ванильном пространстве ID разбросаются между ID из модов, поэтому не рекомендуется.\n1 — Зависит от скрытия пространства: наверху, если пространство скрыто, иначе в середине.\n2 — Всегда наверху\n3 — Всегда в середине\nПрочие значения обрабатываются как 1.',
 'nbtac.options.field.debug_sleep.desc': 'Откладывает загрузку подсказок, чтобы точнее измерить время загрузки (в миллисекундах; рекомендуется несколько тысяч).',
 'nbtac.options.common.tooltip': '%s\n§8По умолчанию: %s',
 'nbtac.options.common.gui_debug_warning': 'Включён отладочный режим экрана настроек!',
})


# ─────────────────────── BonsaiTrees4 / Ars Creo ───────────────────────
FIX.update({
 'commands.bonsaitrees4.generate.model.flood_fill.too_large': 'Модель содержит слишком много блоков. Максимум — %d.',
 'commands.bonsaitrees4.generate.model.flood_fill.too_many_states': 'Модель содержит слишком много состояний блоков. Максимум — %d.',
 'bonsaitrees4.tooltip.unable_to_insert': 'Эти предметы вставить нельзя.',
 'bonsaitrees4.configuration.debug.show_unused_soil_recipes_in_jei': 'Показывать неиспользуемые почвы в JEI',
 'bonsaitrees4.configuration.debug.show_rolls_as_count_in_jei': 'Показывать рулоны как количество предметов в JEI',
 'bonsaitrees4.configuration.gameplay.cutCooldown.tooltip': 'Нужен в первую очередь, чтобы сервер не перегружался от спама попыток срезать бонсай, если выходной слот заполнен. Задержка в тиках перед повторной попыткой срезать бонсай.',
 'bonsaitrees4.configuration.gameplay.toolDamageChance.tooltip': 'Шанс, что инструмент получит повреждения при срезании бонсая.',
 'bonsaitrees4.configuration.gameplay.toolAllowIndestructible.tooltip': 'Если включено, для срезания бонсая можно использовать инструменты, не теряющие прочность (например, топор Supremium из Mystical Agriculture).',
 'bonsaitrees4.configuration.client.minimal_quads': 'Рисовать только квады корпуса',
 'bonsaitrees4.configuration.client.minimal_quads.tooltip': 'Если включено, рисуются только квады корпуса горшка бонсая. Это может помочь производительности на очень слабых машинах.',
 'bonsaitrees4.configuration.client.disable_model_cache.tooltip': 'Если включено, кэш моделей отключается. Полезно для разработки и отладки, но заметно бьёт по производительности даже на мощных машинах.',
 'bonsaitrees4.configuration.client.show_tree_in_sapling_tooltip.tooltip': 'Если включено, дерево рисуется в подсказке саженца.',
 'ars_creo.page1.starbuncle_wheel': 'Вырабатывает вращательную силу для Create. Золотой блок перед колесом увеличит число оборотов.',
 'ars_creo.page1.ars_creo.display_link': 'Связь с индикатором можно подключить к источнику в сосуде, чтобы показывать количество источника, либо к турели заклинаний, чтобы выводить текущее заклинание на табло.',
 'ars_creo.page1.ars_creo.fluid_tank': 'Зелья можно хранить в баке Create и перекачивать между баками трубами и баком.',
})

# ─────────────────────── Building Gadgets 2 ───────────────────────
FIX.update({
 'buildinggadgets2.messages.anchorset': 'Якорь установлен на: ',
 'buildinggadgets2.messages.areatoolarge': 'Область слишком велика! Максимум: %d. Было: %d',
 'buildinggadgets2.messages.axistoolarge': 'Ось %s слишком велика! Максимум: %d. Было: %d',
 'buildinggadgets2.messages.bindsuccess': 'Привязка выполнена к: %s',
 'buildinggadgets2.messages.cutinprogress': 'Идёт вырезание — подождите!',
 'buildinggadgets2.messages.namealreadyexists': 'Это имя уже занято: удалите его командами или задайте новое.',
 'buildinggadgets2.messages.namerequired': 'Для чертежей нужно имя. Попробуйте ещё раз.',
 'buildinggadgets2.messages.range_set': 'Радиус задан: %d',
 'buildinggadgets2.messages.relativepaste': 'Относительная вставка задана: [%s]',
 'buildinggadgets2.messages.render_set': 'Тип отрисовки задан: %s',
 'buildinggadgets2.screen.affecttiles': 'Затрагивать блок-сущности',
 'buildinggadgets2.snap': 'ЩЁЛК!',
 'buildinggadgets2.tooltips.boundto': 'Привязано к: %s:%s',
 'buildinggadgets2.tooltips.holdshift': 'Удерживайте Shift для подробностей',
 'text.craftingstation.error': 'Найден неверный рецепт %s, подробности в логе',
 'text.craftingstation.clear': 'Очистить сетку крафта',
 'deus_ex_machina.death_screen.header': 'Эффекты Deus Ex Machina у %s:',
})

# ─────────────────────── Idle Cinematics ───────────────────────
FIX.update({
 'idlecinematics.settings.entities': 'Оживлять находящихся рядом мобов',
 'idlecinematics.settings.reset_section_confirm_title': 'Сбросить %s к значению по умолчанию?',
 'idlecinematics.settings.reset_all_confirm_title': 'Сбросить все настройки IDLE?',
 'idlecinematics.settings.reset_all_confirm_message': 'Все настройки вернутся к значениям по умолчанию. Изменения останутся черновиком, пока вы не нажмёте «Готово».',
 'jumbofurnace.jumbo_furnace_upgrade_info': 'Предмет(ы) выше можно положить в слот улучшения Jumbo Furnace, чтобы увеличить число рецептов, перерабатываемых за один цикл.',
 'jumbofurnace.advancements.story.obtain_jumbo_furnace.description': 'Создайте Jumbo Furnace (можно переплавкой 27 печей в Jumbo Furnace)',
 'leaderboard.leaderboards.mob_kills': 'Убийства мобов',
 'leaderboard.leaderboards.vanilla_stats': 'Ванильная статистика',
 'item.magic_coins.silver_coin.description': 'Номинал этой монеты — $%s',
 'item.magic_coins.gold_coin.description': 'Номинал этой монеты — $%s',
 'item.magic_coins.crystal_coin.description': 'Номинал этой монеты — $%s',
})

# ─────────────────── Mekanistic Routers / прочее ───────────────────
FIX.update({
 'mekanisticrouters.guiText.popup.chemical.maxTransfer':
   '§a§nМаксимальная передача§r\n\nЭто максимальное количество химиката, которое роутер попытается передать за одну операцию.\n\nУчтите, что скорость передачи всё равно ограничена числом улучшений передачи химикатов в роутере, а также скоростью передачи внешнего бака (если он есть). Поэтому это значение стоит считать пределом, а не целью.',
 'mekanisticrouters.itemText.usage.item.chemical_module_mk1':
   "Передаёт химикаты в роутер или из него в соседний блок/из соседнего блока по направлению модуля.\n• В буфере роутера должен быть предмет-контейнер для химикатов.",
 'mekanisticrouters.itemText.usage.item.chemical_module_mk2':
   "Передаёт химикаты в роутер или из него в любой ближайший блок.\n• В буфере роутера должен быть предмет-контейнер для химикатов.\n• Может передавать в баки и из них.",
 'mekanisticrouters.guiText.popup.chemical_refill.control':
   '§a§Модуль пополнения химикатами§r\n\nЗдесь задаётся:\n- с каким разделом инвентаря игрока работает модуль.\n\nФильтр определяет, какие предметы могут получать химикаты из этого модуля.',
 'mecrh.message.chicken_despawn_warning': 'На арене нет игроков! Курица исчезнет через %s секунд!',
 'mecrh.message.wrong_forcefield_item': 'Неверное оружие для силового поля!',
 'item.rep_ae2_bridge.universal_matter.warning':
   'Внимание: это универсальная заполнительная материя; если вы только что добавили свои предметы-материи, перезапустите клиент и сервер.',
 'block.replication_rs2_bridge.rep_rs2_bridge.priority.help':
   'В автокрафте сначала используются шаблоны с более высоким приоритетом. Так можно отдать предпочтение крафту материи перед обычными рецептами.',
 'item.mob_bottle.tooltip_empty': 'ПКМ по уменьшенной сущности стеклянной бутылкой, чтобы поймать её',
 'shrink.deny_shrink': 'Уменьшение заблокировано из-за запрещающего тега',
 'ars_nouveau.augment_desc.glyph_pickup_fluid_glyph_sensitive':
   'Нацеливается на блок напрямую, а не на относительную позицию.',
 'ars_nouveau.starbuncle.saddle_behavior_set': 'Старбанкл теперь перевозит игроков',
 'ars_nouveau.starbuncle.sided_item_behavior_set': 'Старбанкл теперь вставляет/извлекает предметы с указанных сторон',
})


# ─────────────────────── RF Tools Builder ───────────────────────
_SHIELD = ('Эта машина строит щит из прилегающих блоков-шаблонов. Она умеет фильтровать по типу моба '
           'и выполнять разные действия (урон, твёрдость, ...). Чтобы добавить секцию щита, '
           'используйте умный гаечный ключ')
FIX.update({
 'message.rftoolsbuilder.builder.header':
   'Этот блок умеет вырезать карьеры, перекачивать жидкости, перемещать/копировать/менять местами '
   'конструкции, собирать предметы и опыт, перемещать сущности, строить конструкции, ...',
 'message.rftoolsbuilder.shield_block1.header': _SHIELD,
 'message.rftoolsbuilder.shield_block2.header': _SHIELD,
 'message.rftoolsbuilder.shield_block3.header': _SHIELD,
 'message.rftoolsbuilder.shield_block4.header': _SHIELD,
 'message.rftoolsbuilder.blue_shield_template_block.header':
   'Этот блок можно использовать для постройки щита. Блоки-шаблоны одного цвета должны соприкасаться с проектором щита',
 'message.rftoolsbuilder.red_shield_template_block.header':
   'Этот блок можно использовать для постройки щита. Блоки-шаблоны одного цвета должны соприкасаться с проектором щита',
 'message.rftoolsbuilder.green_shield_template_block.header':
   'Этот блок можно использовать для постройки щита. Блоки-шаблоны одного цвета должны соприкасаться с проектором щита',
 'message.rftoolsbuilder.yellow_shield_template_block.header':
   'Этот блок можно использовать для постройки щита. Блоки-шаблоны одного цвета должны соприкасаться с проектором щита',
 'message.rftoolsbuilder.mover.gold': 'Используется вместе с контроллером перемещения',
 'message.rftoolsbuilder.mover_controller.header':
   'Контроллер системы перемещения. Поставьте его рядом с одним из перемещателей',
 'message.rftoolsbuilder.mover_control.header':
   'Экран управления перемещением (страница 1). Этим блоком можно управлять движением платформы',
 'message.rftoolsbuilder.mover_control2.header':
   'Экран управления перемещением (страница 2). Этим блоком можно управлять движением платформы',
 'message.rftoolsbuilder.mover_control3.header':
   'Экран управления перемещением (страница 3). Этим блоком можно управлять движением платформы',
 'message.rftoolsbuilder.mover_control4.header':
   'Экран управления перемещением (страница 4). Этим блоком можно управлять движением платформы',
 'message.rftoolsbuilder.mover_status.header':
   'Экран состояния перемещателя. Этим блоком можно получить больше сведений о платформе',
 'message.rftoolsbuilder.space_chamber_card.gold':
   'Shift+ПКМ по контроллеру космической камеры задаёт канал этой карточке. ПКМ по воздуху показывает '
   'обзор содержимого области. Вставьте карточку в строитель, чтобы скопировать или переместить связанную область',
 'message.rftoolsbuilder.shape_card.warning': 'Отключено в настройках!',
 'message.rftoolsbuilder.shape_card.header':
   'Карточка задаёт область для щита или строителя. Shift+ПКМ по строителю включает режим разметки, '
   'затем отметьте ПКМ два угла нужной области',
})

# ─────────────────────── StarbuncleMania ───────────────────────
_COS = ' Можно просто надеть как декорацию: Shift+ПКМ не меняет текущее поведение.'
FIX.update({
 'starbunclemania.adv.title.source_condenser': 'Еретическая слизь',
 'starbunclemania.adv.title.wyrm_degree': 'Это ещё и благодаря моим двум степеням...',
 'starbunclemania.balloon.tooltip':
   'Меняет работу старбанкла на перевозку химикатов Mekanism. Можно покрасить и использовать просто как '
   'декорацию: Shift+ПКМ не меняет текущее поведение. Поддерживаются фамильяры: старбанкл, дригми, викси.',
 'starbunclemania.battery.tooltip':
   'Меняет работу старбанкла на перевозку энергии FE.' + _COS,
 'starbunclemania.bucket.tooltip':
   'Меняет работу старбанкла на перевозку жидкостей.' + _COS,
 'starbunclemania.builder_hat.tooltip':
   'Меняет работу старбанкла на установку блоков в заданные места.' + _COS,
 'starbunclemania.chef_hat.tooltip':
   'Декоративный аксессуар для игроков, старбанклов и фамильяров-викси.',
 'starbunclemania.degree_hat.tooltip':
   'Декорация для ваших фамильяров. Поддерживаемые существа: старбанклы, книжный червь (фамильяр). '
   'Также позволяет «предметным» старбанклам читать свитки направления.',
 'starbunclemania.glyph_desc.glyph_pickup_fluid':
   'Забирает жидкости из мира и наполняет бак на хотбаре или рядом с турелью',
 'starbunclemania.glyph_desc.glyph_place_fluid':
   'Разливает жидкость в мир из бака на хотбаре или рядом с турелью',
 'starbunclemania.miner_hat.tooltip':
   'Меняет работу старбанкла на добычу блоков в заданных местах. Можно выдать ему инструмент, '
   'по умолчанию — железная кирка.' + _COS,
 'starbunclemania.robin_mask.tooltip':
   'Меняет перевозку предметов старбанклом на круговой обход инвентарей и отключает подбор. '
   'Можно просто надеть как декорацию: Shift+ПКМ не меняет текущее поведение.',
 'starbunclemania.trash_bin.tooltip':
   'Меняет работу старбанкла на уничтожение поднятых им предметов и предметов из связанных сундуков.' + _COS,
 'starbunclemania.wixie_cauldron.mixer.tooltip':
   'Используйте очарование викси на сосуде с жидкостью. Тогда викси будут брать для рецептов воду и молоко '
   'из боковых баков вместо ведер.',
 'starbunclemania.page.fluid_jar':
   'Бак из каскадных брёвен архудревесины, вмещает до 16 ведер жидкости. Если хранить в нём зелье, а сверху '
   'поставить сосуд для зелий, жидкость будет переливаться в сосуд — для фляжек и слияния.',
 'starbunclemania.page.player_cosmetic':
   'Хотелось когда-нибудь выглядеть как старбанкл? Или носить классную соломенную шляпу, рога или даже '
   'пропеллер? Теперь это возможно! Аксессуары чисто декоративные, их носят игроки в слоте «Голова».',
 'starbunclemania.page.robin_mask':
   'Позволяет старбанклам по кругу раздавать предметы по инвентарям. Пока аксессуар надет, старбанкл только '
   'подбирает предметы с земли. Каждый старбанкл в стопке несёт maxStack/число инвентарей предметов, прежде '
   'чем перейти к следующему инвентарю.',
 'starbunclemania.page.source_condenser':
   'Сжижает источник из сосудов в стабильную жидкость. По возможности сам выдаёт её в бак под собой. '
   'Конкретный источник можно привязать палочкой Домениона — иначе он берётся из всех сосудов рядом.',
 'starbunclemania.page.star_balloon':
   'Позволяет старбанклам перевозить простые газы [Mekanism]. Аксессуар можно покрасить для внешнего вида, '
   'но при перевозке используется цвет хранимого газа. Надев аксессуар, свяжите старбанклов между баками газа '
   'палочкой Домениона.',
 'starbunclemania.page.star_bin':
   'Позволяет старбанклам раскрыть внутреннего енота и выбрасывать предметы. Пока аксессуар надет, '
   'старбанкл уничтожает любой лежащий рядом предмет.',
 'starbunclemania.page.star_hat':
   'Аксессуары StarbuncleMania не только для рабочих старбанклов — их можно надеть и на фамильяра-старбанкла! '
   'Этот ничего не делает, зато стильно. Shift+ПКМ по старбанклу с аксессуаром задаёт только внешний вид, '
   'не меняя работу.',
 'starbunclemania.page.star_saddle':
   'Позволяет старбанклам перевозить игроков. Пока аксессуар надет, старбанкл становится достаточно '
   'большим, чтобы его можно было оседлать. [Функция ещё в доработке]',
 'starbunclemania.page.wixie_cook':
   'Теперь викси умеет готовить! Поставьте викси на печь, чтобы он переплавлял и коптил предметы; также '
   'работает с котлом [Farmer’s Delight] и тиглями [Eidolon Repraised].',
 'starbunclemania.page.wixie_cut':
   'Поставьте очарование викси на каменоломню или разделочную доску, чтобы викси использовал их рецепты '
   'для крафта.',
 'starbunclemania.page.wixie_mixer':
   'Поставьте очарование викси на сосуд с жидкостью, чтобы превратить его в котёл другого типа. У смесителя '
   'два внутренних бака — для воды и молока, чтобы не использовать ведра в рецептах. Работает только для '
   'рецептов верстака и всегда расходует ровно одну единицу нужной жидкости.',
 'starbunclemania.page.wyrm_degree':
   'Позволяет старбанклам вставлять и извлекать предметы с указанной стороны с помощью свитков направления.',
})

# ─────────────────────── Step Crafter ───────────────────────
FIX.update({
 'item.stepcrafter.step_crafter.help':
   'Принимает шаблоны для рекурсивного автокрафта и позволяет задать нижний и верхний пороги. Когда выход '
   'заданного шаблона в сети падает ниже минимума, он автоматически крафтит предметы до максимума.',
 'item.stepcrafter.step_requester.help':
   'Когда заданный ресурс падает ниже настраиваемого минимума, он запрашивает у автокрафтеров нужный объём '
   'партиями заданного размера, пока не будет достигнут настраиваемый максимум.',
 'item.stepcrafter.step_crafting_monitor.help':
   'Показывает состояние пошаговых заданий крафта и позволяет отменять их в вашей сети хранения.',
 'gui.stepcrafter.step_crafter.filter_help': 'Шаблон, который нужно обходить рекурсивно, чтобы поддерживать запас',
 'gui.stepcrafter.step_requester.filter_help': 'Ресурсы, которые нужно запрашивать у автокрафтеров для поддержания запаса',
 'gui.stepcrafter.step_crafter.visible_to_the_step_crafter_manager.help':
   'Если включено, этим пошаговым крафтером можно управлять в менеджере пошаговых крафтеров.',
 'gui.stepcrafter.step_requester.visible_to_the_step_requester_manager.help':
   'Если включено, этим запрашивателем можно управлять в менеджере запрашивателей.',
 'gui.stepcrafter.step_crafter.insert_into_pointed_container': 'Вставлять в указанный контейнер',
 'gui.stepcrafter.step_crafter.insert_into_pointed_container.help':
   'Если включено, пошаговый крафтер попробует сложить изготовленные компоненты в контейнер, на который направлен.',
 'gui.stepcrafter.filter_slot.network_full': 'Сеть заполнена. Невозможно вставить результат крафта',
 'gui.stepcrafter.filter_slot.external_container_full': 'Внешний контейнер заполнен или не принимает предметы',
 'text.autoconfig.stepcrafter.option.stepCrafter.energyUsage.tooltip': 'Энергия, потребляемая пошаговым крафтером.',
 'text.autoconfig.stepcrafter.option.stepCrafter.speedMultiplier.tooltip':
   'Множитель скорости за каждое улучшение слота у пошагового крафтера. Формула: <SpeedUpgradeAmount> * <SpeedMultiplier> + 1',
 'text.autoconfig.stepcrafter.option.stepRequester.energyUsage.tooltip': 'Энергия, потребляемая запрашивателем.',
 'text.autoconfig.stepcrafter.option.stepCrafterManager.energyUsage.tooltip': 'Энергия, потребляемая менеджером пошаговых крафтеров.',
 'text.autoconfig.stepcrafter.option.stepCrafterManager.searchMode.tooltip': 'Режим поиска.',
 'text.autoconfig.stepcrafter.option.stepCraftingMonitor.energyUsage.tooltip': 'Энергия, потребляемая монитором пошагового крафта.',
 'text.autoconfig.stepcrafter.option.stepRequesterManager.energyUsage.tooltip': 'Энергия, потребляемая менеджером запрашивателей.',
 'text.autoconfig.stepcrafter.option.stepRequesterManager.searchMode.tooltip': 'Режим поиска.',
 'text.autoconfig.stepcrafter.option.slotUpgrade.energyUsage.tooltip': 'Энергия, потребляемая улучшением слота.',
 'text.autoconfig.stepcrafter.option.resourceConfigurationDefaults.defaultMinAmount.tooltip':
   'Минимальное количество по умолчанию для новых настраиваемых ресурсов.',
 'text.autoconfig.stepcrafter.option.resourceConfigurationDefaults.defaultMaxAmount.tooltip':
   'Максимальное количество по умолчанию для новых настраиваемых ресурсов.',
 'text.autoconfig.stepcrafter.option.resourceConfigurationDefaults.defaultBatchSize.tooltip':
   'Размер партии по умолчанию для новых настраиваемых ресурсов.',
})


# ──────────────── RGP Client (FTB Worlds) / RSRequestify ────────────────
FIX.update({
 'rgp_client.gui.promo_header': 'Хотите легко играть с друзьями?',
 'rgp_client.gui.promo_subheader':
   'Создайте FTB World в пару кликов и пригласите друзей присоединиться!',
 'rgp_client.gui.button.pending':
   'Сейчас наши сервисы перегружены. Мы постараемся запустить ваш FTB World как можно скорее.',
 'rgp_client.gui.callback.invalid_code': 'Указан неверный код приглашения! Проверьте правильность кода.',
 'rgp_client.gui.worlds_creation.region_selection_disabled':
   'Выбор региона отключён для FTB World, созданных ранее в другом регионе.',
 'rgp_client.gui.worlds_creation.loadout_selection_details': 'Сведения о плане:',
 'rgp_client.gui.worlds_edit.restore_backup_confirmation':
   'Точно восстановить эту резервную копию? Текущий мир будет перезаписан, а весь прогресс после этой '
   'точки будет потерян.',
 'rgp_client.gui.worlds_edit.switch_confirmation':
   'Точно выбрать этот мир? Все игроки онлайн будут отключены, а сервер перезапущен.',
 'rgp_client.gui.worlds_edit.no_backups': 'Резервных копий этого мира ещё не создавалось.',
 'rgp_client.gui.worlds_configuration.cannot_start_different_modpack':
   'Выбранный мир относится к другому модпаку',
 'rgp_client.gui.worlds_create_invite.single_use_invite_desc':
   'Одноразовый код после принятия автоматически добавляет игрока в мир и может быть использован только один раз.',
 'rgp_client.gui.worlds_create_invite.shared_invite_desc':
   'Общий код можно использовать неограниченно долго, но каждого игрока придётся одобрять вручную.',
 'rgp_client.gui.worlds_invite_management.invite_code_copied_to_clipboard':
   'Код приглашения скопирован в буфер обмена.',
 'block.rsrequestify.crafting_emitter.tooltip':
   'Подаёт редстоун-сигнал, когда в фильтре есть задание на крафт указанного предмета/жидкости, '
   'а объём крафта равен заданному или больше него.',
 'block.rsrequestify.requester.tooltip':
   'Поддерживает запас отфильтрованного предмета/жидкости в нужном количестве.',
 'block.rsrequestify.requester.tooltip.filter':
   'Ресурсы, которые нужно держать в запасе в сети хранения',
 'block.rsrequestify.crafting_emitter.tooltip.filter':
   'Ресурсы, по которым нужно подавать редстоун-сигнал',
})
