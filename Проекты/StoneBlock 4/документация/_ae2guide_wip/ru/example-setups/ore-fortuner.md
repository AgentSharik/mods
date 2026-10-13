---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматический обогатитель руды
  icon: minecraft:raw_iron
---

# Автоматизация обогащения руды

<ItemLink id="annihilation_plane" /> можно зачаровывать любыми зачарованиями кирки, включая удачу, поэтому очевидный случай использования —
применить удачу к нескольким, и чтобы <ItemLink id="formation_plane" /> и <ItemLink id="annihilation_plane" /> быстро размещали и
ломали руды.

Обратите внимание, что, поскольку <ItemLink id="import_bus" /> «раскручиваются до скорости», схема начнётся медленно, а затем за несколько секунд достигнет полной скорости.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 0 2" max="3 1 3">
        (1) Шина импорта: в ней несколько Карт ускорения.
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 2" max="2 1 2.3">
        (2) Плоскости формирования: в их конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 0.7" max="2 1 1">
        (3) Плоскости уничтожения: интерфейса для настройки нет, но они зачарованы на Удачу.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 0 0" max="3 1 1">
        (4) Шина хранения: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="3.5 0.5 2.5" color="#00ff00">
        Вход
    </DiamondAnnotation>

<DiamondAnnotation pos="3.5 0.5 0.5" color="#00ff00">
        Выход
    </DiamondAnnotation>

<DiamondAnnotation pos="4 0.5 1.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

*   <ItemLink id="import_bus" /> (1) имеет несколько <ItemLink id="speed_card" /> в нём. Чем больше плоскостей формирования
    в массиве, тем больше нужно, поскольку они заставляют шину импорта извлекать больше предметов за раз.
*   <ItemLink id="formation_plane" /> (2) в их конфигурациях по умолчанию.
*   <ItemLink id="annihilation_plane" /> (3) не имеют интерфейса и не могут настраиваться, но зачарованы на удачу.
*   <ItemLink id="storage_bus" /> (4) в своей конфигурации по умолчанию.

## Как это работает

1.  <ItemLink id="import_bus" /> в зелёной подсети импортирует блоки из первой бочки в [сетевое хранилище](../ae2-mechanics/import-export-storage.md)
2.  Единственное хранилище в зелёной подсети — <ItemLink id="formation_plane" />, который размещает блоки.
3.  <ItemLink id="annihilation_plane" /> в оранжевой подсети ломает блоки, применяя к ним удачу.
4.  <ItemLink id="storage_bus" /> в оранжевой подсети хранит результаты ломания во второй бочке.
