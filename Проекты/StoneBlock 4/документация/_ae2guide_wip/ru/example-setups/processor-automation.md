---
navigation:
  parent: example-setups/example-setups-index.md
  title: Автоматизация производства процессоров
  icon: logic_processor
---

# Автоматизация производства процессоров

Есть много способов автоматизировать [процессоры](../items-blocks-machines/processors.md), и это один из них.

Эта общая схема может быть сделана с любым типом предметной логистической трубы, или канала, или воздуховода, или как его называет мод, пока
вы можете её фильтровать.

![Схема технологического потока](../assets/diagrams/processor_flow_diagram.png)

Здесь подробно описано, как сделать это только с AE2, используя [«трубные» подсети](pipe-subnet.md).

Обратите внимание, что, поскольку здесь используется <ItemLink id="pattern_provider" />, это предназначено для интеграции в вашу схему [автоматического изготовления](../ae2-mechanics/autocrafting.md).
Если вы просто хотите автоматизировать процессоры отдельно, замените поставщика шаблонов ещё одной бочкой и поместите ингредиенты прямо в верхнюю бочку.

Это, оказалось, обратно-совместимо
с предыдущими версиями AE2, потому что даже если <ItemLink id="inscriber" /> направленные, трубные подсети всё равно вставляют и
извлекают с правильных граней.

## Урок кодирования шаблонов

Часто [шаблон](../items-blocks-machines/patterns.md), который вам нужно закодировать, **НЕ БУДЕТ СОВПАДАТЬ С ТЕМ, ЧТО ВЫ ВИДИТЕ В JEI**, или с тем, что JEI выводит, когда вы щёлкаете кнопку +.
В данном случае JEI выведет 2 отдельных шаблона: один для печатных компонентов и один для финальной сборки, а шаблон печатных
компонентов будет включать [пресс](../items-blocks-machines/presses.md). Это не то, чего мы хотим, потому что это не то, что схема будет делать. Мы хотим 1 шаблон, который
вводит сырые ресурсы и выводит завершённый процессор, и поскольку пресс уже в высекателе, мы не должны помещать его в шаблон.

---

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/processor_automation.snbt" />

  <BoxAnnotation color="#dddddd" min="5 1 0" max="6 2 1" thickness=".05">
        (1) Поставщик шаблонов: в своей конфигурации по умолчанию, с нужными шаблонами обработки.

        <Row>
            ![Шаблон логического](../assets/diagrams/logic_pattern_small.png)
            ![Шаблон вычислительного](../assets/diagrams/calculation_pattern_small.png)
            ![Шаблон инженерного](../assets/diagrams/engineering_pattern_small.png)
        </Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4.7 2 0" max="5 3 1" thickness=".05">
        (2) Шина хранения №1: в своей конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 1 0" max="4.3 2 1" thickness=".05">
        (3) Шина экспорта №1: отфильтрована на Кремний, у неё 2 Карты ускорения
        <Row><ItemImage id="silicon" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 4 0" max="4.3 3 1" thickness=".05">
        (4) Шина экспорта №2: отфильтрована на Слиток золота, у неё 2 Карты ускорения
        <Row><ItemImage id="minecraft:gold_ingot" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 5 0" max="4.3 4 1" thickness=".05">
        (5) Шина экспорта №3: отфильтрована на Кристалл истинного кварца, у неё 2 Карты ускорения
        <Row><ItemImage id="certus_quartz_crystal" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 6 0" max="4.3 5 1" thickness=".05">
        (6) Шина экспорта №4: отфильтрована на Алмаз, у неё 2 Карты ускорения
        <Row><ItemImage id="minecraft:diamond" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.3 3 0" max="2 2 1" thickness=".05">
        (7) Шина экспорта №5: отфильтрована на Пыль красного камня, у неё 2 Карты ускорения
        <Row><ItemImage id="minecraft:redstone" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 1 0" max="3 2 1" thickness=".05">
        (8) Высекатель №1: в своей конфигурации по умолчанию. У него Кремниевый пресс и 4 Карты ускорения
        <Row><ItemImage id="silicon_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 3 0" max="3 4 1" thickness=".05">
        (9) Высекатель №2: в своей конфигурации по умолчанию. У него Логический пресс и 4 Карты ускорения
        <Row><ItemImage id="logic_processor_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 4 0" max="3 5 1" thickness=".05">
        (10) Высекатель №3: в своей конфигурации по умолчанию. У него Вычислительный пресс и 4 Карты ускорения
        <Row><ItemImage id="calculation_processor_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="4 5 0" max="3 6 1" thickness=".05">
        (11) Высекатель №4: в своей конфигурации по умолчанию. У него Инженерный пресс и 4 Карты ускорения
        <Row><ItemImage id="engineering_processor_press" scale="2" /> <ItemImage id="speed_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 2 0" max="1 3 1" thickness=".05">
        (12) Высекатель №5: в своей конфигурации по умолчанию. У него 4 Карты ускорения
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 2 0" max="3 1 1" thickness=".05">
        (13) Шина импорта №1: в своей конфигурации по умолчанию, у неё 2 Карты ускорения
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 4 0" max="3 3 1" thickness=".05">
        (14) Шина импорта №2: в своей конфигурации по умолчанию, у неё 2 Карты ускорения
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 5 0" max="3 4 1" thickness=".05">
        (15) Шина импорта №3: в своей конфигурации по умолчанию, у неё 2 Карты ускорения
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2.7 6 0" max="3 5 1" thickness=".05">
        (16) Шина импорта №4: в своей конфигурации по умолчанию, у неё 2 Карты ускорения
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 3 0" max="1 3.3 1" thickness=".05">
        (17) Шина хранения №2: в своей конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 1.7 0" max="1 2 1" thickness=".05">
        (18) Шина хранения №3: в своей конфигурации по умолчанию.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 2 0" max="0.7 3 1" thickness=".05">
        (19) Шина импорта №5: в своей конфигурации по умолчанию, у неё 2 Карты ускорения
        <ItemImage id="speed_card" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="5 0.7 0" max="6 1 1" thickness=".05">
        (20) Шина хранения №4: в своей конфигурации по умолчанию.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.3 2.7 0.3" max="3.7 3 0.7" thickness=".05">
        Кварцевое волокно питает все 3 высекателя, потому что высекатели работают как кабели и, таким образом, передают энергию
  </BoxAnnotation>

