---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Энергохранилища
  icon: energy_cell
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_cell
- ae2:dense_energy_cell
- ae2:creative_energy_cell
---

# Энергохранилища

<Row gap="20">
  <BlockImage id="energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="dense_energy_cell" scale="8" p:fullness="4" />

  <BlockImage id="creative_energy_cell" scale="8" />
</Row>

Энергохранилища дают сети больше хранилища [энергии](../ae2-mechanics/energy.md). Некоторый энергетический буфер помогает сглаживать
скачки потребления энергии, когда большие объёмы предметов вставляются или извлекаются, а большие объёмы хранилища энергии
позволяют сети работать, пока энергия не генерируется (как ночью с солнечными панелями), или покрывать огромное мгновенное
потребление энергии [пространственного хранилища](../ae2-mechanics/spatial-io.md).

## Полосы заполнения

<Row>
<BlockImage id="energy_cell" scale="4" p:fullness="0" />
<BlockImage id="energy_cell" scale="4" p:fullness="1" />
<BlockImage id="energy_cell" scale="4" p:fullness="2" />
<BlockImage id="energy_cell" scale="4" p:fullness="3" />
<BlockImage id="energy_cell" scale="4" p:fullness="4" />
</Row>

Полосы сбоку ячейки соответствуют тому, сколько в ней энергии.

*   0 при заряде ниже 25%
*   1 при заряде от 25% до 50%
*   2 при заряде от 50% до 75%
*   3 при заряде от 75% до 99%
*   4 при заряде выше 99%

## Типы ячеек

*   <ItemLink id="energy_cell" /> может хранить 200 тыс. AE, и одного обычно хватает для большинства задач, легко покрывая скачки потребления
    при обычном использовании сети.
*   <ItemLink id="dense_energy_cell" /> может хранить 1,6 млн AE — для случаев, когда вы хотите работать от запасённой энергии или
    покрывать огромное мгновенное потребление энергии больших схем [пространственного хранилища](../ae2-mechanics/spatial-io.md).
*   <ItemLink id="creative_energy_cell" /> — творческий предмет для тестов, дающий НЕОГРАНИЧЕННУЮ МОЩЬ или что-то вроде того.

## Рецепты

<Row>
  <RecipeFor id="energy_cell" />

  <RecipeFor id="dense_energy_cell" />
</Row>
