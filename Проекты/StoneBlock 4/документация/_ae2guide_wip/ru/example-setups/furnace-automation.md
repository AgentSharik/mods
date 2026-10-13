---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация печи
  icon: minecraft:furnace
---

# Автоматизация печи

Обратите внимание, что, поскольку здесь используется <ItemLink id="pattern_provider" />, это предназначено для интеграции в вашу схему [автоматического изготовления](../ae2-mechanics/autocrafting.md).
Если вы просто хотите автоматизировать печь отдельно, используйте воронки, сундуки и прочее.

Автоматизация <ItemLink id="minecraft:furnace" /> немного сложнее автоматизации более простых машин, например [зарядника](../example-setups/charger-automation.md).
Печь требует ввод с двух отдельных сторон и извлечение с третьей. Предмет для плавки должен помещаться в верхнюю грань,
топливо должно помещаться в боковую грань, а результат нужно извлекать из нижней. 

Это можно сделать через <ItemLink id="pattern_provider" />
сверху, <ItemLink id="export_bus" /> сбоку, чтобы постоянно помещать топливо, и <ItemLink id="import_bus" /> на
низе, чтобы импортировать результаты в сеть. Однако это использует 3 [канала](../ae2-mechanics/channels.md).

Вот как можно сделать это с всего 1 каналом:

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/furnace_automation.snbt" />

<BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (1) Поставщик шаблонов: направленный вариант, созданный гаечным ключом из истинного кварца, с нужными шаблонами обработки.

        ![Шаблон железа](../assets/diagrams/furnace_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (2) Интерфейс: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="1.3 2 1">
        (3) Шина хранения №1: отфильтрована на уголь.
        <ItemImage id="minecraft:coal" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 2 0" max="1 2.3 1">
        (4) Шина хранения №2: отфильтрована в чёрный список угля, с картой инверсии.
        <Row><ItemImage id="minecraft:coal" scale="2" /><ItemImage id="inverter_card" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) в своей конфигурации по умолчанию, с нужными <ItemLink id="processing_pattern" />.
    Он сделан направленным с помощью <ItemLink id="certus_quartz_wrench" />.

  ![Шаблон железа](../assets/diagrams/furnace_pattern.png)

* <ItemLink id="interface" /> (2) в своей конфигурации по умолчанию.
* Первая <ItemLink id="storage_bus" /> (3) отфильтрована на уголь или любое топливо, которое вы хотите использовать.
* Вторая <ItemLink id="storage_bus" /> (4) отфильтрована в чёрный список используемого топлива, с <ItemLink id="inverter_card" />.

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="interface" />.
   (На самом деле, в качестве оптимизации он помещает прямо через шины хранения, как если бы они были продолжениями граней поставщика. Предметы на самом деле никогда не входят в интерфейс.)
2. Интерфейс настроен хранить ничего, поэтому он пытается поместить ингредиенты в [сетевое хранилище](../ae2-mechanics/import-export-storage.md).
3. Единственное хранилище в зелёной подсети — <ItemLink id="storage_bus" />. Шина, отфильтрованная на уголь, помещает уголь в слот топлива печи через боковую грань.
    Шина, отфильтрованная на НЕ уголь, помещает предметы для плавки в верхний слот, через верхнюю грань.
4. Печь делает своё печное дело
5. Воронка извлекает результаты из низа печи и помещает их в слоты возврата поставщика, возвращая их в главную сеть.
