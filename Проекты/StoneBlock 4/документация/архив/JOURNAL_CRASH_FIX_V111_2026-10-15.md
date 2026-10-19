# Крэш запуска v1.1.0 и фикс v1.1.1 (2026-10-15)

## Симптом
Игра падала на загрузке: `Rendering overlay` →
`java.util.NoSuchElementException` в
`dev.ftb.mods.ftbquests.quest.QuestShape.reload` ← `ThemeLoader.loadTheme`.

## Корневая причина (не Psi и не квесты)
Цепочка по latest.log от 15:58:

1. `L_Ender's Cataclysm`: `NoSuchFieldError: CommonConfig$RespawnerConfig
   does not have member field 'Respawner'` в `CMCommonConfig.Commonbake`
   при загрузке конфига → FML: «Failed to wait for future Config loading,
   1 errors found», ModLoader переходит в broken state и перестаёт
   рассылать события («Cowardly refusing to send event...»), ресурсы
   модов не подключаются.
2. Из-за этого `ThemeLoader` FTB Quests не находит ни одного ресурса
   форм квестов (`textures/shapes/*/background.png`), список пуст,
   `MAP.values().iterator().next()` бросает NoSuchElementException.
3. Поле пропало из-за нашего патча конфиг-классов Cataclysm: замена
   UTF8-строки в constant pool переименовала одновременно литерал
   `define("Respawner")` и **имя поля** `Respawner` → «Возрождатель».

## Аудит всех JAR
Скриптом распарсены constant pool всех изменённых классов (182 JAR):
переименованные имена полей/методов нашлись только в 2 классах —
Cataclysm `CommonConfig$RespawnerConfig` и CrashUtilities
`DataListGUI` (ENTITIES/TICKETS). Но у 151 конфиг-класса в 70 JAR тем же
патчем переименованы ключи конфигов в TOML (русские ключи → сброс
настроек сборки, предупреждения «Configuration file ... is not correct.
Correcting» в логе).

## Фикс
Возвращены оригинальные байты 162 классов в 71 JAR (все RENAME-RU
конфиг-классы + DataListGUI). Проверка: аудит переименований — 0;
Cataclysm отличается от оригинала только `ru_ru.json`; все JAR —
валидные ZIP. Сохранены: кодовый фикс CarbonConfig, фикс Apotheosis,
переводы отображаемых строк (CrashUtilities GUI/команды, SimplyTooltips,
обработчики apothic_tooltip_cleanup), подписи конфигов через lang-ключи
(`*.configuration.*` в ru_ru.json — на месте).

## Пользователю при обновлении
Заменить моды из архива v1.1.1 и восстановить папку `config` из чистой
сборки StoneBlock 4 (битые JAR успели переписать часть конфигов
русскими ключами со значениями по умолчанию).
