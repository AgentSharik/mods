---
navigation:
  parent: example-setups/example-setups-index.md
  title: Заполнитель вёдер
  icon: minecraft:water_bucket
---

# Заполнитель вёдер

См. также [Опустошитель вёдер](bucket-emptier.md).

Обратите внимание, что, поскольку здесь используется <ItemLink id="pattern_provider" />, это предназначено для интеграции в вашу схему [автоматического изготовления](../ae2-mechanics/autocrafting.md).

Иногда жизнь неудобна и вам нужны вёдра жидкости вместо самой жидкости. Иногда машина может сделать это за вас
(например, Передатчик жидкостей из Thermal Expansion), но не всегда у вас может быть мод, который делает это удобно. К счастью,
в ванильном Minecraft есть чуть менее удобный способ — <ItemLink id="minecraft:dispenser" />.

**Обратите внимание, что часто это не нужно делать, потому что Замены жидкостей в
[терминале кодирования шаблонов](../items-blocks-machines/terminals.md#pattern-encoding-terminal) позволяют использовать саму жидкость в
рецепте изготовления вместо ведра.**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/bucket_filler.snbt" />

<BoxAnnotation color="#dddddd" min="2 1 0" max="3 2 1">
        (1) Поставщик шаблонов: установлен на блокировку изготовления «При сигнале красного камня», с нужными шаблонами обработки.

        <Row>
        ![Шаблон заполнения](../assets/diagrams/water_fill_pattern_small.png)
        ![Шаблон заполнения](../assets/diagrams/lava_fill_pattern_small.png)
        </Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1.1 0.1" max="3.2 1.9 0.9">
        (2) Интерфейс: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.1 1.1 0.8" max="3.9 1.9 1">
        (3) Шина хранения №1: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="4.05 1.05 0.8" max="4.95 1.95 1">
        (4) Плоскость формирования: отфильтрована в чёрный список вёдер, с картой инверсии.
        <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.2 2 1.2" max="3.8 2.2 1.8">
        (5) Шина импорта: отфильтрована в чёрный список вёдер, с картой инверсии.
        <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2.1 2 0.1" max="2.9 2.2 0.9">
        (6) Шина хранения №2: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="0 1.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="225" pitch="45" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) установлен на блокировку изготовления «При сигнале красного камня», с нужными <ItemLink id="processing_pattern" />.
  
    ![Шаблон зарядника](../assets/diagrams/water_fill_pattern.png)
    ![Шаблон зарядника](../assets/diagrams/lava_fill_pattern.png)

* <ItemLink id="interface" /> (2) в своей конфигурации по умолчанию.
* Первая <ItemLink id="storage_bus" /> (3) в своей конфигурации по умолчанию.
* <ItemLink id="formation_plane" /> (4) отфильтрована в чёрный список вёдер, с картой инверсии.
  <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
* <ItemLink id="import_bus" /> (5) отфильтрована в чёрный список вёдер, с картой инверсии.
  <Row><ItemImage id="minecraft:bucket" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
* Вторая <ItemLink id="storage_bus" /> (6) в своей конфигурации по умолчанию.

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="interface" />.
   (На самом деле, в качестве оптимизации он помещает прямо через шину хранения и плоскость формирования, как если бы они были продолжениями граней поставщика. Предметы на самом деле никогда не входят в интерфейс.)
2. Через механизмы, описанные в [трубных подсетях](pipe-subnet.md#providing-to-multiple-places), и <ItemLink id="formation_plane" />
   ведро оказывается в <ItemLink id="minecraft:dispenser" />, а жидкость размещается плоскостью формирования.
3. <ItemLink id="minecraft:comparator" /> обнаруживает ведро в раздатчике и, таким образом, одновременно питает раздатчик и блокирует
   <ItemLink id="pattern_provider" />.
4. Раздатчик зачерпывает жидкость ведром, теперь у него заполненное ведро в себе.
5. <ItemLink id="import_bus" /> извлекает заполненное ведро из раздатчика и хранит его через
   <ItemLink id="storage_bus" /> в поставщике шаблонов, возвращая в главную сеть.
6. Компаратор видит, что раздатчик пуст, и разблокирует поставщика.
