---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Приёмщик энергии
  icon: energy_acceptor
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_acceptor
---

# Приёмщик энергии

<Row gap="20">
<BlockImage id="energy_acceptor" scale="8" /> 

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/cable_energy_acceptor.snbt" />
</GameScene>
</Row>

Приёмщик энергии конвертирует распространённые формы энергии из других технических модов во внутреннюю форму [энергии](../ae2-mechanics/energy.md) AE2 —
AE. Хотя <ItemLink id="controller" /> тоже может делать это, стороны контроллера ценны, поэтому часто лучше использовать вместо этого приёмщик
энергии.

Коэффициенты конверсии Forge Energy и TechReborn Energy:

*   2 FE = 1 AE (Forge)
*   1 E  = 2 AE (Fabric)

Скорость конверсии полностью зависит от того, сколько AE ваша сеть может хранить, по причинам, объяснённым на
[этой странице](../ae2-mechanics/energy.md).

## Варианты

Приёмщики энергии бывают 2 вариантов: обычные и плоские/[подчасти](../ae2-mechanics/cable-subparts.md). Это позволяет делать некоторые схемы компактнее.

Приёмщики энергии можно переключать между обычным и плоским в сетке крафта.

## Рецепт

<RecipeFor id="energy_acceptor" />

<RecipeFor id="cable_energy_acceptor" />
