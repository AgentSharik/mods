---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Рост истинного кварца
  icon: quartz_cluster
---

# Рост истинного кварца

## В основном просто скопировано со страницы начала работы

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/budding_certus_1.snbt" />
</GameScene>

Бутоны истинного кварца вырастают на [цветущих блоках истинного кварца](../items-blocks-machines/budding_certus.md), подобно аметисту. Если разбить бутон, который ещё не вырос, из него выпадет одна <ItemLink id="certus_quartz_dust" /> — удача на это не влияет. Если разбить полностью выросший кластер, выпадет четыре
<ItemLink id="certus_quartz_crystal" />, и удача увеличит это число.

Всего 4 уровня цветущих блоков: безупречный, несовершенный, потресканный и повреждённый; изначально вы
находите их в [метеоритах](../ae2-mechanics/meteorites.md).

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_blocks.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Каждый раз, когда бутон вырастает на очередную стадию, цветущий блок с некоторым шансом понижается на один уровень и в конце концов превращается
в обычный блок истинного кварца. Их можно чинить (и создавать новые цветущие блоки), бросив цветущий блок (или
блок истинного кварца) в воду с одним или несколькими <ItemLink id="charged_certus_quartz_crystal" />.

<RecipeFor id="damaged_budding_quartz" />

Безупречные цветущие блоки не понижаются и генерируют кварц бесконечно. Однако их нельзя создать или переместить
киркой даже с шёлковым касанием. (Зато можно — через [пространственное хранилище](../ae2-mechanics/spatial-io.md).)

Сами по себе бутоны истинного кварца растут очень медленно. К счастью, <ItemLink id="growth_accelerator" /> сильно
ускоряет этот процесс, если стоит рядом с цветущим блоком. Сделать несколько таких — ваша первая задача.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_certus_2.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Из-за сложных взаимодействий каждая закрытая сторона цветущего блока замедляет суммарную скорость роста этого блока,
что в конце концов перевешивает эффект от большего числа ускорителей. Эмпирические тесты показывают следующее:

![Предметов/мин для соотношений](../assets/diagrams/certus_farm_speed_chart_1.png)

![Распространённые схемы](../assets/diagrams/certus_farm_speed_chart_2.png)

Если кварца не хватает ещё и на <ItemLink id="energy_acceptor" /> или <ItemLink id="vibration_chamber" />,
можно сделать <ItemLink id="crank" /> и пристроить её к концу ускорителя.

Автоматический сбор кварца [описан здесь](../example-setups/simple-certus-farm.md).
