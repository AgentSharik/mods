---
navigation:
  parent: appflux/appflux-index.md
  title: Приёмник флюкса
  icon: appflux:flux_accessor
categories:
- flux accessor
item_ids:
- appflux:flux_accessor
- appflux:part_flux_accessor
---

# Приёмник флюкса

<Row>
<BlockImage id="appflux:flux_accessor" scale="8"></BlockImage>
<GameScene zoom="8" background="transparent">
  <ImportStructure src="../structure/flux_accessor.snbt"></ImportStructure>
</GameScene>
</Row>

Приёмник флюкса может вводить/выводить энергию, хранящуюся в вашей МЭ-сети. По умолчанию у них нет ограничений ввода/вывода, что может быть 
изменено в конфиге Appflux.

У них быстрый и обычный режим. В быстром режиме они выводят энергию каждый тик, что может вызывать лаги при интенсивном использовании.
В обычном режиме они выводят энергию в зависимости от хранящейся энергии цели, что не вызовет проблем с лагами.

* Примечание: «энергия», упоминаемая здесь, — это FE, хранящаяся в ваших [ячейках хранения FE](./flux_cells.md), а не энергия в
[ячейках энергии](ae2:items-blocks-machines/energy_cells.md).
