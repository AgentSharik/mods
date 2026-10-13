---
navigation:
  parent: example-setups/example-setups-index.md
  title: Схема рекурсивного изготовления
  icon: minecraft:netherite_upgrade_smithing_template
---

# Схема рекурсивного изготовления

Как сказано в [автоматическом изготовлении](../ae2-mechanics/autocrafting.md), алгоритм планирования автоматического изготовления не может обрабатывать рецепты, где
главный вывод является одним из входов. Например, он не может обрабатывать клонирование <ItemLink id="minecraft:netherite_upgrade_smithing_template" />.

Одно решение — использование способности <ItemLink id="level_emitter" /> притворяться [шаблоном](../items-blocks-machines/patterns.md).

Затем это будет использоваться, чтобы включить маленькую схему, которая постоянно выполняет изготовление. В данном случае мы посмотрим на схему
для клонирования <ItemLink id="minecraft:netherite_upgrade_smithing_template" />.

<RecipeFor id="minecraft:netherite_upgrade_smithing_template" />

***

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/recursive_recipe_setup.snbt" />

  <BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (1) Интерфейс: установлен хранить требуемые дополнительные ингредиенты: алмаз и незерак.
        <Row><ItemImage id="minecraft:diamond" scale="2" /> <ItemImage id="minecraft:netherrack" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.3 1 0.3" max="2.7 1.3 0.7">
        (2) Излучатель уровня: настроен на «незеритовое улучшение для кузнечного стола», установлен на «Излучать красный камень для изготовления предмета».
        <Row><ItemImage id="minecraft:netherite_upgrade_smithing_template" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 0 0" max="2.3 1 1">
        (3) Шина импорта №1: отфильтрована на предметы, которые Интерфейс хранит. У неё есть Карта красного камня. Режим красного камня установлен на
        «Активен при сигнале».
        <Row>
        <ItemImage id="minecraft:diamond" scale="2" />
        <ItemImage id="minecraft:netherrack" scale="2" />
        <ItemImage id="redstone_card" scale="2" />
        </Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 1" max="4 1.3 2">
        (4) Шина хранения №1: установлен более высокий приоритет, чем у другой шины хранения. ОЧЕНЬ ВАЖНО.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 0 1" max="4 1 2">
        (5) Молекулярный сборщик: в нём есть шаблон для дублирования кузнечного шаблона.

        ![Шаблон](../assets/diagrams/smithing_template_pattern_small.png)

        В нём также уже есть один кузнечный шаблон, вставленный вручную при первой постройке этого.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 0 1" max="3 1 2">
        (6) Шина импорта №2: в своей конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 1" max="2 1 1.3">
        (7) Шина хранения №2: отфильтрована на «незеритовое улучшение для кузнечного стола». Установлен более низкий приоритет, чем у другой шины хранения.
        <ItemImage id="minecraft:netherite_upgrade_smithing_template" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="15" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="interface" /> (1) установлен хранить требуемые дополнительные ингредиенты: алмаз и незерак.
* <ItemLink id="level_emitter" /> (2) настроен на «незеритовое улучшение для кузнечного стола» и установлен на «Излучать красный камень для изготовления предмета».
* Первая <ItemLink id="import_bus" /> (3) отфильтрована на предметы, которые Интерфейс хранит. У неё есть Карта красного камня. Режим красного камня установлен на «Активен при сигнале».
* Первая <ItemLink id="storage_bus" /> (4) установлена на *более высокий* [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чем вторая шина хранения.
* <ItemLink id="molecular_assembler" /> (5) имеет шаблон для дублирования кузнечного шаблона и один кузнечный шаблон, уже вставленный вручную.

  ![Шаблон](../assets/diagrams/smithing_template_pattern.png)

* Вторая <ItemLink id="import_bus" /> (6) в своей конфигурации по умолчанию.
* Вторая <ItemLink id="storage_bus" /> (7) отфильтрована на «незеритовое улучшение для кузнечного стола». У неё *более низкий* [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чем у первой шины хранения.

## Как это работает

1. <ItemLink id="level_emitter" /> притворяется [шаблоном](../items-blocks-machines/patterns.md) из-за вставленной
   <ItemLink id="crafting_card" /> и установки «Излучать красный камень для изготовления предмета». Таким образом, «незеритовое улучшение для кузнечного стола» появляется в
   [терминалах](../items-blocks-machines/terminals.md) как допустимая вещь для [автоматического изготовления](../ae2-mechanics/autocrafting.md).
2. При получении запроса на изготовление этого предмета, либо от игрока, либо от самой системы, излучатель уровня включается.
3. Первая <ItemLink id="import_bus" /> активируется излучателем уровня и извлекает ингредиенты, хранимые в <ItemLink id="interface" />.
4. Единственная <ItemLink id="storage_bus" /> в сети, способная хранить эти ингредиенты, — та, что на сборщике.
5. <ItemLink id="molecular_assembler" /> получает ингредиенты (уже имея 1 кузнечный шаблон внутри) и выполняет изготовление, производя 2 кузнечных шаблона.
6. Вторая <ItemLink id="import_bus" /> извлекает 1 кузнечный шаблон.
7. У первой шины хранения более высокий приоритет, поэтому этот кузнечный шаблон возвращается в сборщик.
8. Вторая <ItemLink id="import_bus" /> извлекает 1 кузнечный шаблон.
9. Сборщик не может получить ещё один кузнечный шаблон, поэтому второй кузнечный шаблон идёт в шину хранения с более низким приоритетом, вставляя его в интерфейс.
10. <ItemLink id="interface" />, не будучи установленным хранить кузнечные шаблоны, вставляет его в сеть.
