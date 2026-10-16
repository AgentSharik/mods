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

## Оговорки
Строки GUI-классов отбирались эвристикой (имена классов + отсутствие маркеров логов); дев-ошибки вида «X must not be null» сознательно не переводились. Запуск в игре не выполнялся.
