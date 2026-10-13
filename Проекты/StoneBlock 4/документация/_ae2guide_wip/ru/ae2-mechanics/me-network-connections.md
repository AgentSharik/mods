---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Сетевые подключения
  icon: fluix_glass_cable
---

# Сетевые подключения

## Что значит «сеть»?

«Сеть» — это группа [устройств](../ae2-mechanics/devices.md), связанных блоками, способными пропускать [каналы](../ae2-mechanics/channels.md),
например [кабелями](../items-blocks-machines/cables.md) или полноблочными машинами и [устройствами](../ae2-mechanics/devices.md). 
(<ItemLink id="charger" />, <ItemLink id="interface" />, <ItemLink id="drive" /> и т.д.)
Строго говоря, даже один кабель — уже сеть.

## Отступление о расположении устройств

Для [устройств](../ae2-mechanics/devices.md) с конкретной сетевой функцией (например, <ItemLink id="interface" />,
помещающий в [сетевое хранилище](../ae2-mechanics/import-export-storage.md) и извлекающий из него, <ItemLink id="level_emitter" />,
читающий содержимое сетевого хранилища, <ItemLink id="drive" />, являющийся сетевым хранилищем, и т.д.)
физическое положение устройства не имеет значения.

Повторю: **физическое положение устройства не имеет значения**. Важно только то, что устройство подключено к сети
(и, конечно, к какой именно сети оно подключено).

## Сетевые подключения

Простой способ определить, что подключено к сети, — <ItemLink id="network_tool" />. Он покажет каждый
компонент в сети, так что если вы видите лишнее или не видите нужное — у вас проблема.

Например, здесь 2 отдельные сети.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        Сеть 1
  </BoxAnnotation>

<BoxAnnotation color="#5CA7CD" min="2 0 0" max="3 2 2">
        Сеть 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Здесь тоже 2 отдельные сети, потому что <ItemLink id="quartz_fiber" /> передаёт [энергию](../ae2-mechanics/energy.md),
не обеспечивая сетевого подключения.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        Сеть 1
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="1.3 0 0" max="3 2 2">
        Сеть 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

А вот здесь всего 1 сеть, а не 2 отдельные. [Квантовый мост](../items-blocks-machines/quantum_bridge.md) работает как
беспроводной [плотный кабель](../items-blocks-machines/cables.md#dense-cable), поэтому оба конца находятся в одной сети.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="7 3 3">
        Вся сеть одна
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Здесь тоже всего 1 сеть, поскольку цвет [кабеля](../items-blocks-machines/cables.md) не имеет отношения к сетевым подключениям, кроме того, что кабели разных цветов
не соединяются друг с другом. Все цвета соединяются с флюисовыми (или «бесцветными») кабелями.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        Вся сеть одна
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Подключения в контексте подсетей

[Подсети](../ae2-mechanics/subnetworks.md) используют сетевые подключения (а точнее — их **отсутствие**),
чтобы ограничивать, какие [устройства](../ae2-mechanics/devices.md) имеют доступ к каким другим устройствам.

Вся подсеть, по сути, — это просто отдельная сеть.

Например, возьмите [автоматический обогатитель руды](../example-setups/ore-fortuner.md). Здесь 3 отдельные сети,
и каждая служит своей цели в схеме.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 2" max="3 1 3">
        Сеть 1 работает как трубная подсеть и ограничивает доступ шины импорта, чтобы та «складывала» блоки руды через
        плоскости формирования.
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="0 0 0" max="3 1 1">
        Сеть 2 работает как ещё одна трубная подсеть и ограничивает доступ плоскостей уничтожения, чтобы те складывали
        обогащённые куски руды в бочку, а не в вашу главную сеть. Она также означает, что они не занимают каналы в
        главной сети.
  </BoxAnnotation>

  <BoxAnnotation color="#82CD5C" min="2 0 1" max="4 1 2">
        Сеть 3 — главная сеть со всем вашим хранилищем и изготовлением. По сути здесь она только подаёт питание и специально
        *не* подключена к двум подсетям.
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Подключения в контексте P2P

Один из вариантов [тоннеля P2P](../items-blocks-machines/p2p_tunnels.md) переносит [каналы](channels.md), а не предметы, жидкости
или сигнал красного камня, и это почему-то сбивает людей с толку. Сеть, на которой закреплён тоннель, не имеет отношения к
сети, которую тоннель переносит. Они *могут* быть одной сетью, но не обязаны, и обычно не являются.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_channels_network_connection.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1.98 2 1">
        Сеть 1 — переносимая сеть (обычно ваша главная сеть)
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="2.02 0 0" max="3.98 1 1">
        Сеть 2 — сеть, на которой работают тоннели МЭ P2P (обычно *не* ваша главная сеть)
  </BoxAnnotation>

  <BoxAnnotation color="#915dcd" min="4.02 0 0" max="6 1 1">
        Сеть 1 — переносимая сеть (обычно ваша главная сеть)
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Менее очевидные подключения

В этом случае здесь всего 1 сеть, потому что <ItemLink id="pattern_provider" />, будучи полноблочным устройством, работает как
кабель, и <ItemLink id="inscriber" /> делает подобное. Таким образом, сетевое подключение проходит через
поставщика и высекатель.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        Вся сеть одна
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Чтобы не допустить этого (полезно для многих схем автоматизации с [подсетями](../ae2-mechanics/subnetworks.md)),
можно щёлкнуть по поставщику правой кнопкой с <ItemLink id="certus_quartz_wrench" />, чтобы сделать его направленным; тогда он
не будет пропускать каналы через одну из сторон.

<Row gap="40">
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1.98 2 2">
        Сеть 1
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="2.02 0 0" max="4 2 2">
        Сеть 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_directional_connection.snbt" />

  <BoxAnnotation color="#ee3333" min="1 .3 .3" max="1.3 .7 .7">
        Обратите внимание, как кабель не соединяется
  </BoxAnnotation>

  <IsometricCamera yaw="255" pitch="30" />
</GameScene>
</Row>

Другие части, не обеспечивающие направленных сетевых подключений, — большинство [подчастей](../ae2-mechanics/cable-subparts.md)
[устройств](../ae2-mechanics/devices.md), например <ItemLink id="import_bus" />, <ItemLink id="storage_bus" /> и
<ItemLink id="cable_interface" />.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/subpart_no_connection.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>
