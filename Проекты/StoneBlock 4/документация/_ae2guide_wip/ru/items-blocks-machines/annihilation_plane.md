---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Плоскость уничтожения
  icon: annihilation_plane
  position: 210
categories:
- devices
item_ids:
- ae2:annihilation_plane
---

# Плоскость уничтожения

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/blocks/annihilation_plane.snbt" />
</GameScene>

Плоскость уничтожения ломает блоки и подбирает предметы. Она работает подобно <ItemLink id="import_bus" />, помещая вещи
в [сетевое хранилище](../ae2-mechanics/import-export-storage.md). Чтобы предметы подбирались, они должны столкнуться с
гранью плоскости; она не подбирает в области.

Плоскости уничтожения можно зачаровывать любыми зачарованиями кирки, так что да, можно поставить безумные уровни удачи на несколько и
[автоматизировать обработку руды](../example-setups/ore-fortuner.md), если ваш модпак это позволяет. Кроме того, шёлковое касание делает то,
что вы ожидаете, эффективность снижает энергозатраты на разбивание блока, а прочность даёт шанс не использовать энергию.

Это [подчасти кабелей](../ae2-mechanics/cable-subparts.md).

**НЕ ЗАБУДЬТЕ ВКЛЮЧИТЬ ПОДДЕЛЬНЫХ ИГРОКОВ В ПРИВАТЕ ВАШЕГО ЧАНКА**

## Фильтрация

Плоскость уничтожения будет ломать блок или подбирать предмет, только если может хранить полученные дропы/предметы
в своей сети. Это означает, что для фильтрации *нужно ограничить то, что может храниться в её сети*, скорее всего поставив
её в [подсеть](../ae2-mechanics/subnetworks.md). <ItemLink id="storage_bus" /> или [ячейка](../items-blocks-machines/storage_cells.md)
может быть [разделена](cell_workbench.md), чтобы достичь этого.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/annihilation_filtering.snbt" />

  <DiamondAnnotation pos="1 0.5 0.5" color="#00ff00">
        Отфильтровано по тому, что выпадает из вещи, которую вы хотите ломать.
  </DiamondAnnotation>

  <DiamondAnnotation pos=".5 0.5 2.5" color="#00ff00">
        Разделено по тому, что выпадает из вещи, которую вы хотите ломать.
  </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Повторю: она фильтрует *по дропам предметов*, поэтому, например, если вы хотите фильтровать ломание <ItemLink id="minecraft:amethyst_cluster" />,
нужна плоскость с шёлковым касанием, иначе каждая предыдущая стадия роста не дропает ничего, и плоскость будет ломать их в любом случае,
поскольку сеть всегда может хранить «ничего».

## Рецепт

<RecipeFor id="annihilation_plane" />
