# Журнал перевода: FTB Pack Companion 21.1.22 (ftbpc)

Дата: 2026-10-14. Статус: переведено и собрано, проверка в игре не выполнялась.

## Итог
- Готовый JAR: `Проекты/StoneBlock 4/ftb-pack-companion-21.1.22.jar`,
  SHA-256 `c26b72c1ab731e0f499e481dd9665d8d22433b1a65c6c6e74886bb36cc1acb4b`.
- Lang: 25/25 ключей `ru_ru.json` (missing=0, плейсхолдеры совпадают);
  книг нет; архив 243 записи, testzip — без ошибок.
- Описание в `neoforge.mods.toml`: «Лучший друг разработчика сборок».

## Ключевые решения
- Это владелец `ftbpc:action_pad` (экран со скриншотов): Spawn → «Спавн»,
  Home → «Дом», Request Teleport → «Запрос телепортации»,
  Action Pad → «Панель действий».
- `ftbpackcompanion.actionpad.tpa.request_sent`: код
  (ActionTPAScreen$1.onClicked) добавляет имя игрока отдельным компонентом
  после строки, поэтому переведено «Запрос телепортации отправлен игроку».
- `ftbpackcompanion.key.category` оставлено названием мода «Pack Companion».
- Экран шейдеров и условия FancyMenu (час больше/меньше) переведены полностью;
  `\n` в описаниях условий сохранены.
