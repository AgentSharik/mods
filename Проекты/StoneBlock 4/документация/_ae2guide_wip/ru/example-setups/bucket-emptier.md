---
navigation:
  parent: example-setups/example-setups-index.md
  title: Опустошитель вёдер
  icon: minecraft:bucket
---

# Опустошитель вёдер

См. также [Заполнитель вёдер](bucket-filler.md).

Обратите внимание, что, поскольку здесь используется <ItemLink id="pattern_provider" />, это предназначено для интеграции в вашу схему [автоматического изготовления](../ae2-mechanics/autocrafting.md).

Иногда жизнь неудобна и вам нужна сама жидкость, но сделать жидкость вы можете только в ведре. Иногда машина может сделать это за вас
(например, Передатчик жидкостей из Thermal Expansion), но не всегда у вас может быть мод, который делает это удобно. К счастью,
в ванильном Minecraft есть чуть менее удобный способ — <ItemLink id="minecraft:dispenser" />.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/bucket_emptier.snbt" />

<BoxAnnotation color="#dddddd" min="2 1 0" max="3 2 1">
        (1) Поставщик шаблонов: установлен на блокировку изготовления «При сигнале красного камня» и включён режим блокировки, с нужными шаблонами обработки.

        <Row>
        ![Шаблон заполнения](../assets/diagrams/water_empty_pattern_small.png)
        ![Шаблон заполнения](../assets/diagrams/lava_empty_pattern_small.png)
        </Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2.1 2 0.1" max="2.9 2.2 0.9">
        (2) Интерфейс: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.1 2 1.1" max="3.9 2.2 1.9">
        (3) Шина хранения №1: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="4.05 1.05 0.8" max="4.95 1.95 1">
        (4) Плоскость уничтожения: интерфейса для настройки нет.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.2 1.2 0.8" max="3.8 1.8 1">
        (5) Шина импорта: отфильтрована на вёдра.
        <ItemImage id="minecraft:bucket" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1.1 0.1" max="3.2 1.9 0.9">
        (6) Шина хранения №2: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="0 1.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="225" pitch="45" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) установлен на блокировку изготовления «При сигнале красного камня» и включён режим блокировки,
  с нужными <ItemLink id="processing_pattern" />.
  
    ![Шаблон зарядника](../assets/diagrams/water_empty_pattern.png)
    ![Шаблон зарядника](../assets/diagrams/lava_empty_pattern.png)

* <ItemLink id="interface" /> (2) в своей конфигурации по умолчанию.
* Первая <ItemLink id="storage_bus" /> (3) в своей конфигурации по умолчанию.
* <ItemLink id="annihilation_plane" /> (4) не имеет интерфейса и не может настраиваться.
* <ItemLink id="import_bus" /> (5) отфильтрована на вёдра.
  <ItemImage id="minecraft:bucket" scale="2" />
* Вторая <ItemLink id="storage_bus" /> (6) в своей конфигурации по умолчанию.

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="interface" />.
   (На самом деле, в качестве оптимизации он помещает прямо через шину хранения, как если бы она была продолжением граней поставщика. Предметы на самом деле никогда не входят в интерфейс.)
2. Через механизмы, описанные в [трубных подсетях](pipe-subnet.md#providing-to-multiple-places),
   ведро оказывается в <ItemLink id="minecraft:dispenser" />.
3. <ItemLink id="minecraft:comparator" /> обнаруживает ведро в раздатчике и, таким образом, одновременно питает раздатчик и блокирует
   <ItemLink id="pattern_provider" />.
4. Раздатчик выливает жидкость из ведра, теперь у него пустое ведро в себе.
5. <ItemLink id="import_bus" /> извлекает пустое ведро из раздатчика и хранит его через
   <ItemLink id="storage_bus" /> в поставщике шаблонов, возвращая в главную сеть.
6. Компаратор видит, что раздатчик пуст, и разблокирует поставщика.
