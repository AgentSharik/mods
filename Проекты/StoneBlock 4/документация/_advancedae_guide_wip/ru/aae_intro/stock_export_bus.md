---
navigation:
  parent: aae_intro/aae_intro-index.md
  title: Шина запаса экспорта
  icon: advanced_ae:stock_export_bus_part
categories:
  - advanced items
item_ids:
  - advanced_ae:stock_export_bus_part
---

# Шина запаса экспорта

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../structure/cable_stock_export_bus.snbt"></ImportStructure>
</GameScene>

Шина запаса экспорта может быть настроена для экспорта точного количества отфильтрованных стаков. Она отслеживает количество
, в данный момент присутствующее в целевом инвентаре, и не вставляет сверх этого числа. Для настройки откройте интерфейс, перетащите
нужный предмет в слот фильтра и средним щелчком можно настроить количество. Учтите, что она не будет
регулировать выход, то есть не извлечёт лишние предметы/жидкости из инвентаря, если они превысят настроенное
количество.
