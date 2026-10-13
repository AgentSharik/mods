---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Ячейки хранения
  icon: item_storage_cell_1k
  position: 410
categories:
- tools
item_ids:
- ae2:item_cell_housing
- ae2:fluid_cell_housing
- ae2:cell_component_1k
- ae2:cell_component_4k
- ae2:cell_component_16k
- ae2:cell_component_64k
- ae2:cell_component_256k
- ae2:item_storage_cell_1k
- ae2:item_storage_cell_4k
- ae2:item_storage_cell_16k
- ae2:item_storage_cell_64k
- ae2:item_storage_cell_256k
- ae2:fluid_storage_cell_1k
- ae2:fluid_storage_cell_4k
- ae2:fluid_storage_cell_16k
- ae2:fluid_storage_cell_64k
- ae2:fluid_storage_cell_256k
---

# Ячейки хранения

<Column>
  <Row>
    <ItemImage id="item_storage_cell_1k" scale="4" />

    <ItemImage id="item_storage_cell_4k" scale="4" />

    <ItemImage id="item_storage_cell_16k" scale="4" />

    <ItemImage id="item_storage_cell_64k" scale="4" />

    <ItemImage id="item_storage_cell_256k" scale="4" />
  </Row>

  <Row>
    <ItemImage id="fluid_storage_cell_1k" scale="4" />

    <ItemImage id="fluid_storage_cell_4k" scale="4" />

    <ItemImage id="fluid_storage_cell_16k" scale="4" />

    <ItemImage id="fluid_storage_cell_64k" scale="4" />

    <ItemImage id="fluid_storage_cell_256k" scale="4" />
  </Row>
</Column>

Ячейки хранения — один из главных методов хранения в Applied Energistics. Они идут в <ItemLink id="drive" />
или <ItemLink id="chest" />.

Объяснение их вместимости в байтах и типах см. в [Байтах и типах](../ae2-mechanics/bytes-and-types.md).

Компоненты хранения можно извлекать из корпуса, если ячейка пуста, щелчком правой кнопкой с Shift с ячейкой в руке.

<Row>
    <Recipe id="upgrade/item_storage_cell_1k_to_4k" />

    Вы можете повышать уровень ячеек хранения до более высоких, объединяя их с компонентами хранения более высокого уровня в сетке крафта. Их содержимое сохранится, а компонент более низкого уровня вернётся.
</Row>

## Вместимость при разном числе типов

[Предварительная стоимость типов](../ae2-mechanics/bytes-and-types.md) такова, что ячейка с 1 типом может вместить вдвое больше, чем ячейка со всеми 63 типами.

| Ячейка                                     | Полная вместимость при 1 типе | Полная вместимость при 63 типах |
| ---------------------------------------- | ----------------------------------------: | ------------------------------------------: |
| <ItemLink id="item_storage_cell_1k" />   |                                     8,128 |                                       4,160 |
| <ItemLink id="item_storage_cell_4k" />   |                                    32,512 |                                      16,640 |
| <ItemLink id="item_storage_cell_16k" />  |                                   130,048 |                                      66,560 |
| <ItemLink id="item_storage_cell_64k" />  |                                   520,192 |                                     266,240 |
| <ItemLink id="item_storage_cell_256k" /> |                                 2,080,768 |                                   1,064,960 |


## Разделение

Ячейки можно фильтровать так, что они принимают только определённые предметы, подобно тому, как можно фильтровать <ItemLink id="storage_bus" />. Это
делается в <ItemLink id="cell_workbench" />.

Предметы можно перетаскивать в слоты из JEI/REI, даже если у вас на самом деле нет такого предмета.

## Улучшения

Ячейки хранения поддерживают следующие [улучшения](upgrade_cards.md), вставляемые через <ItemLink id="cell_workbench" />:

*   <ItemLink id="fuzzy_card" /> (недоступна на жидкостных ячейках) позволяет разделять ячейку по уровню урона и/или игнорировать NBT предметов
*   <ItemLink id="inverter_card" /> переключает фильтр из белого списка в чёрный
*   <ItemLink id="equal_distribution_card" /> выделяет каждому типу одинаковое количество байтового пространства ячейки, поэтому один тип не может занять всю ячейку
*   <ItemLink id="void_card" /> уничтожает вставленные предметы, если ячейка полна (или выделенное пространство конкретного типа в
    случае карты равномерного распределения); полезно, чтобы фермы не забивались. Будьте осторожны при разделении этого!
