---
navigation:
    parent: epp_intro/epp_intro-index.md
    title: Сборочная матрица
    icon: extendedae:assembler_matrix_frame
categories:
- extended devices
item_ids:
- extendedae:assembler_matrix_frame
- extendedae:assembler_matrix_wall
- extendedae:assembler_matrix_glass
- extendedae:assembler_matrix_pattern
- extendedae:assembler_matrix_crafter
- extendedae:assembler_matrix_speed
---

# Сборочная матрица

<Row>
<BlockImage id="extendedae:assembler_matrix_frame" p:formed="true" p:powered="true" scale="5"></BlockImage>
<BlockImage id="extendedae:assembler_matrix_wall" scale="5"></BlockImage>
<BlockImage id="extendedae:assembler_matrix_glass" scale="5"></BlockImage>
</Row>
<Row>
<BlockImage id="extendedae:assembler_matrix_pattern" scale="5"></BlockImage>
<BlockImage id="extendedae:assembler_matrix_crafter" scale="5"></BlockImage>
<BlockImage id="extendedae:assembler_matrix_speed" scale="5"></BlockImage>
</Row>

Сборочная матрица — мультиблочная структура. Это комбинация <ItemLink id="ae2:molecular_assembler" /> и <ItemLink id="ae2:pattern_provider" />.
Она может выполнять много заданий изготовления одновременно (при достаточном количестве <ItemLink id="ae2:crafting_accelerator" /> в вашей МЭ-сети) и экономить вам каналы.

## Структура

<GameScene zoom="3" background="transparent" interactive={true}>
  <ImportStructure src="../structure/assembler_matrix.snbt"></ImportStructure>
</GameScene>

Это прямоугольный параллелепипед с длинами рёбер от 3 до 7. 
- Рёбра состоят из Рамки сборочной матрицы.
- Грани состоят из Стенки/Стекла сборочной матрицы.
- Внутренность состоит из Ядер сборочной матрицы — шаблонов/крафтового/ускорения.

Действительная Сборочная матрица должна содержать как минимум одно Ядро шаблонов и одно Крафтовое ядро.
Она должна быть полностью заполнена и не может быть полой.
Когда Сборочная матрица правильно сформирована и запитана, линии на Рамке сборочной матрицы станут синими.

## Ядра сборочной матрицы

Есть 3 разных Ядра сборочной матрицы.

- Ядро сборочной матрицы

Сборочная матрица берёт шаблоны только из своего Ядра шаблонов. Каждое Ядро шаблонов даёт 36 слотов шаблонов для Сборочной матрицы.

- Крафтовое ядро сборочной матрицы

Сборочная матрица назначает полученные задания изготовления своим Крафтовым ядрам. Каждое Крафтовое ядро может выполнять 8 заданий изготовления одновременно.

- Ядро ускорения сборочной матрицы

Это <ItemLink id="ae2:speed_card" /> для Сборочной матрицы. 5 ядер ускорения позволяют Сборочной матрице работать на полной скорости.
Установка более 5 ядер ускорения не даст дополнительной прибавки скорости.

## Интерфейс

Щелчок правой кнопкой по сформированной и включённой Сборочной матрице открывает её интерфейс.

![Интерфейс](../pic/assembler_matrix.png)

Вы можете добавлять или искать шаблоны в нём и смотреть, сколько заданий изготовления она выполняет.
