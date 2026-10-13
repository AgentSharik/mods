---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Контроллер
  icon: controller
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:controller
---

# Контроллер

<BlockImage id="controller" p:state="online" scale="8" />

Контроллер — маршрутизирующий узел [МЭ-сети](../ae2-mechanics/me-network-connections.md).
Без него сеть «спонтанная» и может иметь максимум 8 [устройств](../ae2-mechanics/devices.md), использующих каналы, всего.

Невозможно иметь 2 контроллера в одной [МЭ-сети](../ae2-mechanics/me-network-connections.md).

Контроллер даёт 32 [канала](../ae2-mechanics/channels.md) на сторону.

Для работы контроллеру нужно 6 AE/тик на блок контроллера. Каждый блок контроллера может хранить 8000 AE, поэтому более крупные сети могут потребовать дополнительное
хранилище энергии. Подробности см. в [энергии](../ae2-mechanics/energy.md).

Мультиблочные контроллеры можно строить в довольно свободной форме.

<GameScene zoom="2" background="transparent">
  <ImportStructure src="../assets/assemblies/controllers.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Однако есть несколько правил, которым нужно следовать:

1.  Все блоки контроллера в [МЭ-сети](../ae2-mechanics/me-network-connections.md) должны быть соединены; иначе блоки станут красными.
2.  Размер контроллера должен быть в пределах 7x7x7; иначе он станет красным.
3.  У контроллера может быть 2 смежных блока максимум по 1 оси; если блок нарушает это правило, он отключится и станет красным.

<GameScene zoom="2" background="transparent">
  <ImportStructure src="../assets/assemblies/controller_rules.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Пока все правила соблюдены и есть питание, контроллер должен светиться и
переливаться цветами.

Можно щёлкнуть правой кнопкой по контроллеру, чтобы получить тот же интерфейс, что и у <ItemLink id="network_tool" />

## Рецепт

<RecipeFor id="controller" />
