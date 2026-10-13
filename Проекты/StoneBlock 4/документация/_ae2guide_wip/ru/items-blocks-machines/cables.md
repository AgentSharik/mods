---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Кабели
  icon: fluix_glass_cable
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:white_glass_cable
- ae2:orange_glass_cable
- ae2:magenta_glass_cable
- ae2:light_blue_glass_cable
- ae2:yellow_glass_cable
- ae2:lime_glass_cable
- ae2:pink_glass_cable
- ae2:gray_glass_cable
- ae2:light_gray_glass_cable
- ae2:cyan_glass_cable
- ae2:purple_glass_cable
- ae2:blue_glass_cable
- ae2:brown_glass_cable
- ae2:green_glass_cable
- ae2:red_glass_cable
- ae2:black_glass_cable
- ae2:fluix_glass_cable
- ae2:white_covered_cable
- ae2:orange_covered_cable
- ae2:magenta_covered_cable
- ae2:light_blue_covered_cable
- ae2:yellow_covered_cable
- ae2:lime_covered_cable
- ae2:pink_covered_cable
- ae2:gray_covered_cable
- ae2:light_gray_covered_cable
- ae2:cyan_covered_cable
- ae2:purple_covered_cable
- ae2:blue_covered_cable
- ae2:brown_covered_cable
- ae2:green_covered_cable
- ae2:red_covered_cable
- ae2:black_covered_cable
- ae2:fluix_covered_cable
- ae2:white_covered_dense_cable
- ae2:orange_covered_dense_cable
- ae2:magenta_covered_dense_cable
- ae2:light_blue_covered_dense_cable
- ae2:yellow_covered_dense_cable
- ae2:lime_covered_dense_cable
- ae2:pink_covered_dense_cable
- ae2:gray_covered_dense_cable
- ae2:light_gray_covered_dense_cable
- ae2:cyan_covered_dense_cable
- ae2:purple_covered_dense_cable
- ae2:blue_covered_dense_cable
- ae2:brown_covered_dense_cable
- ae2:green_covered_dense_cable
- ae2:red_covered_dense_cable
- ae2:black_covered_dense_cable
- ae2:fluix_covered_dense_cable
- ae2:white_smart_cable
- ae2:orange_smart_cable
- ae2:magenta_smart_cable
- ae2:light_blue_smart_cable
- ae2:yellow_smart_cable
- ae2:lime_smart_cable
- ae2:pink_smart_cable
- ae2:gray_smart_cable
- ae2:light_gray_smart_cable
- ae2:cyan_smart_cable
- ae2:purple_smart_cable
- ae2:blue_smart_cable
- ae2:brown_smart_cable
- ae2:green_smart_cable
- ae2:red_smart_cable
- ae2:black_smart_cable
- ae2:fluix_smart_cable
- ae2:white_smart_dense_cable
- ae2:orange_smart_dense_cable
- ae2:magenta_smart_dense_cable
- ae2:light_blue_smart_dense_cable
- ae2:yellow_smart_dense_cable
- ae2:lime_smart_dense_cable
- ae2:pink_smart_dense_cable
- ae2:gray_smart_dense_cable
- ae2:light_gray_smart_dense_cable
- ae2:cyan_smart_dense_cable
- ae2:purple_smart_dense_cable
- ae2:blue_smart_dense_cable
- ae2:brown_smart_dense_cable
- ae2:green_smart_dense_cable
- ae2:red_smart_dense_cable
- ae2:black_smart_dense_cable
- ae2:fluix_smart_dense_cable
---

# Кабели

<GameScene zoom="3" background="transparent">
  <ImportStructure src="../assets/assemblies/cables.snbt" />
  <IsometricCamera yaw="180" pitch="30" />
</GameScene>

Хотя МЭ-сети также создаются смежными машинами, способными на МЭ, кабели — главный способ
расширения МЭ-сети на большие области.

Кабели разных цветов можно использовать, чтобы соседние кабели не соединялись друг с другом,
позволяя более эффективно распределять [каналы](../ae2-mechanics/channels.md). Они также влияют на цвет подключённых к ним терминалов,
так что не все ваши терминалы должны быть фиолетовыми. Флюисовые кабели соединяются со всеми остальными цветами.

