# CarbonConfig 2.1 — патч классов плагина JEI

## Зачем

Экран настроек JEI в сборке рисует не Configured (его в сборке нет), а
**Carbon Config**: в нём живёт плагин `carbonconfiglib/plugins/jei`,
который регистрирует фабрику экрана для JEI. Расследование:

- JEI 19.54.429 сам экрана не имеет — `mezz/jei/neoforge/platform/ConfigHelper`
  зовёт `IConfigScreenFactory.getForMod("jei")`, а в его `en_us.json` прямо
  написано: `jei.jei.configured` = «Install the "Configured" mod to
  access the in-game config».
- ConfiguredDefaults из папки модов — другой мод (Fuzs, 43 КБ), экранов не рисует.
- CarbonConfig берёт значения JEI через `IJeiConfigManager` и рисует их своим
  экраном. Подписи настроек при этом локализованы JEI (`getLocalizedName()`),
  но два места оставались английскими литералами:
  - **имена категорий** — `JEINode.getName()` зовёт
    `IConfigNode.createLabel(category.getName())`, который собирает литерал
    из сырого имени (`ingredientList` → «Ingredient List»);
  - **значения enum** — `JEIHelpers.getSuggestions()` создаёт
    `Suggestion.value(serialize(enum))`, а экран показывает
    `Component.literal(suggestion.getName())` — имя константы как есть.

## Что сделано

Байткод-патч скриптом `scripts/patch_carbon_jei.py` (JDK в песочнице нет,
поэтому constant pool и тела методов переписываются питоном):

- **JEIHelpers** — добавлены два метода:
  - `localizeEnumSuggestions(List, Component, DataType)`: для enum-значений
    достаёт ключ перевода из компонента имени настройки
    (`TranslatableContents.getKey()`), строит ключ
    `<ключ>.<enum_lowercase>` (та же схема, что 61 ключ, добавленный в JEI)
    и создаёт `Suggestion.namedTypeValue(перевод, сыроеЗначение, тип)` —
    `Component.translatableWithFallback`, при отсутствии ключа показывается
    сырое значение. Сериализованное значение (`value`) не изменяется,
    сохранение настроек не страдает.
  - `jeiCategoryLabel(String)`: пробует `I18n.exists` по префиксам
    `jei.config.client.` / `colors.` / `debug.` / `modIdFormat.`,
    при неудаче — прежний `createLabel`.
- **JEIValue.getSuggestions / JEIArray.getSuggestions** — проксируют через
  `localizeEnumSuggestions(suggestions.get(), name, type)`.
- **JEINode.getName** — проксирует через `jeiCategoryLabel(category.getName())`.

Все остальные методы, поля и атрибуты классов байт-в-байт как в оригинале
(проверено попарным сравнением), constant pool только дописан в конец.
Корректность StackMapTable новых методов сверена ручным разбором кадров.

## Перевод

- Существующий `ru_ru.json` (112 ключей интерфейса) — вычитан, правок не потребовал.
- Добавлены 68 отсутствовавших ключей `legacy.gamerule.*` (встроенный экран
  игровых правил Carbon): категории, названия и описания правил.
  Итого 180/180 ключей.

## Сборка и проверка

- JAR пересобран из исходника `CarbonConfig-Neoforge-1.21.1-2.1.jar`:
  заменены только `assets/carbonconfig/lang/ru_ru.json` и 4 класса выше;
  вложенный `Primitive Collections-0.8.9.jar` не тронут; `testzip` чист.
- sha256 готового JAR: `90e5891eb072cfee9fa339e27ce8f11e55cfb1d6633064731ce722ac56a23323`
- В игре не проверялось (нет запуска): ожидается подтверждение, что категории
  и значения enum в экране JEI стали русскими.

Известный остаток: в подсказке настройки JEILeaf дописывает литерал
`serializer.getValidValuesDescription()` (список допустимых значений вида
«RECTANGULAR, MAXIMIZE_AVAILABLE_SPACE») — это технические значения файла
конфига, оставлены как есть.

## Воспроизведение

```bash
python3 scripts/patch_carbon_jei.py "Исходные моды для перевода/CarbonConfig-Neoforge-1.21.1-2.0.2.1.jar" /tmp/carbon_patch
# затем заменить в копии исходного JAR ru_ru.json (снапшот ru_work.json)
# и 4 класса из каталога патча
```
