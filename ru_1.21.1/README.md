# Русифицированные моды — Minecraft 1.21.1 (NeoForge)

Полностью переведённые на русский сборки. Русский сделан основным языком:
в каждом моде `ru_ru.json` содержит перевод, а `en_us.json` продублирован
русским (это фолбэк для любого языка игры), прочие языки удалены.

Переведено не только содержимое предметов, но и **встроенная документация** —
справочник Oracle Index в SimplySwords и гайдбук SimpleTeleporters,
которые видны прямо в игре.

| Мод | modid | Строк lang | Из них на русском | Файлов док-ции |
|---|---|---:|---:|---:|
| SimpleDiscordRichPresence-neoforge-88.0.1-build.54+mc1.21.1.jar | `sdrp` | 3 | 3 | — |
| SimpleTeleportersReforged-1.21.1-2.2.0.jar | `simpleteleporters` | 43 | 42 | 5 |
| SimplyTooltips-neoforge-0.1.5.jar | `simplytooltips` | 31 | 29 | — |
| simplemagnets-1.1.12c-neoforge-mc1.21.jar | `simplemagnets` | 38 | 36 | — |
| simpletomb-1.21.1-1.4.4.jar | `simpletomb` | 84 | 83 | — |
| simplylight-1.5.3+1.21.1-b4.jar | `simplylight` | 192 | 190 | — |
| simplyswords-neoforge-1.70.2-1.21.1.jar | `simplyswords` | 2267 | 2253 | 153 |
| **Итого** | | **2658** | **2636** | **158** |

## Что переведено

* **2658** строк lang-файлов — названия предметов, тултипы, конфиги, названия
  рунных и незерных сил, параметры урона.
* **158** файлов встроенной документации (~50 000 символов):
  * `simplyswords` — 146 страниц Oracle Index + 7 файлов меню;
  * `simpleteleporters` — 5 страниц гайдбука.

## Технические детали

* Идентификаторы ресурсов не менялись: `icon:`, `id=`, `location=`, `parent:`,
  `item_ids:`, `slots={...}` — это ключи, по ним мод ищет ассеты.
* Кириллица корректна: шрифт SimplySwords (`assets/minecraft/font/default.json`)
  дополнен ванильными провайдерами, включая `unifont`, поэтому русский текст
  отображается правильно.
* `sinytra-wiki.json` и прочие служебные файлы оставлены без изменений.
