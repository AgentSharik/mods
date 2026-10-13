---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Ускоритель роста
  icon: growth_accelerator
  position: 310
categories:
- machines
item_ids:
- ae2:growth_accelerator
---

# Ускоритель роста

<BlockImage id="growth_accelerator" p:powered="true" scale="8"/>

Ускоритель роста сильно ускоряет [рост](../ae2-mechanics/certus-growth.md) истинного кварца или аметиста, если стоит рядом с цветущим блоком.

Любопытно, что он может *также* ускорять рост различных растений.

Он делает это, применяя «случайные тики» к смежным блокам, в дополнение к естественно происходящим случайным тикам.
В теории это означает, что 1 ускоритель должен делать рост примерно в 90 раз быстрее обычного, и эффект складывается аддитивно.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/growth_accelerator.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Энергию можно подавать сверху или снизу, через [кабели](cables.md) AE2 или силовые кабели других модов. Он может
принимать либо энергию AE2 (AE), либо Forge Energy (FE).

Чтобы питать его вручную, поставьте <ItemLink id="crank" /> сверху или снизу и щёлкните по нему правой кнопкой.

Верх и низ можно определить по розовым флюисовым декоративным элементам на них.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/accelerator_connections.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Рецепт

<RecipeFor id="growth_accelerator" />
