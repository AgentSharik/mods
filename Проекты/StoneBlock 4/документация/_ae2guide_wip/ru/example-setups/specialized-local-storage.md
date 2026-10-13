---
navigation:
  parent: example-setups/example-setups-index.md
  title: Специализированное локальное хранилище
  icon: drive
---

# Специализированное локальное хранилище

Используя одно из [особых поведений интерфейса](../items-blocks-machines/interface.md#special-interactions), [подсеть](../ae2-mechanics/subnetworks.md) может представлять содержимое своего хранилища главной сети, не будучи способной
видеть хранилище главной сети, и занимая всего 1 [канал](../ae2-mechanics/channels.md).

Это полезно для локального хранилища на какой-то ферме, чтобы предметы не переполняли ваше главное хранилище.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/local_storage.snbt" />

<BoxAnnotation color="#dddddd" min="4 0 0" max="5 2 1">
        (1) Какой-то метод импорта предметов (в данном случае интерфейс)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 0 0" max="4 1 1">
        (2) Накопитель: в нём несколько ячеек. Ячейки должны быть отфильтрованы на то, что ферма производит.
        У ячеек могут быть Карты равномерного распределения и Карты уничтожения излишков.
        <Row><ItemImage id="item_storage_cell_4k" scale="2" /> <ItemImage id="equal_distribution_card" scale="2" /> <ItemImage id="void_card" scale="2" /></Row>
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3 1 0" max="4 2 0.3">
        (3) Терминал изготовления: он может видеть содержимое Накопителя в подсети, но не содержимое хранилища вашей главной сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 0" max="2.3 1 1">
        (4) Интерфейс №2: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1.7 0 0" max="2 1 1">
        (5) Шина хранения: установлен приоритет выше главного хранилища, может быть отфильтрована на то, что ферма производит.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 2 0.3">
        Терминал изготовления: он может видеть и содержимое хранилища главной сети, *и* подсеть.
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* Первый <ItemLink id="interface" /> (1) просто принимает предметы с любой вашей фермы и помещает их в подсеть.
* <ItemLink id="drive" /> (2) имеет несколько [ячеек](../items-blocks-machines/storage_cells.md) в нём. Ячейки должны быть
  [разделены](../items-blocks-machines/cell_workbench.md) на то, что ферма производит.
  У ячеек могут быть <ItemLink id="equal_distribution_card" /> и <ItemLink id="void_card" />.
* Второй <ItemLink id="interface" /> (4) в своей конфигурации по умолчанию.
* У <ItemLink id="storage_bus" /> [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен
  выше главного хранилища. Он может быть отфильтрован на то, что ферма производит.

## Как это работает

* <ItemLink id="interface" /> в подсети показывает <ItemLink id="storage_bus" /> в главной сети содержимое
<ItemLink id="drive" />. Это означает, что шина хранения может напрямую извлекать предметы из ячеек в накопителе и помещать в них предметы.
* У шины хранения установлен высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы предметы предпочтительно
  помещались обратно в подсеть вместо вашего главного хранилища.
* Что важно, если ячейки в подсети заполнятся, предметы не переполнят главную сеть. Если ферма такого типа,
что ломается при забивании, можно использовать <ItemLink id="void_card" />, чтобы вместо этого удалять лишние предметы. 
* Если ферма выводит несколько предметов, <ItemLink id="equal_distribution_card" /> могут не дать одному предмету заполнить все ячейки
и не позволить остальным предметам храниться.
