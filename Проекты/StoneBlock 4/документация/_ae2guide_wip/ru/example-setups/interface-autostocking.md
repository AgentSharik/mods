---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоподдержание запасов интерфейсом
  icon: interface
---

# Автоподдержание запасов интерфейсом

Можно спросить: «Как держать определённое количество различных предметов в наличии, изготовляя ещё по мере необходимости?»

Одно решение — использование <ItemLink id="interface" /> и <ItemLink id="crafting_card" />, чтобы автоматически запрашивать новые предметы
из [автоматического изготовления](../ae2-mechanics/autocrafting.md) вашей сети. Эта схема больше подходит для поддержания небольшого количества широкого
разнообразия предметов.

Эта демонстрационная схема обрезана, чтобы не была слишком широкой; скорее всего оптимально использовать 4 <ItemLink id="interface" /> и 4 <ItemLink id="storage_bus" />,
чтобы использовать все 8 [каналов](../ae2-mechanics/channels.md) в обычном [кабеле](../items-blocks-machines/cables.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_autostocking.snbt" />

<BoxAnnotation color="#dddddd" min="0 0 0" max="2 1 1">
        (1) Интерфейсы: настроены держать желаемые предметы в себе. У них есть Карты изготовления.
        <ItemImage id="crafting_card" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 0" max="2 1.3 1">
        (2) Шины хранения: «Режим ввода/вывода» установлен на «Только извлечение».
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="interface" /> (1) настроены держать желаемые предметы в себе, щёлкая желаемый предмет в их
   верхние слоты или перетаскивая в верхние слоты из JEI, затем щёлкая по значку гаечного ключа над слотами, чтобы задать количество. У них есть <ItemLink id="crafting_card" />.
* <ItemLink id="storage_bus" /> (2) настроены так, что «Режим ввода/вывода» установлен на «Только извлечение».

## Как это работает

1. Если <ItemLink id="interface" /> не может извлечь достаточно настроенного предмета из [сетевого хранилища](../ae2-mechanics/import-export-storage.md),
   (и у него есть <ItemLink id="crafting_card" />), он запросит, чтобы [автоматическое изготовление](../ae2-mechanics/autocrafting.md) сети изготовило ещё этого предмета.
2. <ItemLink id="storage_bus" /> позволяют сети обращаться к содержимому интерфейсов.
