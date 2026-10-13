---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Квантовый мост
  icon: quantum_ring
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:quantum_link
- ae2:quantum_ring
---

# Квантовый сетевой мост

![Собранный Квантовый сетевой мост](../assets/diagrams/quantum_bridge_demonstration.png)

Квантовые сетевые мосты могут расширять [сеть](../ae2-mechanics/me-network-connections.md) на бесконечные расстояния и даже между измерениями.
Они могут переносить всего 32 канала (независимо от того, как кабели подключены к каждой стороне), по сути
работая как беспроводной [плотный кабель](cables.md#dense-cable).

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/quantum_bridge_internal_structure_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/quantum_bridge_internal_structure_2.snbt" />

  <BoxAnnotation color="#33dd33" min="1 1 1" max="6 2 3">
        Воображаемый кабель между двумя концами
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Показательно, что **обе стороны должны быть загружены в чанках**, поэтому нужно использовать <ItemLink id="spatial_anchor" /> или другой загрузчик чанков,
если две стороны далеко друг от друга.

# Квантовое кольцо

<BlockImage id="quantum_ring" scale="8" />

Восемь таких блоков, размещённых вокруг <ItemLink id="quantum_link" />, создадут
Квантовый сетевой мост. Только 4 блока <ItemLink id="quantum_ring" />, смежные с
<ItemLink id="quantum_link" />, будут принимать сетевые подключения,
4 угловых блока не могут соединяться с кабелями.

## Рецепт

<RecipeFor id="quantum_ring" />

# Камера квантовой связки

<BlockImage id="quantum_link" scale="8" />

Один такой блок, окружённый <ItemLink id="quantum_ring" />,
создаст Квантовый сетевой мост. Этот блок не соединяется ни с какими кабелями и регистрируется
как часть сети только когда полный мост собран.

Инвентарь этого блока может содержать только одну <ItemLink id="quantum_entangled_singularity" /> и
доступен для автоматизации.

## Рецепт

<RecipeFor id="quantum_link" />
