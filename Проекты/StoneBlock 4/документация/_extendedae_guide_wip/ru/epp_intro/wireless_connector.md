---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: МЭ беспроводной соединитель
    icon: extendedae:wireless_connect
categories:
- extended devices
item_ids:
- extendedae:wireless_connect
- extendedae:wireless_tool
---

# МЭ беспроводной соединитель

<Row gap="20">
<BlockImage id="extendedae:wireless_connect" scale="6"></BlockImage>
<ItemImage id="extendedae:wireless_tool" scale="6"></ItemImage>
</Row>

МЭ беспроводной соединитель может соединять две сети подобно <ItemLink id="ae2:quantum_link" />, но у него ограниченная дальность и он не может
соединять через измерения. МЭ беспроводной соединитель поддерживает только соединения один-к-одному. Используйте <ItemLink id="extendedae:wireless_hub" />,
если нужны соединения многие-ко-многим.

## Соединение беспроводных соединителей

Используйте Набор беспроводной настройки МЭ, чтобы щёлкнуть по двум Беспроводным соединителям, которые вы хотите соединить.

Приседание + щелчок очищает текущую настройку Набора беспроводной настройки МЭ.

МЭ беспроводной соединитель меняет текстуру, когда соединение успешно установлено.

Несоединённые МЭ беспроводные соединители

<GameScene zoom="5" background="transparent">
  <ImportStructure src="../structure/wireless_connector_off.snbt"></ImportStructure>
</GameScene>

Соединённые МЭ беспроводные соединители

<GameScene zoom="5" background="transparent">
  <ImportStructure src="../structure/wireless_connector_on.snbt"></ImportStructure>
</GameScene>

## Цвет

Беспроводные соединители можно окрашивать как кабели, и они могут соединяться только с кабелями/соединителями того же цвета.

Для окраски соединителя нужен <ItemLink id="ae2:color_applicator" />.

Так вы можете настроить беспроводные соединители следующим образом:

<GameScene zoom="3" background="transparent" interactive={true}>
  <ImportStructure src="../structure/wireless_connector_setup.snbt"></ImportStructure>
</GameScene>

## Потребление энергии

МЭ беспроводные соединители потребляют тем больше энергии, чем дальше они друг от друга. Поскольку кривая стоимости от расстояния нелинейна,
стоимость энергии может стать очень высокой, когда они слишком далеко друг от друга.

Вы можете использовать <ItemLink id="ae2:energy_card" />, чтобы экономить энергию; каждая карта уменьшает стоимость на 10%.
