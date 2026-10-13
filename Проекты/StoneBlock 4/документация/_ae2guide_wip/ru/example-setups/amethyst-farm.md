---
navigation:
  parent: example-setups/example-setups-index.md
  title: Ферма аметиста
  icon: minecraft:amethyst_shard
---

# Ферма аметиста

Хотя <ItemLink id="growth_accelerator" /> работает на аметисте, обычные методы фильтрации [бутонов истинного кварца](../items-blocks-machines/budding_certus.md)
с помощью <ItemLink id="annihilation_plane" /> не работают на бутонах аметиста. В отличие от незрелых бутонов истинного кварца, которые дропают
<ItemLink id="certus_quartz_dust" />, незрелые бутоны аметиста дропают ничего, поэтому плоскость уничтожения всегда будет ломать их,
поскольку сеть всегда может хранить «ничего».

Обходной путь — зачаровать плоскость уничтожения шёлковым касанием. Тогда незрелые бутоны аметиста *дают* что-то
(различные стадии физических блоков бутонов), и, таким образом, могут фильтроваться.

<ItemLink id="minecraft:amethyst_cluster" /> затем должен быть размещён снова <ItemLink id="formation_plane" />, чтобы затем быть
повторно сломан <ItemLink id="annihilation_plane" /> без шёлкового касания, чтобы получить <ItemLink id="minecraft:amethyst_shard" />.

Обратите внимание, что из-за направленности кластера непосредственно напротив плоскости формирования должна быть сплошная грань блока.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/amethyst_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 1 1" max="3 2 2">
        (1) Плоскость уничтожения №1: интерфейса для настройки нет, но зачарована Шёлковым касанием.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 1 1" max="2.3 2 2">
        (2) Плоскость формирования: отфильтрована на Кластер аметиста.
        <ItemImage id="minecraft:amethyst_cluster" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1.3 0.7 1" max="2 1 2">
        (3) Плоскость уничтожения №2: интерфейса для настройки нет, но можно зачаровать на Удачу.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 1" max="1.3 1 2">
        (4) Шина хранения №1: отфильтрована на Осколок аметиста.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 .7" max="1 1 1">
        (5) Шина хранения №2: отфильтрована на Осколок аметиста. Установлен приоритет выше вашего главного хранилища.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет интерфейса и не может настраиваться, но должна быть зачарована шёлковым касанием.
* <ItemLink id="formation_plane" /> (2) отфильтрована на <ItemLink id="minecraft:amethyst_cluster" />.
* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет интерфейса и не может настраиваться, но может быть зачарована на удачу.
* Первая <ItemLink id="storage_bus" /> (4) отфильтрована на <ItemLink id="minecraft:amethyst_shard" />.
* Вторая <ItemLink id="storage_bus" /> (5) отфильтрована на <ItemLink id="minecraft:amethyst_shard" /> и у неё
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) установлен выше вашего главного хранилища.

## Как это работает

1. Первая <ItemLink id="annihilation_plane" /> пытается сломать то, что перед ней, но может ломать только <ItemLink id="minecraft:amethyst_cluster" />,
    потому что единственное хранилище в подсети — <ItemLink id="formation_plane" />, отфильтрованная на кластер аметиста. Это работает только потому, что
плоскость зачарована шёлковым касанием, иначе она могла бы ломать незрелые бутоны, поскольку те дропают ничего.
2. <ItemLink id="formation_plane" /> размещает кластер на блоке напротив неё.
3. Вторая <ItemLink id="annihilation_plane" /> ломает кластер, производя <ItemLink id="minecraft:amethyst_shard" />.
4. Первая <ItemLink id="storage_bus" /> хранит осколки в бочке. Это технически не нужно фильтровать, потому что единственное,
что вторая плоскость уничтожения должна встречать, — полностью выросшие кластеры.
5. Вторая <ItemLink id="storage_bus" /> даёт главной сети доступ ко всем осколкам аметиста в бочке. Установлен
высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы осколки аметиста предпочтительно
помещались обратно в бочку вместо вашего главного хранилища.
