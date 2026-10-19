# Журнал 2026-10-16: полный аудит всех модов (кроме CarbonConfig)

Запрос пользователя: пересмотреть все 209 JAR, не пропущено ли что-то ещё.

## Методика
Скриптом проверены: покрытие lang-ключей en→ru; книги (Patchouli/GuideME/Oracle/Modonomicon) — зеркала ru_ru и литеральный текст против ключей; литеральные тексты advancements в data; строки GUI-классов (Screen/Gui/Tooltip/Widget и т.п.); описания в mods.toml.

## Чисто (подтверждено)
- Отсутствующих lang-ключей: 0 по всем модам. Литеральных EN-текстов в advancements: 0.
- Ложные тревоги аудита: книги Ars Nouveau, Psi (Patchouli), Irregular Implements, Occultism, applied/refined delight, starbunclemania — тексты идут lang-ключами либо зеркала ru_ru полны; у ряда исходников en_us уже содержит русский (поэтому «значение = EN» не сигнал).

## Найдено и исправлено
- AdvancedPeripherals: книга Patchouli была полностью английской (литералы) — переведены 69 полей в 24 файлах, создано зеркало data/.../manual/ru_ru, переведён landing_text в book.json.
- mods.toml: английские описания в списке модов у 19 JAR (AE2, GuideME, Jade, FarmersDelight, ImmersiveEngineering, Bookshelf, Cable Tiers, Modular Routers и др.) — заменены русскими.
- Захардкоженные GUI-строки (124 патча, 12 JAR): xp_synthesiser, applieddelight, refineddelight (экраны котлов), rftoolsbase/builder/utility/power (подсказки и помощь), compactmachines, fdbosses (реплика босса), create-dragons-plus, supplementaries (экран-ошибка), ars_affinity (остаток «not unlocked»).

## Релиз
v1.1.4 перевыпущен на месте (run 38862523349), заметки дополнены. Новой версии не делалось — это правки к текущему релизу.

## Глубокий проход (второй запрос пользователя: «GUI, меню, книги, предметы, описания»)

Методика: §-строки во ВСЕХ классах всех JAR; GuideME/Markdown-книги (.md); Modonomicon; литералы в data-JSON; Patchouli в data; проверка зеркал ru_ru у книг.

### Подтвердилось чисто (ложные тревоги сканера)
- Все книги GuideME имеют полные зеркала `_ru_ru`: Powah 28/28, JustDynaThings 35/35, Energy Meter 2/2, Replication 1/1.
- Modonomicon-книг без ru-зеркала: 0.
- §-строки «провайдеров» (Occultism pentacle errors, BetterBlockZ, Twilight Forest notebook, Modonomicon warning, JustDynaThings DataLang) — datagen-генераторы en_us; все значения есть в lang и переведены в ru_ru (выборочно сверено по значениям).
- Data-JSON «предложения» у Cataclysm, FTB, Iron's, Mekanism, Supplementaries, CompactMachines (datapack-lang имеет ru-зеркало) — комментарии разработчиков, тестовые данные, имена песен; в игре не отображаются.
- Экран титров Supplementaries (§4Author и т.п.) — список имён авторов/спонсоров, сознательно не тронут.

### Найдено и исправлено (глубокий проход)
- EnderDrives: 59 литералов в классах — вся обратная связь команд `/enderdrives` (статистика, проверка лент, автобенчмарк), строка JEI о типах дисков, подсказка TapeDiskItem, заголовки ModCommands. Коды § и плейсхолдеры сохранены.
- Moonlight: заголовок экрана «§3Moonlight Configured» → «§3Настройки Moonlight».
- Ars Nouveau + Ars Elemental: переведены `flavour_text` предустановленных томов заклинаний сообщества (49 + 12) в data-рецептах — отображаются в JEI/подсказках томов. Имена томов оставлены как авторские имена собственные.
- Условия: ru_ru.json всех четырёх JAR после правок байтно сверены с HEAD (2667/832/228/265 ключей — идентичны), testzip=None, EN-оригиналов не осталось.

