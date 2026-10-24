# Apotheosis — пустые русские подсказки и Unicode-шрифт

Дата: 2026-10-24. Статус: решение v1.1.27 отклонено пользователем из-за тонкого Unicode-шрифта; в v1.1.28 заменено возвратом обычного шрифта и исправлением `default.json` Simply Swords.

## Симптом

При нажатии на уровни мира («вершины») Apotheosis открывается тёмная панель без видимого текста. Это отдельный дефект: расширение кнопок в v1.1.26 его не устраняет.

## Что проверено

- EN/RU-значения `button.apotheosis.{haven,frontier,ascent,summit,pinnacle}` и `text.apotheosis.world_tier.*` присутствуют и не пустые. Повторный перевод этих ключей не нужен.
- В upstream Apotheosis есть точное совпадение: issue [#1756](https://github.com/Shadows-of-Fire/Apotheosis/issues/1756) описывает исчезающий русский текст World Tier UI; включение **Force Unicode Font** полностью устраняет проблему у автора репорта.
- Issue [#1808](https://github.com/Shadows-of-Fire/Apotheosis/issues/1808) повторяет жалобу на пустые подсказки при русском языке на Apotheosis 8.9.0. Разработчик снова указывает на Force Unicode Font.
- В исходниках Minecraft 1.21.1 `Options.forceUnicodeFont()` возвращает `OptionInstance<Boolean>`. Его `set(true)` вызывает штатное обновление шрифта и размера интерфейса; отдельная перезагрузка ресурсов не требуется.

## Реализация

Добавлен `release/kubejs/client_scripts/ru_unicode_font.js`:

1. на событии `ClientEvents.loggedIn` получает текущие настройки через `Minecraft.getInstance().options`;
2. работает только при `languageCode == "ru_ru"`;
3. если Unicode-шрифт ещё выключен, вызывает `options.forceUnicodeFont().set(true)`;
4. сохраняет текущие настройки через `options.save()`.

Корневой `options.txt` в архив не добавляется. Скрипт изменяет один параметр, не подменяет остальные настройки и не затрагивает конфигурацию шрифтов FancyMenu. Перевод квестов и JAR Apotheosis этим исправлением не изменяются.

Workflow проверяет синтаксис скрипта через `node --check`, его наличие в `pkg/kubejs` и в итоговом ZIP. Для окончательного подтверждения нужен запуск игры: открыть экран уровней мира с русским языком и убедиться, что текст панели виден.
