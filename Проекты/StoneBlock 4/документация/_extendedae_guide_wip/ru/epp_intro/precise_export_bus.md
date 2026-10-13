---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: МЭ точная шина экспорта
    icon: extendedae:precise_export_bus
categories:
- extended devices
item_ids:
- extendedae:precise_export_bus
---

# МЭ точная шина экспорта

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../structure/cable_precise_export_bus.snbt"></ImportStructure>
</GameScene>

МЭ точная шина экспорта экспортирует предметы или жидкости в заданных количествах. Она экспортирует только если контейнер может полностью принять весь выход.

## Пример

![Интерфейс](../pic/pre_bus_gui1.png)

Это экспортирует 3 булыжника за операцию. Экспорт прекращается, когда количество булыжника в сети меньше 3.

![Интерфейс](../pic/pre_bus_gui2.png)

Она также прекращает экспорт, когда целевой контейнер не может вместить весь выход. В сундуке помещаются только ещё 2 булыжника, поэтому шина экспорта останавливается.