### Инцидент при сборке (исправлен до коммита)
Первая сборка ошибочно велась из исходных JAR и затирала ru_ru сокращёнными авторскими локалями; замечено падением счётчика ключей 70913→69143. JAR восстановлены из HEAD, патчи наложены поверх проектных JAR, счётчик вернулся к 70913. Урок: патчи классов/JSON накладывать ТОЛЬКО поверх текущего проектного JAR, никогда не пересобирать из исходника без повторного наложения lang.

## Контрольный проход (третий запрос пользователя: «точно все ГУИ и меню?»)

Методика точнее прежней: (A) все значения ru_ru, совпадающие с en_us или содержащие английскую прозу (119 кандидатов → триаж); (B) байткод-анализ: строковые константы, реально уходящие в `Component.literal(...)`/`drawString` — 469 кандидатов по 84 JAR, отсев ключей lang и dev-исключений вручную по каждому JAR.

Исправлено (58 JAR, коммиты «Контрольный проход: ...»):
- BetterBlockZ — 37 литералов (режимы всех инструментов, экран выбора варианта);
- Create — 20 (предупреждение о графике «Потрясающая», команды поездов/сцепок);
- демо-книга и демо-буклет Modonomicon — 130 записей lang (предмет книги существует и открывается);
- Modular Routers — 12 значений lang (полностью или частично английские: энергия, фильтр слияния, всплывающие подсказки активатора, принудительное опустошение);
- FTB Quests — подсказки редактора; FTB Chunks — срезы; FTB Library — редактор NBT и тестовые экраны; FTB Team Bases/Echoes/Backups/Pack Companion/Materials — сообщения команд;
- Relics, XyCraft Machines/Core, MooFluids (Jade), Applied/Refined Delight (статусы сети), Occult Engineering, Compact Machines, Advanced Peripherals, Replication (+RS Bridge), AE2 и AE2-Things, Chisel, Apotheosis, Iron's, Ars Nouveau (JEI «Any compatible item»), Ars Elemental и FD Bosses (английские хвосты в русских значениях), JEI (дверь), Apothic Attributes (кнопка), GuideME (заголовок поиска), PetRock (шутки в чате), Supplementaries, EnderMeter, Refined Storage (Край/Незер), Routers, Simple Teleporters («Нажмите ПКМ»), XP Synthesiser, Gaze, Mekanism Lasers, Immersive Engineering (пасхалки), Chance Cubes, SimplyTooltips, Pocket Storage, Simple Tomb, SFM, OctoLib, ResourcefulLib, Irregular Implements, Irons Apothic, Ars Affinity, Reliquified TF.

Остаток после правок (сознательно, по всему пакету единообразно): названия песен на пластинках и бренды вкладок креатива («Create: Dragons Plus» и т.п.); служебные `__`-ключи и превью шрифтов Bookshelf; комментарии помощи в lang Industrial Foregoing; текст команды `/create dismissFabulousWarning` и названия клавиш [Page Up]/[Page Down] в подсказках; dev-команды GuideME StructureCommands и название источника данных Iron's; имена собственные томов Ars и шуток-«системок» PetRock (имя manmaed сохранено).

Финальные сканы после правок: A — только перечисленные выше осознанные пропуски; экстрактор отображаемой прозы по пропатченным JAR — 0 (кроме перечисленных).

## Оговорки
Строки GUI-классов отбирались эвристикой (имена классов + отсутствие маркеров логов); дев-ошибки вида «X must not be null» сознательно не переводились. Комментарии конфигов NeoForge (.toml/классы) не переводились ни у одного мода — они видны только в файлах конфигов и редакторе Configured; это единый осознанный пропуск по всему пакету. Экран титров Supplementaries — список имён. Текст, генерируемый на лету (имена инвейдеров Apotheosis, динамические названия, текст от серверных плагинов/KubeJS вне lang) статически не покрывается. Запуск в игре не выполнялся.