<DiamondAnnotation pos="7 1.5 0.5" color="#00ff00">
        К главной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="185" pitch="5" />
</GameScene>

## Конфигурации

* <ItemLink id="pattern_provider" /> (1) в своей конфигурации по умолчанию, с нужными <ItemLink id="processing_pattern" />.
  Обратите внимание, что шаблоны идут прямым образом от сырых ресурсов к завершённому процессору и **НЕ** включают [пресс](../items-blocks-machines/presses.md).

  ![Шаблон логического](../assets/diagrams/logic_pattern.png)
  ![Шаблон вычислительного](../assets/diagrams/calculation_pattern.png)
  ![Шаблон инженерного](../assets/diagrams/engineering_pattern.png)

* <ItemLink id="storage_bus" /> (2, 17, 18, 20) в их конфигурациях по умолчанию.
* <ItemLink id="export_bus" /> (3-7) отфильтрованы на нужный ингредиент. У них 2 <ItemLink id="speed_card" />.
    <Row>
      <ItemImage id="silicon" scale="2" />
      <ItemImage id="minecraft:gold_ingot" scale="2" />
      <ItemImage id="certus_quartz_crystal" scale="2" />
      <ItemImage id="minecraft:diamond" scale="2" />
      <ItemImage id="minecraft:redstone" scale="2" />
    </Row>
* <ItemLink id="import_bus" /> (13-16, 19) в их конфигурациях по умолчанию. У них 2 <ItemLink id="speed_card" />.
* <ItemLink id="inscriber" /> в их конфигурациях по умолчанию. У них есть нужный [пресс](../items-blocks-machines/presses.md)
   и 4 <ItemLink id="speed_card" />.
   <Row>
     <ItemImage id="silicon_press" scale="2" />
     <ItemImage id="logic_processor_press" scale="2" />
     <ItemImage id="calculation_processor_press" scale="2" />
     <ItemImage id="engineering_processor_press" scale="2" />
   </Row>

## Как это работает

1. <ItemLink id="pattern_provider" /> помещает ингредиенты в бочку.
2. Первая [трубная подсеть](pipe-subnet.md) (оранжевая) извлекает кремний, пыль красного камня и ингредиент нужного процессора
   (Слиток золота, Кристалл истинного кварца или Алмаз) из бочки и помещает их в нужный <ItemLink id="inscriber" />.
3. Первые четыре <ItemLink id="inscriber" /> делают <ItemLink id="printed_silicon" /> и <ItemLink id="printed_logic_processor" />,
   <ItemLink id="printed_calculation_processor" /> или <ItemLink id="printed_engineering_processor" />.
4. Вторая и третья [трубные подсети](pipe-subnet.md) (зелёные) берут печатные схемы из первых четырёх <ItemLink id="inscriber" />
    и помещают их в пятый, финальной сборки <ItemLink id="inscriber" />.
5. Пятый <ItemLink id="inscriber" /> собирает [процессор](../items-blocks-machines/processors.md).
6. Четвёртая [трубная подсеть](pipe-subnet.md) (фиолетовая) помещает процессор в поставщика шаблонов, возвращая его в главную сеть.
