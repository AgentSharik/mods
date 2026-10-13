---
navigation:
  parent: enderdrives_intro/enderdrives_intro-index.md
  title: Эндер-ячейка хранения предметов
  icon: enderdrives:ender_disk_1k
categories:
  - enderdrives
item_ids:
  - enderdrives:ender_disk_1k
  - enderdrives:ender_disk_4k
  - enderdrives:ender_disk_16k
  - enderdrives:ender_disk_64k
  - enderdrives:ender_disk_256k
  - enderdrives:ender_disk_creative
  - enderdrives:ender_fluid_disk_1k
  - enderdrives:ender_fluid_disk_4k
  - enderdrives:ender_fluid_disk_16k
  - enderdrives:ender_fluid_disk_64k
  - enderdrives:ender_fluid_disk_256k
  - enderdrives:ender_fluid_disk_creative
---

# Эндер-накопители

Эндер-накопители — мощные накопители, позволяющие глобально синхронизированное хранение через системы МЭ, измерения и даже разных игроков через частоты.  

<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_disk_1k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_disk_1k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_disk_4k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_disk_4k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_disk_16k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_disk_16k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_disk_64k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_disk_64k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_disk_256k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_disk_256k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_fluid_disk_1k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_fluid_disk_1k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_fluid_disk_4k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_fluid_disk_4k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_fluid_disk_16k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_fluid_disk_16k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_fluid_disk_64k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_fluid_disk_64k" />
  </Column>
</Row>


<Row gap="10">
  <Column>
    <ItemImage id="enderdrives:ender_fluid_disk_256k" />
  </Column>
  <Column>
    <ItemLink id="enderdrives:ender_fluid_disk_256k" />
  </Column>
</Row>


---

## Как это работает
Каждый EnderDrive настроен частотой, областью действия и режимом.
- **Частота**: накопители с одинаковой частотой делят один инвентарь.
- **Область действия**: определяет, кто может получать доступ к накопителю (Глобальная, Личная или Командная).
- **Режим**: управляет потоком предметов (Двусторонний, Только вход, Только выход). 

Все накопители с одинаковой частотой и областью действия будут получать доступ к **одному виртуальному инвентарю**, где бы они ни находились.

---

## Лимиты типов
В отличие от традиционных накопителей AE2, ограничение EnderDrives основано только на типах.  Из-за природы хранения предметов на серверной стороне единственное жёсткое ограничение — количество типов.  Вы можете буквально хранить до 2^63 - 1 или 9 223 372 036 854 775 807 предметов каждого типа.  Но будьте осторожны: стоимость энергии будет расти с количеством предметов, хранящихся на накопителе на этой частоте!

Каждый сервер имеет разное количество типов, при котором начнётся нагрузка.  Вы можете проверить свой сервер командой автобенчмарка.  Для точных результатов понадобится открытый терминал с накопителем на Личной области с выбранной частотой.  Бенчмарк будет идти, пока TPS не упадёт ниже 18.  Это может занять несколько минут.

Мой личный средний результат — около 275 000 типов.  275 000/255 ≈ 1078.  Это значит, что мне пришлось бы заполнить 107,8 МЭ накопителей эндер-накопителями 256К с типизированными предметами, прежде чем начнутся проблемы с производительностью.  Я видел и более высокие, и более низкие рекомендуемые максимальные количества типов.  Этот лимит общий для всех сторон, использующих накопители в одном мире.

---

## Режимы накопителей
Каждый накопитель можно установить в один из трёх **режимов передачи**:

- ![ИШ1](../pic/transport_bidirectional_alt.png) **Двусторонний** _(По умолчанию)_  
  Стандартное поведение МЭ-накопителя. Свободно вставляйте и извлекайте предметы.


- ![ИШ1](../pic/transport_input_alt.png) **Только вход**  
  Предметы можно вставлять, но не извлекать. Полезно для буферов или синхронизации входов.


- ![ИШ1](../pic/transport_output_alt.png) **Только выход**  
  Предметы можно извлекать, но не вставлять. Отлично для выходных буферов или доступа только для чтения.

---

## Область действия и приватность

У каждого накопителя также есть **область действия**, управляющая тем, кто может получать доступ к инвентарю:
-  **Глобальная** _(По умолчанию)_  
   Публично! Любой игрок, использующий ту же частоту, может получить доступ к этому общему инвентарю.


-  **Личная**  
  Привязана к вашему UUID. Только вы можете создавать накопители для доступа к этой частоте.  Любой другой пользователь вашей МЭ-системы всё равно может получать доступ к этому хранилищу


-  **Командная**  
  Общая с вашей командой FTB. Все участники могут создавать накопители с доступом к той же частоте.
