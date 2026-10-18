# Журнал: английский в Ars Elemental (jarjar Sauce) + кнопки-гравировка (2026-10-26)

## Жалоба
1. Ars Elemental: красная строка тултипа «Model and Texture still work in progress...» и английские имена стихийных атрибутов (+15% Earth Glyph Cost Reduction и т.п.) на броне.
2. Кнопки НАСТРОЙКИ/МОДЫ выглядят плохо; образцы пользователя — высеченные в камне буквы, hover — фиолетовое свечение.

## Диагноз
- Атрибуты — ключи `sauce.perk.*` библиотеки Sauce, зашитой jar-in-jar в ars_elemental (`META-INF/jarjar/sauce-1.21.1-0.0.61.100.jar`). В Sauce нет ru_ru.json (97 ключей) — переводческий конвейер вложенные JAR не обрабатывал; игра показывала en fallback.
- Красная строка — `Component.literal(...)` в `HeavyArmorE.appendHoverText` и `LightArmorE.appendHoverText` (хардкод, не lang-ключ).

## Исправления
- Добавлен полный ru_ru.json в Sauce во всех носителях: ars_elemental (0.0.61.100, 97 ключей), starbunclemania (0.0.40.82, 74), ars_hex (0.0.16.46, 55), ars_affinity (0.0.10.35, 54).
- Заодно переведены остальные jarjar-библиотеки без русского: nuggets в ars_nouveau (25), flywheel в create/mekanism_ponders/productivemetalworks (12), productivelib (27), ae2wtlib_api в ExtendedAE (5), better-advanced-tooltips в kubejs (5), mezz_config (18), olympus в tempad (11).
- Классы HeavyArmorE/LightArmorE: UTF8-константа заменена на «Модель и текстура ещё в разработке — пока как у среднего варианта.»
- Пересборка внешних JAR — byte-preserving по центральному каталогу (w14/patch_jarjar.py): верифицировано, что все нетронутые записи байт-идентичны оригиналам, CRC валидны, паритет ru=en полон во всех вложенных.

## Кнопки
- options/mods (normal+hover) перерисованы гравировкой (w14/engrave.py): тёмная выемка букв с сохранением текстуры камня, светлая нижняя кромка, на hover — фиолетовое свечение и лавандовая кромка; Й — бреве с разделительной строкой. Остальные кнопки — v2 без изменений.

## Проверка в игре (за пользователем)
- Тултип Шлема Стража чаши: атрибуты и строка WIP по-русски.
- Кнопки НАСТРОЙКИ/МОДЫ в главном меню.
