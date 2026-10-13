---
navigation:
  parent: example-setups/example-setups-index.md
  title: Простая ферма истинного кварца
  icon: certus_quartz_crystal
  position: 110
---

# Простая ферма истинного кварца

Как сказано в [Росте истинного кварца](../ae2-mechanics/certus-growth.md), автоматизация сбора <ItemLink id="certus_quartz_crystal" />
включает <ItemLink id="annihilation_plane" /> и <ItemLink id="storage_bus" />. 
<ItemLink id="growth_accelerator" /> используются, чтобы сильно ускорить рост бутонов истинного кварца, а затем плоскости
ломают полностью выросший <ItemLink id="quartz_cluster" />. Их фильтруют, пользуясь подозрительно удачной чертой, что незрелые
бутоны истинного кварца дропают <ItemLink id="certus_quartz_dust" /> вместо того чтобы не дропать ничего.

Эта ферма работает полностью автоматически с <ItemLink id="flawless_budding_quartz" />, но с несовершенным, потресканным и повреждённым
цветущим истинным кварцем вам придётся заменять цветущий блок вручную. Или, как описано в [Полуавтоматической ферме истинного кварца](semiauto-certus-farm.md)
и [Продвинутой ферме истинного кварца](advanced-certus-farm.md), автоматически.

Оценки скоростей см. в [Росте истинного кварца](../ae2-mechanics/certus-growth.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/simple_certus_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="3.7 1 1" max="4 2 2">
        (1) Плоскость уничтожения: интерфейса для настройки нет, но можно зачаровать на Удачу.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 1" max="3.3 2 2">
        (2) Шина хранения №1: отфильтрована на Кристалл истинного кварца.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="3 1 .7" max="2 2 1">
        (3) Шина хранения №2: отфильтрована на Кристалл истинного кварца. Установлен приоритет выше главного хранилища.
        <ItemImage id="certus_quartz_crystal" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может настраиваться, но может быть зачарована на удачу.
* Первая <ItemLink id="storage_bus" /> (2) отфильтрована на <ItemLink id="certus_quartz_crystal" />.
* Вторая <ItemLink id="storage_bus" /> (3) отфильтрована на <ItemLink id="certus_quartz_crystal" /> и у неё
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше главного хранилища.

## Как это работает

1. <ItemLink id="annihilation_plane" /> пытается сломать то, что перед ним, но может ломать только <ItemLink id="quartz_cluster" />,
   потому что единственное хранилище в подсети — <ItemLink id="storage_bus" />, отфильтрованная на <ItemLink id="certus_quartz_crystal" />.
4. Первая <ItemLink id="storage_bus" /> хранит кристаллы истинного кварца в бочке.
5. Вторая <ItemLink id="storage_bus" /> даёт главной сети доступ ко всем кристаллам истинного кварца в бочке. Установлен
   высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы кристаллы истинного кварца предпочтительно
   помещались обратно в бочку вместо вашего главного хранилища.
