---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Цветущий истинный кварц
  icon: flawless_budding_quartz
  position: 010
categories:
- misc ingredients blocks
item_ids:
- ae2:flawless_budding_quartz
- ae2:flawed_budding_quartz
- ae2:chipped_budding_quartz
- ae2:damaged_budding_quartz
- ae2:small_quartz_bud
- ae2:medium_quartz_bud
- ae2:large_quartz_bud
- ae2:quartz_cluster
---

# Цветущий истинный кварц

(см. также [Рост истинного кварца](../ae2-mechanics/certus-growth.md))

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_blocks.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Бутоны истинного кварца вырастают на цветущих блоках истинного кварца, подобно аметисту. Их находят в [метеоритах](../ae2-mechanics/meteorites.md).
Есть 4 уровня цветущих блоков: безупречный, несовершенный, потресканный и повреждённый. Легче всего их опознать
с модом вроде HWYLA, Jade, The One Probe и т.д. (или по экрану F3)

У несовершенных, потресканных и повреждённых, каждый раз, когда бутон вырастает на очередную стадию, блок с некоторым шансом
понижается на один уровень, в конце концов превращаясь в обычный <ItemLink id="quartz_block" />.

Безупречные цветущие блоки не понижаются от роста бутонов и действуют как бесконечный источник.

При разбивании обычной киркой цветущие блоки понизятся на 1 уровень. При разбивании киркой
с шёлковым касанием они не понизятся, если не были безупречными. **Это означает, что безупречные цветущие блоки нельзя
поднять и переместить киркой**. Вместо этого можно использовать [Пространственное хранилище](../ae2-mechanics/spatial-io.md), чтобы
вырезать и вставлять безупречные цветущие блоки.

## Рецепты

Несовершенные, потресканные и повреждённые можно сделать, бросив предыдущий уровень цветущего блока (или <ItemLink id="quartz_block" />)
в воду с одним или несколькими <ItemLink id="charged_certus_quartz_crystal" />.

Безупречный цветущий кварц нельзя сделать, его можно только найти в мире.

<Row>
  <RecipeFor id="damaged_budding_quartz" />

  <RecipeFor id="chipped_budding_quartz" />

  <RecipeFor id="flawed_budding_quartz" />
</Row>
