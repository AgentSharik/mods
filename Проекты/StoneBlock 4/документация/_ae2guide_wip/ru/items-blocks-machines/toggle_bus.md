---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Шина переключения
  icon: toggle_bus
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:toggle_bus
- ae2:inverted_toggle_bus
---

# Шина переключения

<GameScene zoom="8" background="transparent">
<ImportStructure src="../assets/assemblies/toggle_bus.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

Шина, функционирующая подобно <ItemLink id="fluix_glass_cable" /> или другим кабелям, но она
позволяет переключать состояние её подключения сигналом красного камня. Это позволяет отрезать
часть [МЭ-сети](../ae2-mechanics/me-network-connections.md).

Когда сигнал красного камня подан, часть включает подключение; <ItemLink id="inverted_toggle_bus" /> обеспечивает обратное
поведение — вместо этого отключает подключение.

Обратите внимание, что переключение может вызвать перезагрузку сети и пересчёт подключённых устройств.

Это [подчасти кабелей](../ae2-mechanics/cable-subparts.md).

## Рецепты

<RecipeFor id="toggle_bus" />

<RecipeFor id="inverted_toggle_bus" />
