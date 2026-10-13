---
navigation:
  title: МЕГА автоматическое изготовление
  icon: 256m_crafting_storage
  parent: index.md
  position: 020
categories:
  - megacells
item_ids:
  - mega_crafting_unit
  - 1m_crafting_storage
  - 4m_crafting_storage
  - 16m_crafting_storage
  - 64m_crafting_storage
  - 256m_crafting_storage
  - mega_crafting_accelerator
  - mega_crafting_monitor
  - mega_pattern_provider
  - cable_mega_pattern_provider
---

# MEGA Cells: Автоматическое изготовление

<GameScene zoom="6" background="transparent">
  <ImportStructure src="assets/assemblies/crafting_cpu.snbt" />
  <IsometricCamera yaw="195" pitch="10" />
</GameScene>

## МЕГА [процессоры создания](ae2:items-blocks-machines/crafting_cpu_multiblock.md)

<Row>
  <BlockImage id="mega_crafting_unit" scale="4" />
  <BlockImage id="1m_crafting_storage" scale="4" />
  <BlockImage id="4m_crafting_storage" scale="4" />
  <BlockImage id="16m_crafting_storage" scale="4" />
  <BlockImage id="64m_crafting_storage" scale="4" />
  <BlockImage id="256m_crafting_storage" scale="4" />
</Row>

Как и с ячейками хранения, MEGA также предоставляет свои более крупные уровни хранения для процессоров создания. Хотя они тоже требуют
своей специальной версии <ItemLink id="ae2:crafting_unit" /> для учёта их роста мощности, они
всё равно легко справятся даже с самыми большими заданиями изготовления при большем объёме памяти, а также выглядят *чертовски круто* в
чёрном цвете.

<RecipeFor id="mega_crafting_unit" />
<RecipeFor id="1m_crafting_storage" />
<RecipeFor id="4m_crafting_storage" />
<RecipeFor id="16m_crafting_storage" />
<RecipeFor id="64m_crafting_storage" />
<RecipeFor id="256m_crafting_storage" />

В качестве дополнительного бонуса MEGA также предоставляет свой эквивалент <ItemLink id="ae2:crafting_accelerator" />, хотя с
преимуществом давать не один, а *ЧЕТЫРЕ* потока сопроцессора в пространстве каждого добавленного блока сопроцессора.

<BlockImage id="mega_crafting_accelerator" scale="4" />
<RecipeFor id="mega_crafting_accelerator" />

И просто ради полноты пакета также есть эквивалент MEGA для <ItemLink id="ae2:crafting_monitor" />.
Этот на самом деле не функционирует никак иначе обычного монитора, но служит дополнением к
вышеупомянутым блокам для пользователей, стремящихся сохранить эстетическую последовательность и тот же гладкий тёмный
вид всего мультиблока процессора.

<BlockImage id="mega_crafting_monitor" scale="4" />
<RecipeFor id="mega_crafting_monitor" />

## МЕГА поставщик шаблонов

<Row>
  <BlockImage id="mega_pattern_provider" scale="4" />
  <GameScene zoom="4" background="transparent">
    <ImportStructure src="assets/assemblies/cable_mega_pattern_provider.snbt" />
  </GameScene>
</Row>

Как компаньон к <ItemLink id="ae2:pattern_provider" />, **МЕГА поставщик шаблонов** поддерживает
тенденцию предоставления увеличенных вариантов соответствующих устройств AE2, удваивая ёмкость шаблонов, позволяя удерживать
18 шаблонов внутри и обрабатывать их. Это, однако, сопряжено с компромиссом: он может удерживать только
[**шаблоны обработки**](ae2:items-blocks-machines/patterns.md), поэтому с
<ItemLink id="ae2:molecular_assembler" /> он не очень-то будет работать.

<Row>
  <RecipeFor id="mega_pattern_provider" />
  <RecipeFor id="cable_mega_pattern_provider" />
</Row>
