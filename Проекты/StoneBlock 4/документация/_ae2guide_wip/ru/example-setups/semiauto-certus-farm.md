---
navigation:
  parent: example-setups/example-setups-index.md
  title: Полуавтоматическая ферма истинного кварца
  icon: certus_quartz_crystal
  position: 115
---

# Полуавтоматическая ферма истинного кварца

К сожалению, [простая ферма истинного кварца](simple-certus-farm.md) требует <ItemLink id="flawless_budding_quartz" />, чтобы работать полностью
автоматически. Для этого требуется либо [Пространственный ввод-вывод](../ae2-mechanics/spatial-io.md), либо строить ферму у [метеорита](../ae2-mechanics/meteorites.md).

Однако AE2 может размещать и ломать блоки, так что, возможно, как раз
получится заставить вашу ферму *заменять цветущий кварц за вас*. (Вам придётся периодически вставлять немного
<ItemLink id="flawed_budding_quartz" /> во входную бочку и извлекать <ItemLink id="quartz_block" /> из бочки отработанного
цветущего кварца)

Чтобы делать это полностью автоматически, см. [Продвинутую ферму истинного кварца](advanced-certus-farm.md).

Эта ферма немного сложнее [простой фермы истинного кварца](simple-certus-farm.md), потому что это на самом деле
3 отдельные схемы, сдавленные вместе.

Оценки скоростей см. в [Росте истинного кварца](../ae2-mechanics/certus-growth.md).

**ЭТО СЛОЖНАЯ ПОСТРОЙКА С ВЕЩАМИ, СКРЫТЫМИ ЗА ДРУГИМИ ВЕЩАМИ, ВРАЩАЙТЕ, ЧТОБЫ РАССМОТРЕТЬ СО ВСЕХ СТОРОН**

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/semiauto_certus_farm.snbt" />

  <BoxAnnotation color="#ddaaaa" min="3.7 2 1" max="4 3 2">
        (1) Плоскость уничтожения №1: интерфейса для настройки нет, но можно зачаровать на Удачу.
  </BoxAnnotation>

  <BoxAnnotation color="#ddaaaa" min="2 2 1" max="2.3 3 2">
        (2) Шина хранения №1: отфильтрована на Кристалл истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 2.5 1.5" color="#ff0000">
    Подсеть разрушителя кластеров
  </DiamondAnnotation>

  <BoxAnnotation color="#aaddaa" min="3.7 1 1" max="4 2 2">
        (3) Плоскость уничтожения №2: интерфейса для настройки нет, но зачарована Шёлковым касанием.
  </BoxAnnotation>

  <BoxAnnotation color="#aaddaa" min="2 1 1" max="2.3 2 2">
        (4) Шина хранения №2: отфильтрована на Блок истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
  </BoxAnnotation>

  <DiamondAnnotation pos="3 1.5 1.5" color="#00ff00">
    Подсеть разрушителя блоков истинного кварца
  </DiamondAnnotation>

  <BoxAnnotation color="#ffddaa" min="4 0.7 1" max="5 1 2">
        (5) Плоскость формирования: в своей конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#ffddaa" min="2 0 1" max="2.3 1 2">
        (6) Шина импорта: в своей конфигурации по умолчанию.
  </BoxAnnotation>

  <DiamondAnnotation pos="3 0.5 1.5" color="#ddcc00">
    Подсеть размещателя цветущих блоков
  </DiamondAnnotation>

  <BoxAnnotation color="#aaaadd" min="0.7 2 1" max="1 3 2">
        (7) Шина хранения №3: отфильтрована на Кристалл истинного кварца. Установлен приоритет выше вашего главного хранилища.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

    <DiamondAnnotation pos="1.5 0.5 1.5" color="#00ff00">
        Вставляйте вручную несовершенный цветущий истинный кварц.
        <BlockImage id="flawed_budding_quartz" scale="2" />
    </DiamondAnnotation>

    <DiamondAnnotation pos="1.5 1.5 1.5" color="#00ff00">
        Извлекайте вручную блок истинного кварца.
        <BlockImage id="quartz_block" scale="2" />
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="165" pitch="5" />
</GameScene>

## Конфигурации

### Разрушитель кластеров:

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может настраиваться, но может быть зачарована на Удачу.
* Первая <ItemLink id="storage_bus" /> (2) отфильтрована на <ItemLink id="certus_quartz_crystal" />.

### Разрушитель блоков истинного кварца:

* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет интерфейса и не может настраиваться, но должна быть зачарована Шёлковым касанием.
* Вторая <ItemLink id="storage_bus" /> (4) отфильтрована на <ItemLink id="quartz_block" />.

### Размещатель цветущих блоков:

* <ItemLink id="formation_plane" /> (5) в своей конфигурации по умолчанию.
* <ItemLink id="import_bus" /> (6) в своей конфигурации по умолчанию.

### В главной сети:

* Третья <ItemLink id="storage_bus" /> (7) отфильтрована на <ItemLink id="certus_quartz_crystal" /> и у неё
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше вашего главного хранилища.

## Как это работает

### Разрушитель кластеров:

Подсеть разрушителя кластеров работает очень похоже на подсеть в [простой ферме истинного кварца](simple-certus-farm.md).

1. <ItemLink id="annihilation_plane" /> пытается сломать то, что перед ней, но может ломать только <ItemLink id="quartz_cluster" />,
   потому что единственное хранилище в подсети — <ItemLink id="storage_bus" />, отфильтрованная на <ItemLink id="certus_quartz_crystal" />.
2. <ItemLink id="storage_bus" /> хранит кристаллы истинного кварца в бочке.

### Разрушитель блоков истинного кварца

Подсеть разрушителя блоков истинного кварца служит для ломания истощённого цветущего блока, когда он превращается в обычный <ItemLink id="quartz_block" />.
Работает подобно разрушителю кластеров.

1. <ItemLink id="annihilation_plane" /> пытается сломать то, что перед ней, но может ломать только <ItemLink id="quartz_block" />,
   потому что единственное хранилище в подсети — <ItemLink id="storage_bus" />, отфильтрованная на <ItemLink id="quartz_block" />.
   Плоскости нужно шёлковое касание, чтобы цветущий блок не понижался при ломании, и, таким образом, плоскость не сломает его преждевременно.
2. <ItemLink id="storage_bus" /> хранит блок истинного кварца в бочке отработанного
   цветущего кварца; вам придётся вручную бросать его в воду с <ItemLink id="charged_certus_quartz_crystal" />, чтобы обновить его.

### Размещатель цветущих блоков

Подсеть размещателя цветущих блоков служит для размещения нового <ItemLink id="flawed_budding_quartz" />, когда подсеть разрушителя ломает старый истощённый.

1. <ItemLink id="import_bus" /> импортирует цветущий блок из входной бочки.
2. Единственное хранилище в подсети — <ItemLink id="formation_plane" />, который размещает цветущий блок.

### В главной сети

* <ItemLink id="storage_bus" /> даёт главной сети (а также [Автоматизации зарядника](charger-automation.md)) доступ ко всем кристаллам истинного кварца в бочке. Установлен
  высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы истинного кварца предпочтительно
  помещались обратно в бочку вместо вашего главного хранилища.
