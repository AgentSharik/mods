---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Тоннели P2P
  icon: me_p2p_tunnel
  position: 210
categories:
- devices
item_ids:
- ae2:me_p2p_tunnel
- ae2:redstone_p2p_tunnel
- ae2:item_p2p_tunnel
- ae2:fluid_p2p_tunnel
- ae2:fe_p2p_tunnel
- ae2:light_p2p_tunnel
---

# Тоннели «точка-точка»

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_tunnels.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Тоннели P2P — способ перемещать по сети такие вещи, как предметы, жидкости, сигналы красного камня, энергию, свет и [каналы](../ae2-mechanics/channels.md),
без их непосредственного взаимодействия с сетью. Вариантов тоннелей P2P много, но каждый
транспортирует только свой конкретный тип вещи. По сути они работают как порталы, напрямую соединяющие
две грани блоков на расстоянии. Они не двунаправленные, есть определённые входы и выходы.

![Портал](../assets/assemblies/p2p_portal.png)

Например, воронка, направленная на предметный P2P, будет работать так, как будто напрямую подключена к бочке, и предметы потекут.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_hopper_barrel.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Однако две бочки рядом друг с другом не будут передавать предметы между собой.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_barrel_barrel.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Есть также другие варианты, например красный камень P2P.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_redstone.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

И МЭ P2P, который перемещает каналы.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_channels.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Типы тоннелей P2P и настройка

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_tunnels.snbt" />
  <IsometricCamera yaw="180" pitch="90" />
</GameScene>

Типов тоннелей P2P много. Только тоннель МЭ P2P напрямую создаваемый, остальные делаются щелчком правой кнопкой по другим
тоннелям P2P определёнными предметами:
- Тоннели МЭ P2P выбираются щелчком правой кнопкой с любым [кабелем](../items-blocks-machines/cables.md).
- Тоннели красного камня P2P выбираются щелчком правой кнопкой с различными компонентами красного камня.
- Предметные тоннели P2P выбираются щелчком правой кнопкой с сундуком или воронкой.
- Жидкостные тоннели P2P выбираются щелчком правой кнопкой с ведром или флаконом.
- Энергетические тоннели P2P выбираются щелчком правой кнопкой почти с любым предметом, содержащим энергию.
- Световые тоннели P2P выбираются щелчком правой кнопкой с факелом или светящимся камнем

У некоторых типов тоннелей есть особенности. Например, каналы тоннелей МЭ P2P не могут проходить через другие тоннели МЭ P2P, а
энергетические тоннели P2P косвенно взимают налог 2,5% с FE, проходящей через них, увеличивая своё потребление
[энергии](../ae2-mechanics/energy.md).

## Самая используемая форма P2P

Самый распространённый случай использования тоннелей P2P — использование тоннеля МЭ P2P для уплотнения плотности транспортировки [каналов](../ae2-mechanics/channels.md).
Вместо пучка плотного кабеля можно использовать один плотный кабель, чтобы переносить много каналов.

В этом примере 8 входов МЭ P2P берут 256 каналов (8*32) из <ItemLink id="controller" /> главной сети, а 8 выходов МЭ P2P 
выводят их куда-то ещё. Обратите внимание, как каждый вход или выход тоннеля P2P занимает 1 канал. Таким образом мы можем прогнать много каналов 
через тонкий кабель. И поскольку наши тоннели P2P находятся в выделенной [подсети](../ae2-mechanics/subnetworks.md), мы даже
не используем для этого каналы главной сети! Также обратите внимание, что, хотя тоннели P2P можно размещать прямо
на контроллере, между ними можно поставить [плотный умный кабель](../items-blocks-machines/cables.md#smart-cable), чтобы легче визуализировать каналы.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/p2p_compact_channels.snbt" />

  <BoxAnnotation color="#dddddd" min="1.3 1.3 6.3" max="2 2.7 6.7">
        Кварцевое волокно делит энергию между главной сетью и подсетью P2P.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4.1 0 5.7" max="5 2.3 6.4">
        Можно либо поставить вход тоннеля прямо на контроллер, либо подключить к нему кабелем.
  </BoxAnnotation>

  <IsometricCamera yaw="225" pitch="30" />
</GameScene>

Ещё один пример (включая его использование с [Квантовыми мостами](quantum_bridge.md)) смотрите на этой схеме в MS Paint, которую мне лень
подправлять:

![P2P и квантовые мосты](../assets/diagrams/p2p_quantum_network.png)

## Вложение

Однако это нельзя использовать, чтобы отправить бесконечные каналы через один кабель. Канал для тоннеля МЭ P2P не будет
проходить через другой тоннель МЭ P2P, поэтому рекурсивно вкладывать их нельзя. Обратите внимание, как внешний слой тоннелей МЭ P2P
на красных кабелях отключён. Обратите внимание, что это относится только к тоннелям МЭ P2P, другие типы тоннелей P2P могут проходить через тоннель МЭ P2P,
как видно по нормально работающим тоннелям красного камня P2P.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_nesting.snbt" />
  <IsometricCamera yaw="225" pitch="30" />
</GameScene>

## Связка

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_linking_frequency.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Концы соединения тоннеля P2P можно связать с помощью <ItemLink id="memory_card" />. Частота будет отображаться
как массив цветов 2x2 на задней стороне тоннеля.
- Щелчок правой кнопкой с Shift — сгенерировать новую частоту связки P2P.
- Щелчок правой кнопкой — вставить настройки, карты улучшений или частоту связки.

Тоннель, по которому вы щёлкнете правой кнопкой с Shift, будет входом, а тоннель, по которому щёлкнете правой кнопкой, — выходом. Можно иметь несколько выходов,
но с тоннелями МЭ P2P каналы, протекающие на вход, будут разделены между выходами, поэтому дублировать каналы нельзя.

## Рецепт

<RecipeFor id="me_p2p_tunnel" />
