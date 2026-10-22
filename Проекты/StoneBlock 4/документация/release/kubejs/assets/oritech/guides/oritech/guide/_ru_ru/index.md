---
navigation:
  title: Обзор ускорителя частиц
  position: 1
  icon: "oritech:accelerator_controller"
item_ids:
  - oritech:accelerator_controller

---
# Обзор ускорителя частиц

<Column alignItems="center" fullWidth={true}>
### <ItemImage id="oritech:accelerator_controller" scale="0.75" /> <Color id="aqua">Ускоритель частиц</Color><ItemImage id="oritech:accelerator_controller" scale="0.75" />

---


<GameScene zoom="2.5" interactive={true}>
  <ImportStructure src="assets/particle_accelerator.nbt" />
  <RemoveBlocks id="minecraft:stone" />
  <IsometricCamera yaw="225" pitch="30" />
</GameScene>
#### *Пример установки ускорителя частиц для достижения 15 000 Дж*
</Column>

---

<Color id="aqua">Ускоритель частиц</Color> — это сложная установка, которая разгоняет предмет до столкновения с другим предметом. От скорости в момент <Color id="gold">столкновения</Color> зависит успех создания нового предмета.

Хотя <Color id="aqua">ускоритель частиц</Color> используется в нескольких рецептах, чаще всего он нужен для создания <Color id="gold">Чёрной дыры</Color>: <Color id="light_purple">Крошечный фрагмент Хаоса</Color> должен столкнуться с <Color id="light_purple">Гранулой антиматерии</Color> при энергии не менее <Color id="aqua">15 000 Дж</Color>.

<Column alignItems="center" fullWidth={true}>
## [Изучить основы](oritech:items/the_basics.md)
## [Автоматизация рецептов](oritech:items/automating_recipes.md)
</Column>

