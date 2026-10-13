---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: МЭ шина хранения по тегам
    icon: extendedae:tag_storage_bus
categories:
- extended devices
item_ids:
- extendedae:tag_storage_bus
---

# МЭ шина хранения по тегам

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../structure/cable_tag_storage_bus.snbt"></ImportStructure>
</GameScene>

МЭ шина хранения по тегам — <ItemLink id="ae2:storage_bus" />, фильтруемая по тегам предметов или жидкостей и поддерживающая некоторые базовые логические операторы.

Вот несколько примеров:

- Принимать только необработанную руду

c:raw_materials/*

- Принимать все слитки и самоцветы

c:ingots/* | c:gems/*