*   Портативные ячейки могут принимать <ItemLink id="energy_card" />, чтобы увеличить ёмкость батареи

## Окрашивание

Портативные ячейки предметов и жидкостей можно окрашивать подобно кожаной броне, создавая их вместе с красителями.

# Корпуса

Ячейки можно делать из компонента хранения и корпуса или по рецепту корпуса вокруг компонента хранения:

<Row>
  <Recipe id="network/cells/item_storage_cell_1k" />

  <Recipe id="network/cells/item_storage_cell_1k_storage" />
</Row>

Сами корпуса делаются так:

<Row>
  <RecipeFor id="item_cell_housing" />

  <RecipeFor id="fluid_cell_housing" />
</Row>

# Компоненты хранения

Компоненты хранения — ядро всех ячеек AE2, определяющее вместимость ячеек. Каждый уровень увеличивает вместимость
в 4 раза и стоит 3 предыдущего уровня.

<Column>
  <Row>
    <RecipeFor id="cell_component_1k" />

    <RecipeFor id="cell_component_4k" />

    <RecipeFor id="cell_component_16k" />
  </Row>

  <Row>
    <RecipeFor id="cell_component_64k" />

    <RecipeFor id="cell_component_256k" />
  </Row>
</Column>

# Ячейки хранения предметов

Ячейки хранения предметов могут вмещать до 63 различных типов предметов и доступны всех стандартных вместимостей.

<Column>
  <Row>
    <Recipe id="network/cells/item_storage_cell_1k_storage" />

    <Recipe id="network/cells/item_storage_cell_4k_storage" />

    <Recipe id="network/cells/item_storage_cell_16k_storage" />
  </Row>

  <Row>
    <Recipe id="network/cells/item_storage_cell_64k_storage" />

    <Recipe id="network/cells/item_storage_cell_256k_storage" />
  </Row>
</Column>

## Портативное хранение предметов

Они работают как крошечный <ItemLink id="chest" /> в вашем кармане или как форма рюкзака. Их можно заряжать в <ItemLink id="charger" />

В отличие от стандартных ячеек хранения, они на самом деле *уменьшаются* по вместимости типов по мере роста вместимости байт и имеют половину
общей вместимости байт.

В дополнение к картам улучшений, которые могут получать все ячейки, они также принимают <ItemLink id="energy_card" /> для улучшения своих внутренних батарей.

<Column>
  <Row>
    <RecipeFor id="portable_item_cell_1k" />

    <RecipeFor id="portable_item_cell_4k" />

    <RecipeFor id="portable_item_cell_16k" />
  </Row>

  <Row>
    <RecipeFor id="portable_item_cell_64k" />

    <RecipeFor id="portable_item_cell_256k" />
  </Row>
</Column>

# Ячейки хранения жидкостей

Ячейки хранения жидкостей могут вмещать до 5 различных типов жидкостей и доступны всех стандартных вместимостей.

<Column>
  <Row>
    <Recipe id="network/cells/fluid_storage_cell_1k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_4k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_16k_storage" />
  </Row>

  <Row>
    <Recipe id="network/cells/fluid_storage_cell_64k_storage" />

    <Recipe id="network/cells/fluid_storage_cell_256k_storage" />
  </Row>
</Column>

## Портативное хранение жидкостей

Они работают как крошечный <ItemLink id="chest" /> в вашем кармане или как форма рюкзака. Их можно заряжать в <ItemLink id="charger" />

В отличие от стандартных ячеек хранения, они на самом деле *уменьшаются* по вместимости типов по мере роста вместимости байт и имеют половину
общей вместимости байт.

В дополнение к картам улучшений, которые могут получать все ячейки, они также принимают <ItemLink id="energy_card" /> для улучшения своих внутренних батарей.

<Column>
  <Row>
    <RecipeFor id="portable_fluid_cell_1k" />

    <RecipeFor id="portable_fluid_cell_4k" />

    <RecipeFor id="portable_fluid_cell_16k" />
  </Row>

  <Row>
    <RecipeFor id="portable_fluid_cell_64k" />

    <RecipeFor id="portable_fluid_cell_256k" />
  </Row>
</Column>

# Творческая ячейка хранения

<Row>
  <ItemImage id="creative_storage_cell" scale="2" />
</Row>

Творческие ячейки **не дают бесконечного хранилища**. Вместо этого они работают как бесконечные источники и приёмники любого
предмета или жидкости, на которые вы их [разделите](cell_workbench.md).
