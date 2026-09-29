# -*- coding: utf-8 -*-
"""Переводы англоязычных строк, зашитых прямо в .class (подсказки конфигов, подписи GUI).

Сюда попадает только пользовательский текст. Сообщения лога и ошибки
(«Failed to …», «Cannot …») намеренно остаются латиницей — в лог игрок не смотрит.
Плейсхолдеры %s, %d и форматирование Minecraft сохраняются как есть.
"""

CLASS = {
    # ─────────────── rftoolsbuilder ───────────────
    'Additional amount of RF per 16x16x16 subchunk needed for a filtered scan':
        'Дополнительное количество RF на подчанк 16x16x16, нужное для сканирования с фильтром',
    'Additional amount of RF per 16x16x16 subchunk needed for a scan for hostile entities':
        'Дополнительное количество RF на подчанк 16x16x16, нужное для сканирования враждебных сущностей',
    'Additional amount of RF per 16x16x16 subchunk needed for a scan for low energy':
        'Дополнительное количество RF на подчанк 16x16x16, нужное для сканирования низкой энергии',
    'Additional amount of RF per 16x16x16 subchunk needed for a scan for passive entities':
        'Дополнительное количество RF на подчанк 16x16x16, нужное для сканирования мирных сущностей',
    'Additional amount of RF per 16x16x16 subchunk needed for a scan for players':
        'Дополнительное количество RF на подчанк 16x16x16, нужное для сканирования игроков',
    'Amount of RF needed per tick during the scan':
        'Количество RF, расходуемое за тик во время сканирования',
    'Amount of RF needed per tick during the scan for a remote scanner':
        'Количество RF, расходуемое за тик во время сканирования удалённым сканером',
    'Amount of RF used for one movement':
        'Количество RF на одно перемещение',
    'Amount of dimensional shards per looting kill. Remember that this is per block that does damage':
        'Количество осколков измерений за убийство с добычей. Помните: значение считается на каждый '
        'блок, нанесший урон',
    'Base RF per block operation for the builder when used as a pump':
        'Базовое количество RF на операцию с блоком, когда Строитель используется как насос',
    'Base RF per block operation for the builder when used as a quarry or voider (actual cost depends on hardness of block)':
        'Базовое количество RF на операцию с блоком, когда Строитель используется как карьер или '
        'уничтожитель (итоговая стоимость зависит от твёрдости блока)',
    'Base amount of RF needed for a scan per 16x16x16 subchunk':
        'Базовое количество RF на сканирование подчанка 16x16x16',
    'Base amount of RF/tick for every 10 blocks in the shield (while active)':
        'Базовое количество RF/тик на каждые 10 блоков щита (пока он активен)',
    'Color for the shield': 'Цвет щита',
    'Damage as done by a player': 'Урон, наносимый игроком',
    'Drag left mouse button to rotate': 'Зажмите ЛКМ и потяните, чтобы повернуть',
    'Enable preview of the': 'Включить предпросмотр',
    'Enable visual scanlines when the scan refreshes':
        'Показывать визуальную линию сканирования при обновлении сканирования',
    'Entity that matches this filter': 'Сущность, подходящая под этот фильтр',
    'Fixed amount of RF needed for a scan': 'Фиксированное количество RF на сканирование',
    'Height of the beacon in case beacons are used': 'Высота маяка, если маяки используются',
    'How many projector preview planes to aggregate before logging one compression summary line':
        'Сколько слоёв предпросмотра проектора объединять перед выводом одной сводной строки о сжатии',
    'How many ticks to wait before sending the next projector preview plane to clients. Increase this to spread setup load over time':
        'Сколько тиков ждать перед отправкой клиентам следующего слоя предпросмотра проектора. '
        'Увеличьте, чтобы распределить нагрузку во времени',
    'How many ticks we wait before collecting again (with the builder \'collect items\' mode)':
        'Сколько тиков ждать перед повторным сбором (в режиме Строителя «собирать предметы»)',
    'How much more expensive a move accross dimensions is':
        'Во сколько раз перемещение между измерениями дороже',
    'If pressed, light is blocked': 'Если включено, свет блокируется',
    'If true a holo hud with current progress is shown above the builder':
        'Если включено, над Строителем показывается голографический индикатор с текущим прогрессом',
    'If true the quarry will also quarry tile entities. Otherwise it just ignores them':
        'Если включено, карьер добывает и блок-сущности. Иначе он их игнорирует',
    'If true the quarry will chunkload a chunk at a time. If false the quarry will stop if a chunk is not loaded':
        'Если включено, карьер догружает по одному чанку. Если выключено, карьер остановится, '
        'если чанк не загружен',
    'If true we allow quarry cards to be crafted':
        'Если включено, разрешено создавать карты карьера',
    'If true we allow shape cards to be crafted. Note that in order to use the quarry system you must also enable this':
        'Если включено, разрешено создавать карты формы. Обратите внимание: для работы системы '
        'карьера нужно включить и этот параметр',
    'If true we allow the clearing quarry cards to be crafted (these can be heavier on the server)':
        'Если включено, разрешено создавать карты очищающего карьера (они сильнее нагружают сервер)',
    'If true we go back to the old (wrong) sphere/cylinder calculation for the builder/shield':
        'Если включено, для Строителя и щита используется старая (неверная) формула сферы и цилиндра',
    'Log aggregated statistics about projector preview packet compression. Intended as a diagnostic for scan/blockstate-heavy previews':
        'Выводить сводную статистику сжатия пакетов предпросмотра проектора. Нужно для диагностики '
        'тяжёлых превью сканирования и состояний блоков',
    'Maximum RF storage that the builder can hold': 'Максимальный запас RF, который вмещает Строитель',
    'Maximum RF storage that the locator can hold': 'Максимальный запас RF, который вмещает локатор',
    'Maximum RF storage that the mover controller can hold':
        'Максимальный запас RF, который вмещает контроллер перемещения',
    'Maximum RF storage that the projector can hold': 'Максимальный запас RF, который вмещает проектор',
    'Maximum RF storage that the scanner can hold': 'Максимальный запас RF, который вмещает сканер',
    'Maximum RF storage that the shield block can hold': 'Максимальный запас RF, который вмещает блок щита',
    'Maximum amount of 16x16 chunks we support for energy scanning':
        'Максимальное количество чанков 16x16, поддерживаемое при сканировании энергии',
    'Maximum amount of entities in a single block to show markers/beacons for':
        'Максимальное количество сущностей в одном блоке, для которых показываются метки и маяки',
    'Maximum dimension for the space chamber': 'Максимальный размер камеры пространства',
    'Maximum dimension of the shape when a scanner/projector card is used':
        'Максимальный размер формы при использовании карты сканера/проектора',
    'Maximum dimension of the shape when a shape card is used':
        'Максимальный размер формы при использовании карты формы',
    'Maximum dimension of the shape when a shape card is used in the builder':
        'Максимальный размер формы при использовании карты формы в Строителе',
    'Maximum distance at which you can add disjoint shield sections to a composed shield':
        'Максимальное расстояние, на котором можно добавить отдельный сегмент к составному щиту',
    'Maximum offset of the shape when a shape card is used':
        'Максимальное смещение формы при использовании карты формы',
    'Maximum offset of the shape when a shape card is used in the builder':
        'Максимальное смещение формы при использовании карты формы в Строителе',
    'Maximum offset of the shape when a shape card is used in the scanner/projector':
        'Максимальное смещение формы при использовании карты формы в сканере/проекторе',
    'Maximum size (in blocks) of a tier 1 shield': 'Максимальный размер (в блоках) щита 1-го уровня',
    'Multiply the infusion factor with this value and add that to the quarry base speed':
        'Умножать коэффициент заливки на это значение и прибавлять к базовой скорости карьера',
    'Number of ticks between every scan of the locator': 'Количество тиков между сканированиями локатора',
    'Open the shape card editor': 'Открыть редактор карт формы',
    'RF per block operation for the builder when used to build':
        'RF на операцию с блоком, когда Строитель используется для строительства',
    'RF per block that is skipped (used when a filter is added to the builder)':
        'RF на каждый пропущенный блок (используется, когда к Строителю добавлен фильтр)',
    'RF per entity move operation for the builder': 'RF на операцию перемещения сущности Строителем',
    'RF per player move operation for the builder': 'RF на операцию перемещения игрока Строителем',
    'RF per tick that the builder can receive': 'RF/тик, которые может принимать Строитель',
    'RF per tick that the locator can receive': 'RF/тик, которые может принимать локатор',
    'RF per tick that the mover controller can receive':
        'RF/тик, которые может принимать контроллер перемещения',
    'RF per tick that the projector can receive': 'RF/тик, которые может принимать проектор',
    'RF per tick that the scanner can receive': 'RF/тик, которые может принимать сканер',
    'RF per tick that the shield block can receive': 'RF/тик, которые может принимать блок щита',
    'RF per tick/per block for the vehicle control module':
        'RF на тик и на блок для модуля управления транспортом',
    'RF per tick/per block for the vehicle status module':
        'RF на тик и на блок для модуля состояния транспорта',
    'RF/t for the projector while it is in use': 'RF/тик для проектора во время работы',
    'RF/tick for every 10 block addeds in case of shield mode':
        'RF/тик на каждые 10 добавленных блоков в режиме щита',
    'RF/tick for every 10 blocks added in case of camo mode':
        'RF/тик на каждые 10 добавленных блоков в режиме камуфляжа',
    'Render baked block models in the world': 'Отрисовывать запечённые модели блоков в мире',
    'Set this to false if you don\'t want invisible shield rendering mode to be possible':
        'Поставьте false, если не хотите режим отрисовки невидимого щита',
    'Settings for the builder': 'Настройки Строителя',
    'Settings for the mover system': 'Настройки системы перемещения',
    'Settings for the scanner, composer, and projector':
        'Настройки сканера, композитора и проектора',
    'Settings for the shield system': 'Настройки системы щита',
    'Shield is completely invisible': 'Щит полностью невидим',
    'Shift drag left mouse to pan': 'Shift + перетаскивание ЛКМ — перемещение',
    'Show a visual scanline': 'Показывать визуальную линию сканирования',
    'The RF per operation of the builder is multiplied with this factor when using the fortune quarry shape card':
        'RF за операцию Строителя умножаются на этот коэффициент при использовании карты формы '
        'карьера с удачей',
    'The RF per operation of the builder is multiplied with this factor when using the silk quarry shape card':
        'RF за операцию Строителя умножаются на этот коэффициент при использовании карты формы '
        'карьера с шёлковым касанием',
    'The RF per operation of the builder is multiplied with this factor when using the void shape card':
        'RF за операцию Строителя умножаются на этот коэффициент при использовании карты формы '
        'уничтожителя',
    'The RF/t per area to keep checking for items in a given area (builder \'collect items\' mode))':
        'RF/тик на проверку предметов в заданной области (режим Строителя «собирать предметы»)',
    'The amount of \'surface area\' that the server will send to the client for the projector. Increasing this will increase the speed at which projections are ready but also increase the load for server and client':
        'Объём «площади поверхности», который сервер отправляет клиенту для проектора. Увеличение '
        'ускорит готовность проекций, но повысит нагрузку на сервер и клиент',
    'The amount of RF to consume for a single spike of damage for one entity':
        'Количество RF, расходуемое на единичный всплеск урона по одной сущности',
    'The amount of RF to consume for a single spike of damage for one entity (used in case of player-type damage)':
        'Количество RF, расходуемое на единичный всплеск урона по одной сущности '
        '(используется для урона от игрока)',
    'The amount of damage to do for a single spike on one entity':
        'Количество урона за единичный всплеск по одной сущности',
    'The amount of milliseconds before the client will remove shape render data that hasn\'t been used. Decreasing this will free memory faster at the cost of having to update shape renders more often':
        'Количество миллисекунд, через которое клиент удалит неиспользуемые данные отрисовки формы. '
        'Уменьшение освобождает память быстрее, но требует чаще обновлять отрисовку',
    'The amount of milliseconds that a scanline \'flash\' will exist on the client':
        'Количество миллисекунд, сколько «вспышка» линии сканирования будет на клиенте',
    'The amount of surface area the scanner will scan in a tick. Increasing this will increase the speed of the scanner but cause more strain on the server':
        'Площадь, которую сканер обработает за тик. Увеличение повысит скорость сканера, но '
        'нагрузит сервер',
    'The base speed (number of blocks per tick) of the quarry':
        'Базовая скорость карьера (количество блоков за тик)',
    'The cost of collecting 1 XP level (builder \'collect items\' mode))':
        'Стоимость сбора 1 уровня опыта (режим Строителя «собирать предметы»)',
    'The cost of collecting an item (builder \'collect items\' mode))':
        'Стоимость сбора одного предмета (режим Строителя «собирать предметы»)',
    'The looting kill bonus': 'Бонус за убийство с добычей',
    'The volume for the projector sound (0.0 is off)': 'Громкость звука проектора (0.0 — выключить)',
    'Use middle click to reset rotation': 'СКМ — сбросить поворот',
    'Use mouse wheel to zoom in/out': 'Колесо мыши — приблизить или отдалить',
    'Use the texture from the supplied block': 'Использовать текстуру указанного блока',
    'Use this block for the builder to replace with': 'Использовать этот блок для замены Строителем',

    # ─────────────── mighty-ender-chicken-rehatched ───────────────
    'Chicken will despawn if no players are in the arena for 30 seconds':
        'Курица исчезнет, если в арене 30 секунд не будет игроков',
    'Damage dealt by the laser to entities it hits (ignores armor)':
        'Урон, который лазер наносит задетым сущностям (броня игнорируется)',
    'Damage increase (multiplicative) for the laser every second an entity remains in the laser beam':
        'Множительный рост урона лазера за каждую секунду, пока сущность находится в луче',
    'Damage the chicken will do to entities hit by the charge attack':
        'Урон, который курица наносит сущностям, задетым рывком',
    'Height in blocks above chicken\'s foot position that the zombie rider initially spawns':
        'Высота в блоках над позицией лап курицы, на которой изначально появляется зомби-наездник',
    'If false, Ender Chicken only targets players, and entities which attack it':
        'Если false, Курица Эндера нацелена только на игроков и на сущности, которые её атакуют',
    'If true, any blocks broken by the chicken will drop items as normal':
        'Если true, любые разрушенные курицей блоки выпадают как обычно',
    'If true, non-player damage sources ignore the chicken shield':
        'Если true, источники урона не от игрока игнорируют щит курицы',
    'Invulnerability ticks (iframes) for entities hit by the laser':
        'Тики неуязвимости (неуязвимости после попадания) для сущностей, задетых лазером',
    'Max amount of incoming damage that the chicken can take in one hit':
        'Максимальный входящий урон, который курица может получить за одно попадание',
    'Max distance in blocks that the chicken will charge':
        'Максимальное расстояние рывка курицы в блоках',
    'Max number of special attacks which can be used at one time':
        'Максимальное количество особых атак, которые можно применить одновременно',
    'Max time in ticks that the chicken will spend charging':
        'Максимальное время рывка курицы в тиках',
    'Maximum interval in ticks for the laser attack': 'Максимальный интервал лазерной атаки в тиках',
    'Maximum interval in ticks for the peck of doom (leap/drop) attack':
        'Максимальный интервал атаки «судьбоносный клюв» (прыжок/падение) в тиках',
    'Maximum interval in ticks for the spin/egg-bomb attack':
        'Максимальный интервал атаки «вращение/яйцебомба» в тиках',
    'Maximum interval in ticks for the stampede/zombie spawn attack':
        'Максимальный интервал атаки «табун/призыв зомби» в тиках',
    'Maximum number of baby zombies spawned in the stampede':
        'Максимальное количество детских зомби, призываемых табуном',
    'Maximum speed for launched egg bombs': 'Максимальная скорость запускаемых яйцебомб',
    'Maximum time in ticks the chicken will spend trying to reach the target':
        'Максимальное время в тиках, которое курица потратит на попытку добраться до цели',
    'Maximum time the chicken will take to drop onto the target':
        'Максимальное время падения курицы на цель',
    'Minimum interval in ticks for the charge attack': 'Минимальный интервал атаки рывком в тиках',
    'Minimum interval in ticks for the laser attack': 'Минимальный интервал лазерной атаки в тиках',
    'Minimum interval in ticks for the peck of doom (leap/drop) attack':
        'Минимальный интервал атаки «судьбоносный клюв» (прыжок/падение) в тиках',
    'Minimum interval in ticks for the spin/egg-bomb attack':
        'Минимальный интервал атаки «вращение/яйцебомба» в тиках',
    'Minimum interval in ticks for the stampede/zombie spawn attack':
        'Минимальный интервал атаки «табун/призыв зомби» в тиках',
    'Minimum number of baby zombies spawned in the stampede':
        'Минимальное количество детских зомби, призываемых табуном',
    'Minimum speed for launched egg bombs': 'Минимальная скорость запускаемых яйцебомб',
    'Number of blocks above target the chicken will fly to':
        'На сколько блоков выше цели поднимется курица',
    'Number of chicken-riding strays which are spawned in':
        'Количество бродячих наездников, которых курица призывает',
    'Number of ticks after breaking the chicken\'s forcefield that it comes back up':
        'Количество тиков до восстановления силового поля курицы после его пробития',
    'Number of ticks the laser will set entities on fire for':
        'Количество тиков, в течение которых лазер поджигает сущности',
    'Server-specific configuration for Mighty Ender Chicken Rehatched':
        'Серверные настройки Mighty Ender Chicken Rehatched',
    'Set to 0 to disable despawn': 'Поставьте 0, чтобы отключить исчезновение',
    'Speed multiplier for the charge attack': 'Множитель скорости атаки рывком',
    'The radius of the fight arena: the chicken is spawned in the center of this arena,':
        'Радиус арены боя: курица появляется в её центре,',
    'Time in seconds after which the chicken will despawn if no players are within the arena':
        'Время в секундах, после которого курица исчезнет, если в арене нет игроков',
    'Warmup/warning time for the charge attack': 'Время подготовки/предупреждения для атаки рывком',
    'Warmup/warning time for the peck of doom attack':
        'Время подготовки/предупреждения для атаки «судьбоносный клюв»',

    # ─────────────── magic_coins ───────────────
    'Enable convert buttons on collect coins button':
        'Показывать кнопки обмена на кнопке сбора монет',
    'Enable crystal-for-gold button': 'Показывать кнопку «кристалл за золото»',
    'Enable gold-for-crystal button': 'Показывать кнопку «золото за кристалл»',
    'Enable gold-for-silver button': 'Показывать кнопку «золото за серебро»',
    'Enable silver-for-gold button': 'Показывать кнопку «серебро за золото»',
    'Should coins be lootable from chests': 'Должны ли монеты выпадать из сундуков',
    'Syncing server config to all players': 'Синхронизация серверных настроек со всеми игроками',
    'The value of a crystal coin': 'Номинал кристальной монеты',
    'The value of a gold coin': 'Номинал золотой монеты',
    'The value of a silver coin': 'Номинал серебряной монеты',
    'Tips: 80 for align for right side': 'Подсказка: 80 — выравнивание по правой стороне',
    'WARNING: When considering an economic system that operates only with integers, values are automatically rounded up. For example, a value of 1.5 is treated as 2.':
        'ВНИМАНИЕ: если экономическая система работает только с целыми числами, значения '
        'автоматически округляются вверх. Например, значение 1,5 считается как 2.',
    'X position for coin button': 'Позиция X для кнопки монет',
    'X position of crystal button': 'Позиция X кнопки кристалла',
    'X position of gold button': 'Позиция X кнопки золота',
    'X position of silver button': 'Позиция X кнопки серебра',
    'Y position for coin button': 'Позиция Y для кнопки монет',
    'Y position of crystal button': 'Позиция Y кнопки кристалла',
    'Y position of gold button': 'Позиция Y кнопки золота',
    'Y position of silver button': 'Позиция Y кнопки серебра',

    # ─────────────── bonsaitrees4 ───────────────
    'If a bonsai tree has been cut, but its drops do not fit into the output inventory, how many ticks to wait before trying again.':
        'Если бонсай срублен, но добыча не помещается в выходной инвентарь — сколько тиков ждать '
        'до следующей попытки.',
    'If true, indestructible tools (like those from Mystical Agriculture) can be used in bonsai pots.':
        'Если true, в горшки с бонсаем можно использовать неразрушимые инструменты '
        '(например, из Mystical Agriculture).',
    'Show all trees in JEI': 'Показывать все деревья в JEI',
    'Show chances in tooltips': 'Показывать шансы в подсказках',
    'Show rolls as count in JEI': 'Показывать броски как количество в JEI',
    'Show the tree model in the sapling tooltip': 'Показывать модель дерева в подсказке саженца',
    'Show unknown loot conditions in tooltips': 'Показывать неизвестные условия добычи в подсказках',
    'Show unused soil recipes in JEI': 'Показывать неиспользуемые рецепты почвы в JEI',
    'The amount of damage to deal to the tool when cutting a bonsai tree.':
        'Количество урона, наносимого инструменту при рубке бонсая.',
    'The base number of ticks it takes for a bonsai tree to grow.':
        'Базовое количество тиков, необходимое для роста бонсая.',
    'The chance that a tool will take damage when cutting a bonsai tree.':
        'Шанс, что инструмент получит урон при рубке бонсая.',
    'Use minimal quads for rendering': 'Использовать минимальное число полигонов при отрисовке',

    # ─────────────── trophymanager ───────────────
    'Allow fake players (machines) to get trophy drops':
        'Разрешать фейковым игрокам (машинам) получать трофеи',
    'Allow non opped players to change the settings for a trophy.':
        'Разрешать игрокам без прав оператора менять настройки трофея.',
    'Block to use for trophies dropped when killing a mob.':
        'Блок для трофеев, выпадающих при убийстве моба.',
    'Default YOffset for trophies dropped when killing a mob. If defaultBaseBlock is a full block this should be 1.0, slabs 0.5 and carpets 0.1':
        'Смещение по Y для трофеев, выпадающих при убийстве моба. Для полного блока — 1.0, '
        'для плиты — 0.5, для ковра — 0.1',
    'Default size for trophies dropped when killing a mob.':
        'Размер по умолчанию для трофеев, выпадающих при убийстве моба.',
    'Drop chance for trophies when a boss entity is killed by a player.':
        'Шанс выпадения трофея, когда игрок убивает босса.',
    'Drop chance for trophies when a normal entity is killed by a player.':
        'Шанс выпадения трофея, когда игрок убивает обычную сущность.',
    'Drop chance for trophies when a player is killed by a player.':
        'Шанс выпадения трофея, когда игрок убивает другого игрока.',
    'Looting enchant will increase drop change': 'Чары «Добыча» увеличивают шанс выпадения',
    'Maximum Y offset for a trophy.': 'Максимальное смещение по Y для трофея.',
    'Maximum size multiplier for a trophy.': 'Максимальный множитель размера для трофея.',

    # ─────────────── starbunclemania ───────────────
    'Enable the mob jar fluid\'s rendering': 'Включить отрисовку жидкости в банке существ',
    'Source cost to make a bucket of liquid source.':
        'Стоимость источника для создания ведра жидкого источника.',
    'Threshold of the fluid starbuncles, lower this if you need them to check and fill more often.':
        'Порог для жидкостных старбанклов. Снизьте его, если им нужно проверять и наполнять чаще.',
    'Threshold rate of the energy starbuncles, lower this if you need them to check and fill more often.':
        'Частота проверки для энергетических старбанклов. Снизьте её, если им нужно проверять и '
        'наполнять чаще.',
    'Threshold rate of the gas starbuncles, lower this if you need them to check and fill more often.':
        'Частота проверки для газовых старбанклов. Снизьте её, если им нужно проверять и наполнять чаще.',
    'Transfer rate of the energy starbuncles': 'Скорость передачи энергетических старбанклов',
    'Transfer rate of the fluid starbuncles': 'Скорость передачи жидкостных старбанклов',
    'Transfer rate of the gas starbuncles': 'Скорость передачи газовых старбанклов',
    'Value of milli-bucket of fluid converted in source by the sourcelink':
        'Количество жидкости в милливедре, которое источник sourcelink превращает в источник',

    # ─────────────── xycraft_core ───────────────
    'Display recipes that would otherwise be hidden to the player in a recipe viewer':
        'Показывать в просмотрщике рецептов те рецепты, которые иначе скрыты от игрока',
    'How fast a ui should change states each tick (example between two colors)':
        'Как быстро интерфейс должен менять состояния за тик (например, между двумя цветами)',
    'Play a ding and print a message upon successful reload':
        'Проигрывать звук и выводить сообщение об успешной перезагрузке',
    'Show item tags in the tooltip of an item, when using advanced tooltips (F3 + h).':
        'Показывать теги предметов в подсказке при использовании расширенных подсказок (F3 + H).',
    'The unit that should be displayed when measuring fluids. Buckets, Liters, and Units are interchangeable.':
        'Единица измерения, отображаемая при измерении жидкостей. Вёдра, литры и единицы взаимозаменяемы.',
    'To help isolate the number of blockstates each mod adds. Only turn this on if you are building a pack and wanting to help debug the amount of blocks your client has. (This runs at level join)':
        'Помогает отдельно посчитать, сколько состояний блоков добавляет каждый мод. Включайте, '
        'только если собираете сборку и хотите помочь с отладкой количества блоков у клиента. '
        '(Выполняется при входе в мир)',
    'Used primarily for debugging or pack creation. May not be ideal to have turned on when playing the game or publishing a pack.':
        'Используется в основном для отладки или создания сборок. Нежелательно включать при игре '
        'или публикации сборки.',
    'Whether or not XyCraft uses neo or internal energy handling with items. The intended way is to use internal.':
        'Использует ли XyCraft энергию NeoForge или собственную для предметов. Предпочтительный '
        'вариант — собственная.',

    # ─────────────── mekanisticrouters ───────────────
    'Base range for Chemical Module Mk2 (no range upgrades)':
        'Базовая дальность химического модуля Mk2 (без улучшений дальности)',
    'Energy cost (FE) to run one operation for the Chemical Module Mk1':
        'Расход энергии (FE) на одну операцию химического модуля Mk1',
    'Energy cost (FE) to run one operation for the Chemical Module Mk2':
        'Расход энергии (FE) на одну операцию химического модуля Mk2',
    'Energy cost (FE) to run one operation for the Chemical Refill Module':
        'Расход энергии (FE) на одну операцию химического модуля пополнения',
    'However, tanks placed inside the router will still respect their radiation check no matter what this config value is':
        'Однако баки внутри роутера всё равно будут проходить проверку радиации, каким бы ни было '
        'это значение',
    'Max range for Chemical Module Mk2': 'Максимальная дальность химического модуля Mk2',
    'Whether to allow routers to transfer irradiated gases.':
        'Разрешить роутерам переносить радиоактивные газы.',

    # ─────────────── buildinggadgets2 ───────────────
    'Base cost per block Paste (Copy is Free)':
        'Базовая стоимость вставки за блок (копирование бесплатно)',
    'Maximum distance you can build at': 'Максимальное расстояние строительства',
    'Maximum power for the Building Gadget': 'Максимальная мощность Строительного гаджета',
    'Maximum power for the Copy and Paste Gadget':
        'Максимальная мощность Гаджета копирования и вставки',
    'Maximum power for the Cut and Paste Gadget':
        'Максимальная мощность Гаджета вырезания и вставки',
    'Maximum power for the Destruction Gadget': 'Максимальная мощность Гаджета разрушения',
    'Maximum power for the Exchanging Gadget': 'Максимальная мощность Гаджета замены',

    # ─────────────── rep_ae2_bridge ───────────────
    'Enable aggressive debug logging for troubleshooting (includes network state dumps and reconnection logs)':
        'Включить подробный отладочный журнал для диагностики (включает дампы состояния сети '
        'и журнал переподключений)',
    'Enable the Replication NetworkBlockEntity mixin that wraps addElement() in onLoad()':
        'Включить миксин Replication NetworkBlockEntity, оборачивающий addElement() в onLoad()',
    'Enable the Titanium NetworkManager mixin that prevents \'Element network is null\' crashes':
        'Включить миксин Titanium NetworkManager, предотвращающий падение «Element network is null»',
    'Energy consumption rate (AE/t) for the RepAE2Bridge':
        'Скорость расхода энергии (AE/т) для RepAE2Bridge',
    'Only disable this if another mod provides the same fix or it causes conflicts.':
        'Отключайте, только если другой мод решает ту же проблему или возникают конфликты.',
    'Requires game restart to take effect.': 'Требуется перезапуск игры.',

    # ─────────────── idlecinematics ───────────────
    'Allow shots to feature nearby living entities':
        'Разрешить кадрам показывать живые сущности рядом',
    'Comma-separated namespaced cinematic preset identifiers disabled by the user':
        'Идентификаторы заблокированных пользователем пресетов через запятую, с указанием пространства имён',
    'How the shot director selects compositions':
        'Как режиссёр кадров выбирает композиции',
    'Keep rendering cinematic frames while Minecraft is inactive or unfocused':
        'Продолжать отрисовывать кинематографические кадры, пока Minecraft свёрнут или не в фокусе',
    'Seconds before choosing a new shot': 'Секунд до выбора нового кадра',
    'Show the selected shot pool and preset while cinematic mode is active':
        'Показывать выбранный набор кадров и пресет, пока активен кинематографический режим',

    # ─────────────── rgp-client ───────────────
    'Layout of the config screen': 'Оформление экрана настроек',
    'Layout of the title screen': 'Оформление главного меню',
    'The tenant to run API calls for': 'Идентификатор, для которого выполняются запросы API',
    'X position of the server button': 'Позиция X кнопки сервера',
    'Y position of the server button': 'Позиция Y кнопки сервера',

    # ─────────────── deus_ex_machina ───────────────
    'Default reset behavior for Attack Boost when a mob is killed by the player.':
        'Сброс «Усиления атаки» по умолчанию, когда игрок убивает моба.',
    'Default reset behavior for Resistance when a mob is killed by the player.':
        'Сброс «Сопротивления» по умолчанию, когда игрок убивает моба.',
    'Enable debug mode for additional logging.':
        'Включить режим отладки для расширенного журнала.',
    'FULL resets to minimum, PARTIAL reduces by the increase value, NONE keeps current value.':
        'FULL — сброс к минимуму, PARTIAL — уменьшение на значение прибавки, NONE — без изменений.',
    'Show Deus Ex Machina icon on the player\'s HUD when the effect is active.':
        'Показывать значок Deus Ex Machina на HUD игрока, пока эффект активен.',

    # ─────────────── xp_synthesiser ───────────────
    'Affects base cost, which applies equally to small and big recordings':
        'Влияет на базовую стоимость, одинаковую для малых и больших пластинок',
    'Affects scaling costs, which applies mostly to big recordings. Doubling this doubles scaling cost':
        'Влияет на масштабируемую стоимость, в основном для больших пластинок. Удвоение '
        'удваивает и её',
    'Kill Recorder necessary to run': 'Нужен Kill Recorder для запуска',
    'NOT ENOUGH POWER': 'НЕДОСТАТОЧНО ЭНЕРГИИ',
    'Whether or not the XP Synthesiser needs power': 'Нужен ли XP Synthesiser энергия',

    # ─────────────── ExtremeSoundMuffler ───────────────
    'Blacklisted Sounds - add the name of the sounds to blacklist, separated with comma':
        'Заблокированные звуки — добавьте названия через запятую',
    'Set to true to move the muffle and play buttons to the left side of the GUI':
        'Поставьте true, чтобы перенести кнопки заглушения и воспроизведения влево',
    'Volume set when pressed the mute button by default':
        'Громкость, которая устанавливается при нажатии кнопки заглушения',
    'Whether or not use the dark theme': 'Использовать ли тёмную тему',

    # ─────────────── ars_creo ───────────────
    'Base speed of the wheel': 'Базовая скорость колеса',
    'Speed of the wheel with a gold block in front': 'Скорость колеса с золотым блоком спереди',
    'Stress capacity of the wheel': 'Ёмкость натяжения колеса',

    # ─────────────── busy_villagers ───────────────
    'Make villagers work all day without schedule changes (No more "Meeting" or "Idle", overwrites preventVillagerSleep) (default: false)':
        'Заставить жителей работать весь день без смены расписания (больше никаких «Собрание» и '
        '«Покой», переопределяет preventVillagerSleep) (по умолчанию: false)',
    'Maximum number of restocks allowed per reset period (default: 2)':
        'Максимальное число пополнений за период сброса (по умолчанию: 2)',
    'Prevent villagers from sleeping at night (default: true)':
        'Запретить жителям спать ночью (по умолчанию: true)',

    # ─────────────── laserio ───────────────
    'Maximum FE/T for Energy Cards': 'Максимум FE/т для энергетических карточек',
    'Millibuckets for Chemical Cards without Overclockers installed (Only is Mekanism is installed)':
        'Милливедры для химических карточек без установленных разгонов (только если установлен Mekanism)',
    'Millibuckets for Fluid Cards without Overclockers installed':
        'Милливедры для карточек жидкостей без установленных разгонов',

    # ─────────────── ars_ocultas ───────────────
    'Allow empty Soul Gems to be used on filled Containment Jars to pickup the contained mob':
        'Разрешить пустым самоцветам души извлекать существо из заполненной содержательной банки',
    'Allow filled Soul Gems to be used on empty Containment Jars to place the mob into the jar':
        'Разрешить заполненным самоцветам души помещать существо в пустую содержательную банку',

    # ─────────────── mob_grinding_utils ───────────────
    'Fan blades are stronger, fan is only blocked by more solid blocks':
        'Лопасти вентилятора прочнее, вентилятор блокируется только более твёрдыми блоками',
    'Max upgrades for masher': 'Максимум улучшений измельчителя',

    # ─────────────── jumbofurnace ───────────────
    'Shearable: Allow jumbo furnaces to be cleanly dismantled with shears':
        'Разбираемый: разрешить аккуратно разбирать большие печи ножницами',

    # ─────────────── replication_rs2_bridge ───────────────
    'Use the side button': 'Используйте боковую кнопку',

    # ─────────────── BrandonsCore ───────────────
    'Allows you to disable the tpx command.':
        'Позволяет отключить команду tpx.',
    'Both the wings and Elytra will render on top of each other.':
        'И крылья, и элитра будут отрисованы друг поверх друга.',
    'Enable / Disable dark mode in my GUI\'s. (This can also be toggled in game from any gui that supports dark mode)':
        'Включить или отключить тёмную тему в моих меню. (Её также можно переключить в игре в '
        'любом меню, которое поддерживает тёмную тему)',
    'Extend and flap': 'Расправить и взмах',
    'Not supported by this tag': 'Этот тег не поддерживается',
    'Not sure why you would want this but its an option.':
        'Не уверен, зачем вам это, но такая возможность есть.',
    'The Elytra model will be disabled in favour of the wings.':
        'Модель элитра будет отключена в пользу крыльев.',
    'The original patreon badge': 'Изначальный значок Patreon',
    'The wings will replace the Elytra model.': 'Крылья заменят модель элитра.',
    'This badge is only given to long term supporters':
        'Этот значок получают только постоянные сторонники',
    'This is a permanent badge.': 'Этот значок выдаётся навсегда.',
    'Use the colour picker to configure,': 'Используйте палитру цветов для настройки',
    'What happens when you are not flying': 'Что происходит, когда вы не летите',
    'When using creative style flight': 'При полёте в стиле творческого режима',
    'Wings Colour A:': 'Цвет крыльев A:',
    'Wings Shader A:': 'Шейдер крыльев A:',
    'Wings will be hidden while wearing Elytra.':
        'Крылья будут скрыты, пока надеты элитра.',
    'Wings Colour A: ': 'Цвет крыльев A: ',
    'Wings Colour B: ': 'Цвет крыльев B: ',
    'Wings Shader A: ': 'Шейдер крыльев A: ',
    'Wings Shader B: ': 'Шейдер крыльев B: ',
    'Wings: ': 'Крылья: ',
    'Hide wings': 'Скрыть крылья',
    'Uses the right click block event to verify that players have permission to interact with BC / DE blocks.':
        'Использует событие ПКМ по блоку, чтобы проверить, есть ли у игрока право взаимодействовать '
        'с блоками BC / DE.',
    'This ensures there is no possible way a player can interact with a BC block if a protection system is blocking the interaction':
        'Это гарантирует, что игрок не сможет взаимодействовать с блоком BC, если системой защиты '
        'взаимодействие заблокировано',
    'In theory not even a modified client sending raw packets will be able to bypass this.':
        'Теоретически обойти это не сможет даже изменённый клиент, отправляющий сырые пакеты.',
    'I have added the ability to disable this feature because it seems in rare cases it blocks players who should have access and i have no idea why.':
        'Я добавил возможность отключить эту функцию, потому что в редких случаях она блокирует '
        'игроков, которым доступ положен, и я понятия не имею почему.',
}
