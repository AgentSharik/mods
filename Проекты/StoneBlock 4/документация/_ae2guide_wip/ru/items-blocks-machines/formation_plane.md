---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Плоскость формирования
  icon: formation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:formation_plane
---

# Плоскость формирования

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/formation_plane.snbt" />
</GameScene>

Плоскость формирования размещает блоки и роняет предметы. Она работает подобно <ItemLink id="storage_bus" /> только на вставку,
размещая/роняя, когда вещи «складываются» в неё [устройствами](../ae2-mechanics/devices.md), вставляющими в [сетевое хранилище](../ae2-mechanics/import-export-storage.md),
например <ItemLink id="import_bus" /> и <ItemLink id="interface" />.

<GameScene zoom="8" interactive={true}>
  <ImportStructure src="../assets/assemblies/formation_plane_demonstration.snbt" />
  <IsometricCamera yaw="255" pitch="30" />
</GameScene>

Обратите внимание, что это похоже на трубы шина импорта -> шина хранения и интерфейс -> шина хранения в [трубных подсетях](../example-setups/pipe-subnet.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_storage_pipe.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Это [устройство](../ae2-mechanics/devices.md) использует механику, которую шины хранения применяют в таких вещах, как [трубные подсети](../example-setups/pipe-subnet.md),
и может заменять шины хранения в тех схемах, если вы хотите ронять предметы/размещать блоки вместо транспортировки предметов.

Это [подчасти кабелей](../ae2-mechanics/cable-subparts.md).

**НЕ ЗАБУДЬТЕ ВКЛЮЧИТЬ ПОДДЕЛЬНЫХ ИГРОКОВ В ПРИВАТЕ ВАШЕГО ЧАНКА**

## Фильтрация

По умолчанию плоскость будет размещать/ронять всё. Предметы, вставленные в её слоты фильтра, будут работать как белый список, только
разрешая размещать эти конкретные предметы.

Предметы и жидкости можно перетаскивать в слоты из JEI/REI, даже если у вас на самом деле нет такого предмета.

Щёлкните правой кнопкой контейнером с жидкостью (например, ведром или жидкостным баком), чтобы задать эту жидкость фильтром вместо предмета ведра или бака.

## Приоритет

Приоритеты задаются щелчком по гаечному ключу в правом верхнем углу интерфейса.
Предметы, входящие в сеть, сначала направляются в хранилище с наивысшим приоритетом.

## Настройки

*   Плоскость можно настроить на размещение блоков в мире или роняние предметов

## Улучшения

Плоскость формирования поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="capacity_card" /> увеличивает количество слотов фильтра
*   <ItemLink id="fuzzy_card" /> позволяет плоскости фильтровать по уровню урона и/или игнорировать NBT предметов
*   <ItemLink id="inverter_card" /> переключает фильтр из белого списка в чёрный

## Рецепт

<RecipeFor id="formation_plane" />
