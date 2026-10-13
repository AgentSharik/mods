---
navigation:
  parent: example-setups/example-setups-index.md
  title: «Трубная» подсеть предметов/жидкостей
  icon: storage_bus
---

# «Трубная» подсеть предметов/жидкостей

Простой метод эмуляции трубы предметов и/или жидкостей с помощью [устройств](../ae2-mechanics/devices.md) AE2, полезный для, ну, всего, для чего вы использовали бы трубу предметов или жидкостей.
Это включает возвращение результата изготовления в <ItemLink id="pattern_provider" />.

Как правило, есть два разных метода достичь этого:

## Шина импорта -> Шина хранения

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) Шина импорта: может быть отфильтрована.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) Шина хранения: может быть отфильтрована. Она (и другие шины хранения, которые вы хотите сделать назначением)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="import_bus" /> (1) на инвентаре источника импортирует предметы или жидкость и пытается хранить их в [сетевом хранилище](../ae2-mechanics/import-export-storage.md).
Поскольку единственное хранилище в сети — <ItemLink id="storage_bus" /> (2) (вот почему это подсеть, а не в вашей главной сети), предметы или жидкость
помещаются в инвентарь назначения, таким образом передаваясь. Энергия обеспечивается через <ItemLink id="quartz_fiber" />.
И шина импорта, и шина хранения могут быть отфильтрованы, но схема будет передавать всё, к чему у неё есть доступ, если фильтры не применены.
Эта схема также работает с несколькими шинами импорта и несколькими шинами хранения.

## Шина хранения -> Шина экспорта

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        (1) Шина хранения: может быть отфильтрована. Она (и другие шины хранения, которые вы хотите сделать источником)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        (2) Шина экспорта: должна быть отфильтрована.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<ItemLink id="export_bus" /> на инвентаре назначения пытается извлечь предметы из своего фильтра из [сетевого хранилища](../ae2-mechanics/import-export-storage.md).
Поскольку единственное хранилище в сети — <ItemLink id="storage_bus" /> (вот почему это подсеть, а не в вашей главной сети), предметы или жидкость
извлекаются из инвентаря источника, таким образом передаваясь. Энергия обеспечивается через <ItemLink id="quartz_fiber" />.
Поскольку шины экспорта должны быть отфильтрованы для работы, эта схема работает только если вы отфильтруете шину экспорта.
Эта схема также работает с несколькими шинами хранения и несколькими шинами экспорта.

## Схема, которая не работает (Шина импорта -> Шина экспорта)

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_export_pipe.snbt" />

<BoxAnnotation color="#dd3333" min="3.7 0 0" max="4 1 1">
        Шина импорта: поскольку в сети нет хранилища, ей некуда импортировать.
  </BoxAnnotation>

<BoxAnnotation color="#dd3333" min="1 0 0" max="1.3 1 1">
        (2) Шина экспорта: поскольку в сети нет хранилища, ей нечего экспортировать.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 0.5" color="#ff0000">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 0.5" color="#ff0000">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Схема с просто шинами импорта и экспорта не будет работать. Шина импорта попытается извлекать из инвентаря источника
и хранить предметы или жидкость в сетевом хранилище. Шина экспорта попытается извлекать из сетевого хранилища и помещать
предметы или жидкость в инвентарь назначения. Однако поскольку в этой сети **нет хранилища**, шина импорта не может импортировать,
а шина экспорта не может экспортировать, поэтому ничего не происходит.

## Ввод и вывод через 1 грань

Скажем, у вас есть какая-то машина, которая может получать ввод и извлекать свой вывод через 1 грань. (Например, <ItemLink id="charger" />)
Вы можете и помещать в неё ингредиенты, и извлекать результат, комбинируя 2 метода трубной подсети:

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/import_storage_export_pipe.snbt" />

<BoxAnnotation color="#dddddd" min="4 1 1" max="5 1.3 2">
        (1) Шина импорта: может быть отфильтрована.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 1 1" max="3 1.3 2">
        (2) Шина хранения: может быть отфильтрована. Она (и другие шины хранения, через которые вы хотите помещать и извлекать предметы)
        должна быть единственным хранилищем в сети.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="2 0 1" max="3 1 2">
        (3) Вещь, в которую вы хотите помещать и из которой извлекать: в данном случае Зарядник.
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 1" max="1 1.3 2">
        (4) Шина экспорта: должна быть отфильтрована.
  </BoxAnnotation>

<DiamondAnnotation pos="4.5 0.5 1.5" color="#00ff00">
        Источник
    </DiamondAnnotation>

<DiamondAnnotation pos="0.5 0.5 1.5" color="#00ff00">
        Назначение
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Интерфейсы

Оказывается, есть [устройства](../ae2-mechanics/devices.md), помимо шин импорта и шин экспорта, которые помещают предметы в
[сетевое хранилище](../ae2-mechanics/import-export-storage.md) и извлекают предметы из него!
Здесь актуален <ItemLink id="interface" />. Если вставлен предмет, который интерфейс не установлен хранить, интерфейс
поместит его в сетевое хранилище, что мы можем использовать подобно трубе шина импорта -> шина хранения. Установка интерфейса
хранить какой-то предмет будет извлекать его из сетевого хранилища, подобно трубе шина хранения -> шина экспорта. Интерфейсы можно установить
хранить некоторые вещи и не хранить другие, позволяя вам удалённо помещать и извлекать через шины хранения, если вы по какой-то причине хотите это делать.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/interface_pipes.snbt" />

<BoxAnnotation color="#dddddd" min="3.7 0 0" max="4 1 1">
        Интерфейс
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 1">
        Шина хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="3.7 0 2" max="4 1 3">
        Шина хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 2" max="1 1.3 3">
        Интерфейс
  </BoxAnnotation>

<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Один-ко-многим и многие-к-одному (и многие-ко-многим)

Конечно, вам не нужно использовать только одну <ItemLink id="import_bus" />, или <ItemLink id="export_bus" />, или <ItemLink id="storage_bus" />

<GameScene zoom="3" background="transparent">
<ImportStructure src="../assets/assemblies/many_to_many_pipe.snbt" />

<IsometricCamera yaw="185" pitch="30" />
</GameScene>

## Помещение в несколько мест

Из всего этого мы можем вывести метод отправки ингредиентов с одной грани <ItemLink id="pattern_provider" /> во много различных
мест, например в массив машин или несколько разных граней одной машины.

Нам не нужна труба импорт -> хранилище или хранилище -> экспорт, потому что <ItemLink id="pattern_provider" /> никогда
на самом деле не содержит ингредиенты. Вместо этого поставщики *выталкивают* ингредиенты в смежные инвентари, поэтому нам нужен некий 
смежный инвентарь, который также может импортировать предметы.

Это звучит как... <ItemLink id="interface" />!
Убедитесь, что поставщик находится в режиме направленной или плоской подчасти и/или интерфейс находится в режиме плоской подчасти, чтобы они не образовывали сетевое
подключение.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/provider_interface_storage.snbt" />

<BoxAnnotation color="#dddddd" min="2.7 0 1" max="3 1 2">
        Интерфейс (должен быть плоским, а не полноблочным)
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="1 0 0" max="1.3 1 4">
        Шины хранения
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 0 0" max="1 1 4">
        Места, куда вы хотите помещать шаблонами (несколько машин или несколько граней 1 машины)
  </BoxAnnotation>

<IsometricCamera yaw="185" pitch="30" />
</GameScene>
