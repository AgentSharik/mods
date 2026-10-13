---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Зарядник
  icon: charger
  position: 310
categories:
- machines
item_ids:
- ae2:charger
---

# Зарядник

<BlockImage id="charger" scale="8" />

Зарядник обеспечивает способ заряжать
поддерживаемые инструменты и <ItemLink id="certus_quartz_crystal" />.

Энергию можно подавать сверху или снизу, через [кабели](cables.md) AE2 или силовые кабели других модов. Он может
принимать либо энергию AE2 (AE), либо Forge Energy (FE). Предметы можно вставлять или извлекать с любой стороны. Извлекать можно только результаты,
поэтому фильтры не нужны, чтобы не извлекать кристаллы истинного кварца вместо заряженных. Можно вращать
<ItemLink id="certus_quartz_wrench" /> для удобства автоматизации.

Может использоваться для создания <ItemLink id="charged_certus_quartz_crystal" />
из <ItemLink id="certus_quartz_crystal" /> и <ItemLink id="meteorite_compass" /> из <ItemLink id="minecraft:compass" />.

Чтобы питать его вручную, поставьте <ItemLink id="crank" /> сверху или снизу и щёлкайте по нему правой кнопкой, пока предмет не зарядится.

Он также служит рабочей станцией для [Исследователя флюиса](fluix_researcher.md).

## Простая автоматизация

Например, вращаемость позволяет полуавтоматизировать зарядники вот так:

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/charger_hopper.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Рецепт

<RecipeFor id="charger" />
