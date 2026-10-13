---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация зарядника
  icon: charger
---

# Автоматизация зарядника

Обратите внимание, что, поскольку здесь используется <ItemLink id="pattern_provider" />, это предназначено для интеграции в вашу схему [автоматического изготовления](../ae2-mechanics/autocrafting.md).
Если вы просто хотите автоматизировать <ItemLink id="charger" /> отдельно, используйте воронки, сундуки и прочее.

Автоматизация <ItemLink id="charger" /> довольно проста. <ItemLink id="pattern_provider" /> помещает ингредиент в зарядник, затем [трубная подсеть](pipe-subnet.md)
или другая предметная труба помещает результат обратно в поставщика.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/charger_automation.snbt" />

<BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (1) Поставщик шаблонов: в своей конфигурации по умолчанию, с нужными шаблонами обработки. Также обеспечивает зарядник энергией.

        ![Шаблон зарядника](../assets/diagrams/charger_pattern_small.png)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 0" max="1 1.3 1">
        (2) Шина импорта: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (3) Шина хранения: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) в своей конфигурации по умолчанию, с нужными <ItemLink id="processing_pattern" />.
  Он также обеспечивает <ItemLink id="charger" /> [энергией](../ae2-mechanics/energy.md), поскольку работает как [кабель](../items-blocks-machines/cables.md).
  
    ![Шаблон зарядника](../assets/diagrams/charger_pattern.png)

* <ItemLink id="import_bus" /> (2) в своей конфигурации по умолчанию.
* <ItemLink id="storage_bus" /> (3) в своей конфигурации по умолчанию.

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в <ItemLink id="charger" />.
2. Зарядник делает своё зарядное дело.
3. <ItemLink id="import_bus" /> в зелёной подсети извлекает результат из зарядника и пытается хранить его в
   [сетевом хранилище](../ae2-mechanics/import-export-storage.md).
4. Единственное хранилище в зелёной подсети — <ItemLink id="storage_bus" />, который хранит полученные предметы в поставщике шаблонов, возвращая их в главную сеть.
