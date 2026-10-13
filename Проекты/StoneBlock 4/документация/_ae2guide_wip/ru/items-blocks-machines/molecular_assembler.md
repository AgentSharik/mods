---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Молекулярный сборщик
  icon: molecular_assembler
  position: 310
categories:
- machines
item_ids:
- ae2:molecular_assembler
---

# Молекулярный сборщик

<BlockImage id="molecular_assembler" scale="8" />

Молекулярный сборщик принимает помещённые в него предметы и выполняет операцию, определённую смежным <ItemLink id="pattern_provider" />,
или вставленным <ItemLink id="crafting_pattern" />, <ItemLink id="smithing_table_pattern" /> или <ItemLink id="stonecutting_pattern" />,
а затем помещает результат в смежные инвентари.

У этого сборщика есть шаблон изготовления, задающий рецепт 1 дубовое бревно = 4 дубовые доски. Когда дубовые брёвна подаются в верхнюю воронку,
сборщик создаёт и выплёвывает дубовые доски в нижнюю воронку.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/standalone_assembler.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Главное использование молекулярного сборщика

Однако их главное использование — рядом с <ItemLink id="pattern_provider" />. У поставщиков шаблонов в этом случае есть особое поведение,
и они отправляют информацию о нужном шаблоне вместе с ингредиентами соседним сборщикам. Поскольку сборщики автоматически выбрасывают результаты
изготовления в смежные инвентари (и тем самым в слоты возврата поставщика шаблонов), одного сборщика на поставщике шаблонов
достаточно, чтобы автоматизировать шаблоны изготовления.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/assembler_tower.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Улучшения

Молекулярный сборщик поддерживает следующие [улучшения](upgrade_cards.md):

*   <ItemLink id="speed_card" />

## Рецепт

<RecipeFor id="molecular_assembler" />

## Примечание

Optifine ломает функцию «поместить в смежные инвентари», поэтому большинство схем изготовления со сборщиками не будет работать.
