---
navigation:
  title: Автоматизация рецептов
  position: 2
  icon: "oritechthings:particle_accelerator_speed_sensor"
  parent: oritech:getting_started.md
item_ids:
  - oritechthings:particle_accelerator_speed_sensor
  - oritech:accelerator_sensor
  - oritechthings:advanced_target_designator
---
# Автоматизация рецептов

<Column alignItems="center" fullWidth={true}>
### <ItemImage id="oritechthings:particle_accelerator_speed_sensor" scale="0.75" /> <Color id="aqua">Автоматизация ускорителя частиц</Color> <ItemImage id="oritechthings:particle_accelerator_speed_sensor" scale="0.75" />
</Column>

---

Рецепт <Color id="aqua">ускорителя частиц</Color> завершается только тогда, когда второй предмет сталкивается с разогнанной частицей на нужной скорости. Для автоматизации нужны две вещи:

1. Определить момент, когда частица достигла скорости рецепта.
2. В этот точный момент выпустить предмет для столкновения.

<Color id="aqua">Датчик скорости</Color> выполняет шаг 1. Пара из <Color id="green">раздатчика</Color> и <Color id="green">воронки</Color> — шаг 2.

---

<Column alignItems="center" fullWidth={true}>
## <Color id="gold">Датчик скорости</Color>
</Column>

<Column alignItems="center" fullWidth={true}>
<ItemImage scale="3" id="oritechthings:particle_accelerator_speed_sensor" />
</Column>

<Color id="aqua">Датчик скорости</Color> выдаёт сигнал редстоуна, когда частица в связанном ускорителе достигает заданной скорости. Есть два режима:

- <Color id="green">Авто</Color> — датчик сам берёт необходимую скорость из активного рецепта ускорителя. Используйте этот режим для обычных рецептов.
- <Color id="yellow">Ручной</Color> — вы сами задаёте порог для особой редстоун-логики.

Переключатель ВКЛ/ВЫКЛ в интерфейсе определяет, выдаёт ли датчик сигнал вообще.

---

### <Color id="aqua">Привязка</Color>

Привязка работает так же, как у <Color id="gold">магнитного поля</Color>, — с помощью <ItemLink id="oritechthings:advanced_target_designator" />:

1. Нажмите ПКМ с зажатым Shift по <Color id="aqua">ускорителю частиц</Color>, держа целеуказатель, чтобы сохранить его позицию.
2. Разместите <Color id="aqua">датчик скорости</Color> в любом месте в пределах <Color id="green">128 блоков</Color>.
3. Нажмите ПКМ с зажатым Shift по датчику скорости тем же целеуказателем, чтобы привязать его.

После привязки в интерфейсе датчика появятся координаты связанного ускорителя.

<RecipeFor id="oritechthings:particle_accelerator_speed_sensor" />

---

<Column alignItems="center" fullWidth={true}>
## <Color id="gold">Подача предмета для столкновения</Color>
</Column>

<Color id="green">Раздатчик</Color>, установленный вплотную к грани <Color id="aqua">ускорителя частиц</Color>, <Color id="red">не</Color> помещает предметы в его инвентарь. Вместо этого он просто выбрасывает предмет в мир.

Решение: поставьте <Color id="green">воронку</Color> между раздатчиком и ускорителем. Воронка подберёт выпавший предмет и обычным образом передаст его в инвентарь ускорителя.

<Column alignItems="center" fullWidth={true}>
<Row>
  <ItemImage scale="2" id="minecraft:dispenser" />
  <ItemImage scale="2" id="minecraft:hopper" />
  <ItemImage scale="2" id="oritech:accelerator_controller" />
</Row>
</Column>

Направьте раздатчик так, чтобы выброшенный предмет падал на воронку, а воронку — в ускоритель. Загрузите в раздатчик предмет для столкновения.

---

<Column alignItems="center" fullWidth={true}>
## <Color id="gold">Собираем всё вместе</Color>
</Column>

1. Постройте рабочий ускоритель (см. [«Основы»](oritech:items/the_basics.md)).
2. Установите и привяжите <ItemLink id="oritechthings:particle_accelerator_speed_sensor" />, оставив его в режиме <Color id="green">Авто</Color>.
3. Установите <Color id="green">раздатчик</Color> и <Color id="green">воронку</Color> так, чтобы они подавали предметы в одну из входных граней ускорителя. Загрузите в раздатчик предмет для столкновения.
4. Подключите выход редстоуна датчика скорости к раздатчику.
5. Вставьте первый ингредиент в ускоритель и подайте питание на двигатели. Как только будет достигнута скорость рецепта, система автоматически подаст второй ингредиент.

Для сравнения: обычный <ItemLink id="oritech:accelerator_sensor" /> выдаёт только аналоговый сигнал, пропорциональный скорости частицы. Его тоже можно использовать, но потребуется компаратор и ручная настройка порога для каждого рецепта. Автоматический режим <Color id="aqua">датчика скорости</Color> избавляет от этой работы.
