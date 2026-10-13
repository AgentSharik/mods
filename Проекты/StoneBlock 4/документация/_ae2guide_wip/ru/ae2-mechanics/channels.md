---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Каналы
  icon: controller
---

# Каналы

МЭ-сетям в Applied Energistics 2 требуются
каналы для поддержки [устройств](../ae2-mechanics/devices.md), использующих сетевое хранилище или другие сетевые
службы. Представьте каналы как USB-кабели к вашим устройствам. У компьютера ограниченное число USB-портов, и он поддерживает
лишь ограниченное число подключённых устройств. Большинство машин, полноблочных устройств и стандартных кабелей могут пропускать сквозь себя не более
8 каналов. Полноблочные устройства и стандартные кабели можно представить как пучок из 8 «канальных проводов». Однако [плотные кабели](../items-blocks-machines/cables.md#dense-cable) могут поддерживать до
32 каналов. Единственные другие устройства, способные передавать 32 канала, — <ItemLink id="me_p2p_tunnel" />
и [Квантовый сетевой мост](../items-blocks-machines/quantum_bridge.md). Каждый раз, когда устройство использует канал, представьте, что с пучка снимается USB-«провод»,
а значит, дальше по линии этот «провод» недоступен.

<GameScene zoom="7" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_demonstration_1.snbt" />

  <LineAnnotation color="#33ff33" from="1 .4 .7" to="2.4 .4 .7" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .7" to="2.4 .6 .7" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .6" to="2.6 .4 .6" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .6" to="2.6 .6 .6" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .6" to="2.6 .6 .6" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.4 .6 .7" to="2.4 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.4 .4 .7" to="2.4 .4 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .6 .6" to="2.6 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .4 .6" to="2.6 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.1 .6 1.5" to="2.4 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .4 1.5" to="2.9 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.6 .6 1.5" to="2.6 .9 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.4 .1 1.5" to="2.4 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1 .6 .4" to="3.5 .6 .4" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .4" to="3.5 .4 .4" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="3.5 .6 .4" to="3.5 .9 .4" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="3.5 .1 .4" to="3.5 .4 .4" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1 .6 .3" to="1.5 .6 .3" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .3" to="1.5 .4 .3" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1.5 .6 .3" to="1.5 .9 .3" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1.5 .1 .3" to="1.5 .4 .3" alwaysOnTop={true}/>

  <LineAnnotation color="#ff3333" from="3.5 .5 .5" to="5.5 .5 .5" alwaysOnTop={true}>
  Все 8 каналов в кабеле использованы, поэтому накопитель не получает канал.  
  </LineAnnotation>

  <LineAnnotation color="#993333" from="1 .5 .5" to="1.25 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="1.5 .5 .5" to="1.75 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="2 .5 .5" to="2.25 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="2.5 .5 .5" to="2.75 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="3 .5 .5" to="3.25 .5 .5" alwaysOnTop={true}/>

  <DiamondAnnotation pos="3.6 0.5 0.5" color="#ff0000">
        Все 8 каналов в кабеле использованы, поэтому накопитель не получает канал.
    </DiamondAnnotation>

  <IsometricCamera yaw="15" pitch="30" />
</GameScene>

Простой способ увидеть, как каналы используются и направляются по вашей сети, — [умные кабели](../items-blocks-machines/cables.md): они показывают пути и использование каналов прямо на себе.

Каналы потребляют 1⁄128 AE/тик на каждый узел, который они пересекают; это означает, что добавление <ItemLink id="controller" /> в
сеть с 8 устройствами и более чем 96 узлами может на самом деле
уменьшить потребление энергии, поскольку меняется способ распределения каналов.

Важное примечание: **КАНАЛЫ НЕ ИМЕЮТ НИКАКОГО ОТНОШЕНИЯ К ЦВЕТУ КАБЕЛЯ** — цвет кабеля делает только то, что кабели не соединяются.

## Маршрутизация каналов

При использовании <ItemLink id="controller" />
каналы проходят в 3 шага. Сначала они идут кратчайшим путём через смежные машины к ближайшему [обычному кабелю](../items-blocks-machines/cables.md)
(стеклянному, покрытому или умному). Затем они идут кратчайшим путём через этот обычный кабель к ближайшему [плотному кабелю](../items-blocks-machines/cables.md)
(плотному или плотному умному). Затем идут кратчайшим путём через этот плотный кабель к <ItemLink id="controller" />.
Если кратчайший путь уже загружен до предела, некоторые [устройства](devices.md) могут не получить требуемые каналы; используйте
цветные кабели, кабельные якоря и тоннели в свою пользу, чтобы каналы шли тем путём, который вам нужен.

Например, в этом случае некоторые накопители не получают каналов, потому что, хотя пропускной способности кабелей достаточно,
каналы пытаются идти кратчайшим путём, перегружая одни кабели и оставляя другие пустыми.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_path_length_issue.snbt" />

  <LineAnnotation color="#33ff33" from="3 .5 1.4" to="0.4 0.5 1.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 .5 1.4" to="0.4 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 0.5 3.6" to="1.4 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.4 0.5 3.6" to="1.4 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="3 0.5 3.6" to="1.6 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.6 0.5 3.6" to="1.6 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#ff3333" from="3 .5 1.6" to="0.6 .5 1.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="0.6 .5 1.6" to="0.6 .5 3.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="0.6 .5 3.4" to="1.4 .5 3.4" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#ff3333" from="3 .5 3.4" to="1.6 .5 3.4" alwaysOnTop={true} thickness="0.05"/>

  <BoxAnnotation color="#dddddd" min="1.2 0.2 3.2" max="1.8 0.8 3.8" alwaysOnTop={true} thickness="0.05">
        Больше 8 каналов пытаются пройти здесь, поэтому некоторые отрезаются.
  </BoxAnnotation>

  <IsometricCamera yaw="90" pitch="90" />

</GameScene>

Это можно исправить, тщательнее ограничивая пути, которые могут занять каналы. Сети должны быть древовидными (или кустовидными).
Петли и неоднозначные маршруты каналов должны быть сведены к минимуму.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_path_length_issue_fix.snbt" />

  <LineAnnotation color="#33ff33" from="3 .5 1.4" to="0.4 0.5 1.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 .5 1.4" to="0.4 0.5 5.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 0.5 5.6" to="1 0.5 5.6" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="3 0.5 3.6" to="1.6 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.6 0.5 3.6" to="1.6 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <IsometricCamera yaw="90" pitch="90" />

</GameScene>

## Спонтанные сети

Сеть без <ItemLink id="controller" />
считается спонтанной и может поддерживать до 8 устройств, использующих каналы.
Как только вы превысите 8 устройств, такие устройства в сети отключатся,
и вы сможете либо удалить устройства, либо добавить <ItemLink id="controller" />.

В отличие от сетей с контроллером, [умные кабели](../items-blocks-machines/cables.md) в спонтанных сетях показывают число
каналов, используемых по всей сети, а не число каналов, проходящих через конкретный кабель.

При использовании спонтанных сетей каждое устройство будет
использовать 1 канал по всей сети; это очень отличается от того, как <ItemLink id="controller" /> распределяет каналы на основе
кратчайшего маршрута.

## Дизайн

Как уже говорилось в [маршрутизации каналов](channels.md#channel-routing), сеть лучше проектировать древовидной структурой: плотные кабели ветвятся от контроллера, обычные —
от плотных, а [устройства](../ae2-mechanics/devices.md) собраны кластерами по 8 или меньше на обычных кабелях.

Пример того, как делать не надо:

Идём по путям каналов,

1. Сразу при выходе из контроллера направо мы упираемся в ограничение в 8 каналов, потому что накопитель работает как обычный кабель.
Однако мы не используем здесь умный кабель, поэтому не можем увидеть, сколько каналов используется. Осталось 8 каналов.
2. Накопитель занимает канал.
Осталось 7 каналов.
3. 2 канала уходят вверх к терминалам.
Осталось 5 каналов.
4. Продолжая направо, интерфейс занимает ещё канал.
Осталось 4 канала.
5. 1 канал уходит вверх к поставщику шаблонов.
Осталось 3 канала.
6. Продолжая направо, 1 канал уходит вверх к шине импорта.
Осталось 2 канала.
7. Кластер поставщиков шаблонов, питающих сборщики, получает только 2 канала, поэтому 2 поставщика каналов не получают.

В конечном итоге ошибка — в «бутылочном горлышке» каналов и в непродуманном их распределении.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/bad_network_structure.snbt" />

<LineAnnotation color="#33ff33" from="6.5 .5 1.5" to="6 .5 1.5" alwaysOnTop={true} thickness="0.4">
  32 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="6 .5 1.5" to="5.5 .5 1.5" alwaysOnTop={true} thickness="0.2">
  8 каналов
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="5.5 1.5 1.5" alwaysOnTop={true} thickness="0.1">
  2 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="5.5 .3 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 1.5 1.5" to="5.5 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 2.5 1.5" to="5.5 2.5 1.1" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="4.5 .5 1.5" alwaysOnTop={true} thickness="0.158">
  5 каналов
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="4.5 .3 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="4.5 1.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="3.5 .5 1.5" alwaysOnTop={true} thickness="0.122">
  3 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 .5 1.5" to="3.5 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 2.5 1.5" to="3.7 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 .5 1.5" to="1.5 .5 1.5" alwaysOnTop={true} thickness="0.1">
  2 канала
</LineAnnotation>

<LineAnnotation color="#33ff33" from="1.5 0.5 1.5" to="1.5 0.3 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="1.5 0.5 1.5" to="0.5 0.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#33ff33" from="0.5 0.5 1.5" to="0.5 0.5 0.5" alwaysOnTop={true} thickness="0.071">
  1 канал
</LineAnnotation>

<LineAnnotation color="#ff3333" from="0.5 1.5 1.5" to="0.5 1.3 1.5" alwaysOnTop={true} thickness="0.071">
  нет каналов
</LineAnnotation>

<LineAnnotation color="#ff3333" from="1.5 1.5 0.5" to="1.5 1.3 0.5" alwaysOnTop={true} thickness="0.071">
  нет каналов
</LineAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

---

Пример хорошей структуры:

<GameScene zoom="2.5" interactive={true}>
  <ImportStructure src="../assets/assemblies/treelike_network_structure.snbt" />

    <BoxAnnotation color="#dddddd" min="6.9 0 4.9" max="9.1 4 7.1" thickness="0.05">
        Обратите внимание, что поставщики шаблонов находятся в отдельных группах по 8.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5 4 4" max="8 5 5" thickness="0.05">
        Два полных обычных кабеля, сходящихся вместе, означают, что нужен плотный кабель.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5 0 13" max="8 1 14" thickness="0.05">
        Используются разные цвета кабелей, чтобы соседние не соединялись.
    </BoxAnnotation>


  <IsometricCamera yaw="315" pitch="30" />
</GameScene>

## Режимы каналов

AE2 10.0.0 для Minecraft 1.18 вводит новые варианты изменения поведения каналов AE2 в вашем мире.
Есть новый параметр конфигурации в общем разделе (`channels`), управляющий этим, и новая внутриигровая
команда для операторов, чтобы менять режим и конфигурацию изнутри игры. Команда — `/ae2 channelmode <mode>`
для изменения и `/ae2 channelmode` для показа текущего режима. Когда режим меняется в игре, все существующие сети
перезагружаются и сразу используют новый режим.

Это возрождает и улучшает вариант, который был доступен в Minecraft 1.12, и вводит лучшие варианты для
игроков, которым нужен просто чуть более расслабленный геймплей, но без полного удаления механики.

В таблице ниже перечислены доступные режимы как для файла конфигурации, так и для команды.

| Настройка    | Описание                                                                                                                                                                                                                               |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `default`  | Стандартный режим с пропускной способностью каналов кабелей и спонтанных сетей, как описано на этом сайте                                                                                                                           |
| `x2`       | Все пропускные способности каналов удвоены (16 на обычном кабеле, 64 на плотном кабеле, спонтанные сети поддерживают 16 каналов)                                                                                                                           |
| `x3`       | Все пропускные способности каналов утроены (24 на обычном кабеле, 92 на плотном кабеле, спонтанные сети поддерживают 24 канала)                                                                                                                           |
| `x4`       | Все пропускные способности каналов учетверены (32 на обычном кабеле, 128 на плотном кабеле, спонтанные сети поддерживают 32 канала)                                                                                                                       |
| `infinite` | Все ограничения каналов сняты. Контроллеры всё равно *значительно* снижают энергопотребление сетей. Умные кабели переключаются только между полностью выключено (каналы не переносятся) и полностью включено (переносится 1 или более каналов). |
