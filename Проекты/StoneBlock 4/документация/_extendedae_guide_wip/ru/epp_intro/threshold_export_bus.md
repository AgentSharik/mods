---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: МЭ пороговая шина экспорта
    icon: extendedae:threshold_export_bus
categories:
- extended devices
item_ids:
- extendedae:threshold_export_bus
---

# МЭ пороговая шина экспорта

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../structure/cable_threshold_export_bus.snbt"></ImportStructure>
</GameScene>

МЭ пороговая шина экспорта работает, когда количество предмета, хранящегося в МЭ-сети, выше или ниже порога.

## Пример

![Интерфейс](../pic/thr_bus_gui1.png)

Порог меди установлен на 128, поэтому она экспортирует медь, когда количество меди, хранящейся в сети, превышает 128.

![Интерфейс](../pic/thr_bus_gui2.png)

Порог тот же, что выше, но режим установлен НИЖЕ. Она экспортирует медь, когда количество хранящейся меди ниже 128.
