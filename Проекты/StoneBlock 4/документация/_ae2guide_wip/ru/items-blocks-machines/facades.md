---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Облицовка
  icon: facade
  icon_components:
    "ae2:facade_item": "minecraft:stone"
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:facade
---

# Облицовка

Облицовку можно использовать, чтобы ваша база выглядела чище. Она может закрывать кабели обоих размеров и делаться из многих
видов блоков.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/facades_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Она может закрывать все стороны кабеля, но [подчасти](../ae2-mechanics/cable-subparts.md) и кабельные соединения будут
выступать сквозь неё.

<GameScene zoom="6"  interactive={true}>
  <ImportStructure src="../assets/assemblies/facades_2.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Используйте её хитроумно, чтобы улучшить эстетику базы или сделать блоки с различными текстурами на каждой стороне.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/facades_3.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Скрытие облицовки

Облицовка будет скрыта, пока вы держите <a href="network_tool.md">сетевой инструмент</a> в любой руке.

Вы можете взаимодействовать с блоками за скрытой облицовкой, не снимая её сначала.

## Рецепт

Поместите блок, текстуру которого хотите, в середину 4 <ItemLink id="cable_anchor" />.

![Рецепт облицовки](../assets/diagrams/facade_recipe.png)
