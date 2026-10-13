---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: МЭ пороговый излучатель уровня
    icon: extendedae:threshold_level_emitter
categories:
- extended devices
item_ids:
- extendedae:threshold_level_emitter
---

# МЭ пороговый излучатель уровня

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../structure/cable_threshold_level_emitter.snbt"></ImportStructure>
</GameScene>

Он работает как RS-триггер. Он выключает сигнал редстоуна, когда количество предмета в сети меньше
нижнего порога, и включает его, когда количество больше верхнего порога.

Например, предположим, нижний порог установлен на 100, а верхний порог — на 150.

Изначально сеть пуста, поэтому излучатель неактивен.

По мере роста количества предмета за 150 излучатель посылает сигнал редстоуна.

Когда количество опускается ниже 150, излучатель всё ещё посылает сигнал.

Наконец, когда количество падает ниже 100, излучатель выключается.
