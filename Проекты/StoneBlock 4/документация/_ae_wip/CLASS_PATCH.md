# Патч классов Ars Elemental (красная строка брони)

Строка «Model and Texture still work in progress, currently same as medium variant.»
задана в коде через `Component.literal(...)` в классах
`alexthw/ars_elemental/common/items/armor/HeavyArmorE.class` и `LightArmorE.class`,
поэтому lang-файлом не переводится. В готовом JAR обе константы заменены на
«Модель и текстура ещё в разработке — пока такие же, как у среднего варианта.»

Воспроизведение:
1. Собрать JAR из исходника с текущим `ru_work.json` (как обычно).
2. Для каждого из двух классов: `scripts/patch_class_string.py <класс> <выход> '<старая строка>' '<новая строка>'` и заменить класс в архиве.
   Скрипт меняет только байты Utf8-константы в constant pool, индексы не сдвигаются.
3. Проверка: относительно исходника изменены только `ru_ru.json` и эти два класса;
   английской строки в JAR не осталось, `testzip` чист.
