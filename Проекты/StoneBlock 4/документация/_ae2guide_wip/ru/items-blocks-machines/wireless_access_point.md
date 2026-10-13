---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Беспроводная точка доступа
  icon: wireless_access_point
  position: 210
categories:
- devices
item_ids:
- ae2:wireless_booster
- ae2:wireless_access_point
---

# Беспроводная точка доступа

<BlockImage id="wireless_access_point" p:state="has_channel" scale="8" />

Позволяет беспроводной доступ через <ItemLink id="wireless_terminal" />.
Радиус и энергопотребление определяются числом установленных <ItemLink id="wireless_booster" />.

В сети может быть сколько угодно беспроводных точек доступа с любым числом
<ItemLink id="wireless_booster" /> в каждой, что позволяет оптимизировать энергопотребление
и радиус, изменяя свою схему.

Требуется [канал](../ae2-mechanics/channels.md).

Также используется для привязки [беспроводных терминалов](wireless_terminals.md)

# Беспроводной усилитель

<ItemImage id="wireless_booster" scale="2" />

Используется для увеличения радиуса беспроводной точки доступа.

## Рецепты

<RecipeFor id="wireless_access_point" />

<RecipeFor id="wireless_booster" />
