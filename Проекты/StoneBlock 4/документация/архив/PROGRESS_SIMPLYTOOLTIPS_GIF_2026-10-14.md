# SimplyTooltips — сообщения записи GIF (14.10.2026)

Повод: скриншот из игры — `[Simply Tooltips] No tooltip is currently visible to capture.`

## Аудит

- lang: `en_us`/`ru_ru` по 31 ключу, пропусков нет; все значения вычитаны ранее.
- Непереведённая строка lang-ключом не является: сообщения записи подсказки
  в GIF зашиты через `Component.literal(...)` в
  `net/sweenus/simplytooltips/client/render/TooltipGifRecorder.class`
  (8 сообщений чата) плюс текст ошибки `No frames available for GIF output`
  в нём же и в `AnimatedGifWriter.class`.

## Что сделано

- Новый скрипт `scripts/patch_simplytooltips_gif.py`: заменены все 9 строк
  на русские, префикс `[Simply Tooltips]` (имя мода) сохранён.
- JAR пересобран из готового заменой только этих двух классов. Сверка по
  CRC/размеру: от исходника отличаются 3 записи (`ru_ru.json` и два класса),
  от предыдущей готовой версии — 2 записи. `testzip` — ок, JSON валиден.

Проверка запуском в игре — за пользователем: сообщение при нажатии записи
GIF без видимой подсказки должно быть русским.
