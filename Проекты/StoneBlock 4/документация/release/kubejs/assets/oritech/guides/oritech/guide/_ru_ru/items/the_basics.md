---
navigation:
  title: Основы
  position: 1
  icon: "oritech:accelerator_ring"
  parent: oritech:getting_started.md
item_ids:
  - oritech:accelerator_ring
  - oritech:accelerator_motor
  - oritechthings:accelerator_magnetic_field
---
# Основы

<Column alignItems="center" fullWidth={true}>
### <ItemImage id="oritech:accelerator_controller" scale="0.75" /> <Color id="aqua">Основы</Color> <ItemImage id="oritech:accelerator_controller" scale="0.75" />

<GameScene zoom="2.5" interactive={true}>
  <ImportStructure src="oritech:assets/basic_accelerator.nbt" />
  <IsometricCamera yaw="225" pitch="30" />
  <DiamondAnnotation pos="0.5 0.5 1.5" color="#ff6d6dff">
  Для каждой установки нужны 2 ускорителя частиц. Они служат для ввода предметов, которые будут разгоняться. <ItemImage id="oritech:accelerator_controller" />
  </DiamondAnnotation>
  <DiamondAnnotation pos="3.5 0.5 2.5" color="#ff6d6dff">
  Магнитное поле ускорителя частиц размещается в центре и привязывается к основному ускорителю частиц с помощью продвинутого целеуказателя.
  
  Оно также поддерживает аддоны! <ItemImage id="oritechthings:accelerator_magnetic_field" />
  </DiamondAnnotation>
  <DiamondAnnotation pos="5.5 0.5 2.5" color="#ff6d6dff">
  Линейный двигатель ускорителя частиц размещается внутри кольца ускорителя. При подаче энергии он разгоняет частицу. <ItemImage id="oritech:accelerator_motor" />
  </DiamondAnnotation>
  <DiamondAnnotation pos="2.5 0.5 4.5" color="#ff6d6dff">
  Направляющие кольца ведут частицу по кругу. После установки по ним можно нажать ПКМ, чтобы изменить угол входа. <ItemImage id="oritech:accelerator_ring" />
  </DiamondAnnotation>
</GameScene>

#### *Базовая установка ускорителя частиц*
</Column>

---

Для настройки <Color id="aqua">ускорителя частиц</Color> вам понадобятся следующие предметы:

<ItemGrid>
  <ItemIcon id="oritech:accelerator_motor" />
  <ItemIcon id="oritech:accelerator_controller" />
  <ItemIcon id="oritech:accelerator_ring" />
  <ItemIcon id="oritechthings:accelerator_magnetic_field" />
  <ItemIcon id="oritechthings:advanced_target_designator" />
</ItemGrid>

---

<Column alignItems="center" fullWidth={true}>
<ItemImage scale="3" id="oritech:accelerator_controller" />
</Column>

<Color id="aqua">Ускорители частиц</Color> — основные машины любой установки ускорителя. Именно сюда вставляются предметы, которые попадут в кольцо ускорения. Для базовой установки нужны как минимум 2 <Color id="aqua">ускорителя частиц</Color>, оба соединены с <Color id="green">направляющим кольцом ускорителя частиц</Color>.

---

<Column alignItems="center" fullWidth={true}>
<ItemImage scale="3" id="oritech:accelerator_ring" />
</Column>
<Color id="green">Направляющие кольца ускорителя частиц</Color> ведут частицу по кругу. После установки по ним можно нажать ПКМ, чтобы изменить угол входа. Чем больше кольцо, тем больше направляющих вам понадобится.

---

<Column alignItems="center" fullWidth={true}>
<ItemImage scale="3" id="oritech:accelerator_motor" />
</Column>
<Color id="green">Линейный двигатель ускорителя частиц</Color> разгоняет частицы по кольцу. Для его работы нужна энергия. Одного двигателя достаточно, но чем их больше, тем быстрее разгоняется частица.

---

<Column alignItems="center" fullWidth={true}>
<ItemImage scale="3" id="oritechthings:accelerator_magnetic_field" />
</Column>
<Color id="gold">Магнитное поле ускорителя частиц</Color> размещается в центре установки. Чтобы привязать его, нажмите ПКМ с зажатым Shift по основному ускорителю частиц, держа <Color id="aqua">продвинутый целеуказатель</Color>, а затем ПКМ с зажатым Shift по магнитному полю.

Магнит также потребляет очень много энергии. Можно установить аддоны или удлинители аддонов машины — для небольших колец это необходимо, чтобы достичь 15 кДж.

---

Рекомендуемые аддоны для магнитного поля:

<ItemGrid>
  <ItemIcon id="oritechthings:addon_block_efficient_speed_tier_8" />
  <ItemIcon id="oritechthings:addon_block_capacitor_tier_8" />
</ItemGrid>

*Примечание: эта небольшая установка также может достичь 15 кДж с удлинителем аддонов машины и несколькими указанными выше аддонами.*



