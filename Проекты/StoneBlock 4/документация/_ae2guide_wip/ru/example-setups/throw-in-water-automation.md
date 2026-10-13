---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация бросания в воду
  icon: fluix_crystal
---

# Автоматизация рецептов бросания в воду

Обратите внимание, что, поскольку здесь используется <ItemLink id="pattern_provider" />, это предназначено для интеграции в вашу схему [автоматического изготовления](../ae2-mechanics/autocrafting.md).

Некоторые рецепты требуют бросать предметы в воду (хотя похожая схема может бросать предметы и в другие места).
Это можно автоматизировать с помощью <ItemLink id="formation_plane" />, <ItemLink id="annihilation_plane" /> и некоторой поддерживающей
инфраструктуры (это по сути 2 модифицированные [трубные подсети](pipe-subnet.md)).

Эта схема предназначена для использования в сочетании с [автоматизацией зарядника](charger-automation.md), чтобы обеспечить <ItemLink id="charged_certus_quartz_crystal" />.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/throw_in_water.snbt" />

<BoxAnnotation color="#dddddd" min="2 0 1" max="3 1 2">
        (1) Поставщик шаблонов: в своей конфигурации по умолчанию, с нужными шаблонами обработки.

        ![Шаблон флюиса](../assets/diagrams/fluix_pattern_small.png) ![Шаблон несовершенного цветения](../assets/diagrams/flawed_budding_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1.7 0 1" max="2 1 2">
        (2) Интерфейс: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 .7 1" max="2 1 2">
        (3) Плоскость формирования: установлен ронять вход как предметы.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 2 1" max="2 2.3 2">
        (4) Плоскость уничтожения: интерфейса для настройки нет.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 1 1" max="3 1.3 2">
        (5) Шина хранения: отфильтрована на выходы шаблонов
        <Row><ItemImage id="fluix_crystal" scale="2" /><BlockImage id="flawless_budding_quartz" scale="2" /></Row>
  </BoxAnnotation>

<DiamondAnnotation pos="3.9 0.5 1.5" color="#00ff00">
        К главной сети и автоматизации зарядника
        <GameScene zoom="3" background="transparent">
          <ImportStructure src="../assets/assemblies/charger_automation.snbt" />
          <IsometricCamera yaw="195" pitch="30" />
        </GameScene>
    </DiamondAnnotation>

  <IsometricCamera yaw="180" pitch="0" />
</GameScene>

## Конфигурации и шаблоны

* <ItemLink id="pattern_provider" /> (1) в своей конфигурации по умолчанию, с нужными <ItemLink id="processing_pattern" />
  * Для <ItemLink id="fluix_crystal" /> рецепт по умолчанию из JEI/REI работает отлично:

    ![Шаблон флюиса](../assets/diagrams/fluix_pattern.png)

  * Для <ItemLink id="flawed_budding_quartz" /> лучше, вероятно, делать его прямо из <ItemLink id="quartz_block" />,
    что позволяет избежать проблем, когда вход одного рецепта является выходом другого, из-за чего шина хранения не может фильтровать:

    ![Шаблон несовершенного цветения](../assets/diagrams/flawed_budding_pattern.png)

* <ItemLink id="interface" /> (2) в своей конфигурации по умолчанию.
* <ItemLink id="formation_plane" /> (3) установлен ронять вход как предметы.
* <ItemLink id="annihilation_plane" /> (4) не имеет интерфейса и не может настраиваться.
* <ItemLink id="storage_bus" /> (5) отфильтрована на выходы шаблонов.

## Как это работает

1.  <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="interface" /> на своей стороне, в зелёную подсеть
2.  Интерфейс (будучи настроенным по умолчанию хранить ничего) пытается поместить своё содержимое в [сетевое хранилище](../ae2-mechanics/import-export-storage.md)
3.  Единственное хранилище в зелёной подсети — <ItemLink id="formation_plane" />, который роняет получаемые предметы в воду
4.  <ItemLink id="annihilation_plane" /> в оранжевой подсети пытается поднять предметы, которые только что уронили, но не может, потому что
    <ItemLink id="storage_bus" /> поверх поставщика шаблонов (единственное хранилище в оранжевой подсети) отфильтрована и принимает только результаты возможных изготовлений
5.  Предметы выполняют своё превращение в мире.
6.  Плоскость уничтожения теперь может поднять предметы перед ней, поскольку шине хранения разрешено хранить их.
7.  Шина хранения хранит полученные предметы в поставщике шаблонов, возвращая их в сеть.
