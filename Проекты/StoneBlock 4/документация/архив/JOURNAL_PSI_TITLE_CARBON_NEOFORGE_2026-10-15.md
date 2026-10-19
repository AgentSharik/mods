# Журнал: заголовок книги Psi в Oracle Index и подписи NeoForge в CarbonConfig (2026-10-15)

## Жалобы (скриншоты пользователя)
1. В Oracle Index книга Psi открывается, страницы русские, но заголовок
   книги в шапке — сырой ключ `oracle_index.title.psi`.
2. Экран CarbonConfig -> NeoForge -> neoforge-client: все подписи пунктов
   английские («Experimental Forge Light Pipeline Enabled» и др.).

## Диагноз
1. Oracle Index берёт имя книги из ключа `oracle_index.title.<id>` (id из
   `sinytra-wiki.json`). Для вставленных нами книг ключи нашлись у всех,
   кроме Psi (свип по всем 182 JAR: книги irregular-implements, mffs,
   oracle-index, oritech, simplyswords — OK; psi — отсутствует).
2. NeoForge — не мод из папки mods, а загрузчик: его конфиги
   (neoforge-client/common/server.toml, исходник NeoForgeConfig ветки
   1.21.1 сверен) не покрывались генерацией `<modid>.configuration.*`,
   поэтому патч CarbonConfig падал в «очеловечивание» имён TOML.

## Исправления
- `oracle_index-neoforge-1.4.0.jar`: в en_us и ru_ru добавлен
  `oracle_index.title.psi` = "Encyclopedia Psionica" / «Энциклопедия
  Псионики» (имя — по `psi.book.name`). Снапшот `_oracle_index_wip/ru_work.json`
  обновлён (47 ключей).
- `CarbonConfig-Neoforge-1.21.1-2.0.2.1.jar`: в ru_ru добавлено 44 ключа
  для всех 14 пунктов NeoForgeConfig (client 4, common 5, server 5):
  подписи `neoforge.configuration.<пункт>` (их читает патч CarbonConfig)
  + пары `neoforge.configgui.<ключ>` и `.tooltip` (штатный экран
  NeoForge; у experimentalForgeLightPipelineEnabled ключ перевода —
  forgeLightPipelineEnabled) + 2 `.tooltip.warning`. Итого 224 ключа,
  снапшот `_carbonconfig_wip/ru_work.json` обновлён.
- Счётчики пересчитаны скриптом: 68 454 -> 68 499 ключей (README и
  RELEASE_CONTENTS в синхроне, --check чистый).

## Остаётся английским (честно)
- Описания пунктов NeoForge в окне CarbonConfig: CarbonConfig берёт их
  из комментариев в классах самого NeoForge (библиотека загрузчика, не
  мод из сборки) — патчить нечем со стороны пакета модов. Подписи —
  то, что видно списком, — теперь русские.
- Значения enum (SILENCED, DEV_SHORT) — технические значения файла
  конфига, не переводятся.

## Проверка
- В обоих JAR изменены только целевые lang-записи (посписковое
  сравнение с HEAD), testzip чистый, JSON валиден.
- Запуска в игре не было; нужна проверка пользователем: шапка книги
  Psi в Oracle Index и список NeoForge -> neoforge-client.
