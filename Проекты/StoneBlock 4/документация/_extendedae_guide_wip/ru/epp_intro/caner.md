---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: МЭ наполнитель
    icon: extendedae:caner
categories:
- extended devices
item_ids:
- extendedae:caner
---

# МЭ наполнитель

<BlockImage id="extendedae:caner" scale="8"></BlockImage>

МЭ наполнитель — машина для «консервирования» материалов, включая жидкости, газ Mekanism, ману Botania и даже энергию!

Первый слот содержит наполняющий материал, второй слот содержит предмет, который нужно заполнить.

Для работы ему нужна энергия, и каждая операция стоит 80 AE.

![Интерфейс](../pic/caner_gui.png)

По умолчанию он заполняет только жидкости; установите соответствующий аддон, чтобы он заполнял другие материалы.

### Поддерживаемые аддоны:
- Applied Flux
- Applied Mekanistics
- Applied Botanics Addon

## Автоматическое изготовление с МЭ наполнителем

Только верхние и нижние стороны могут принимать энергию и подключаться к сети.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../structure/caner_example.snbt"></ImportStructure>
</GameScene>

Простая схема для МЭ наполнителя. МЭ наполнитель автоматически выбрасывает заполненный предмет, когда получает ингредиенты от <ItemLink id="ae2:pattern_provider" />.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../structure/caner_auto.snbt"></ImportStructure>
</GameScene>

Шаблон должен содержать только наполняющий материал и контейнер, который нужно заполнить. Вот несколько примеров:

Заполнить ведро воды:

![П1](../pic/fill_water.png)

Усилить энерготаблетку (требуется Applied Flux):

![П1](../pic/fill_energy.png)


## Расконсервирование

МЭ наполнитель также может в режиме Опустошения извлекать материалы из контейнера. Нужно поменять в шаблоне входы и выходы.