Показательно, что **КАНАЛЫ НЕ ИМЕЮТ НИКАКОГО ОТНОШЕНИЯ К ЦВЕТУ КАБЕЛЯ**

## Важное примечание

**Если вы новичок в AE2 и не знакомы с каналами, используйте умный кабель и плотный умный кабель везде, где можно.
Он покажет, как каналы направляются по вашей сети, делая их поведение более понятным.**

## Ещё одно примечание

**Это не трубы предметов, жидкостей, энергии и т.д.** У них нет внутреннего инвентаря, поставщики шаблонов и машины не «выталкивают»
в них, всё, что они делают, — соединяют [устройства](../ae2-mechanics/devices.md) AE2 вместе в сеть.

## Стеклянный кабель

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/fluix_glass_cable.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="fluix_glass_cable" /> — самый простой в изготовлении кабель, передаёт энергию
и до 8 [каналов](../ae2-mechanics/channels.md). Он бывает 17 различных цветов, по умолчанию
флюисовый, и может быть окрашен в любой цвет с использованием любого из 16 красителей.

Чтобы создать цветные кабели, окружите краситель любого типа 8 кабелями одного
типа (цвет кабелей не имеет значения, но они должны быть одного типа:
стеклянные, умные и т.д.). Можно также красить кабели любой совместимой с Forge кистью для краски
в мире.

Можно создать любой цветной кабель с ведром воды, чтобы смыть краситель.

Можно покрыть кабель шерстью, чтобы создать <ItemLink id="fluix_covered_cable" />, и создать <ItemLink id="fluix_smart_cable" />, чтобы лучше понимать, что происходит с
вашими [каналами](../ae2-mechanics/channels.md).

<RecipeFor id="fluix_glass_cable" />

<RecipeFor id="blue_glass_cable" />

## Покрытый кабель

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Вариант покрытого кабеля не предлагает игровых преимуществ по сравнению с аналогом <ItemLink id="fluix_glass_cable" />. Однако его можно использовать
как альтернативный эстетичный выбор, если вы предпочитаете покрытый вид.

Может быть окрашен так же, как <ItemLink id="fluix_glass_cable" />. Четыре <ItemLink id="fluix_covered_cable" /> можно создать с
красным камнем и светящимся камнем, чтобы сделать <ItemLink id="fluix_covered_dense_cable" />.

<Recipe id="network/cables/covered_fluix" />

<RecipeFor id="blue_covered_cable" />

## Плотный кабель

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_covered_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Кабель большей вместимости, может переносить 32 канала в отличие от стандартного кабеля, который может переносить только 8,
однако он не поддерживает шины, поэтому сначала нужно перейти с плотного на
меньший кабель (например, <ItemLink id="fluix_glass_cable" /> или <ItemLink id="fluix_smart_cable" />) перед использованием шин или
панелей.

Плотные кабели слегка переопределяют поведение каналов «кратчайший путь»: каналы пойдут кратчайшим путём к
плотному кабелю, а затем кратчайшим путём через этот плотный кабель к контроллеру.

<Recipe id="network/cables/dense_covered_fluix" />

<RecipeFor id="blue_covered_dense_cable" />

## Умный кабель

<Row>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/fluix_smart_dense_cable.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
</Row>

Несмотря на некоторое сходство по внешнему виду с <ItemLink id="fluix_covered_cable" />, они
обеспечивают диагностическую функцию, визуализируя использование каналов на кабелях;
каналы появляются как освещённые цветные линии, которые проходят вдоль чёрной полосы на
кабелях, давая вам понимание того, как ваши каналы используются в
вашей сети. Для обычных умных кабелей первые четыре канала показываются линиями под цвет
кабеля, следующие четыре — белыми линиями. Для плотного умного кабеля каждая полоса соответствует 4 каналам.

В сетях с <ItemLink id="controller" /> линии на кабелях показывают точный путь, который проходят каналы.

Умные кабели в спонтанных сетях вместо этого будут показывать число каналов, используемых по всей сети, а не число каналов, проходящих через конкретный кабель.

Их также можно окрашивать так же, как <ItemLink id="fluix_glass_cable" />.

<Recipe id="network/cables/smart_fluix" />

<Recipe id="network/cables/dense_smart_fluix" />

<RecipeFor id="blue_smart_cable" />
