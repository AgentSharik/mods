# Проверка IPN Pick Block Fix 1.0.1

Дата: 2026-10-29.

## Исправление запуска после 1.0.0

Игровой `stderr_stream.log` показал `MixinApplyError` во время `PREPARE`: `The mixin 'com.agentsharik.ipnpickblockfix.LockedSlotKeeperMixin' is missing an @Mixin annotation`.

Причина найдена: `@Mixin` имеет `RetentionPolicy.CLASS`, но генератор 1.0.0 ошибочно записал её в `RuntimeVisibleAnnotations`. В 1.0.1 аннотация класса записана в `RuntimeInvisibleAnnotations`. `@Inject` имеет `RetentionPolicy.RUNTIME` и по-прежнему находится в `RuntimeVisibleAnnotations` на методе. Дополнительная сверка с compiled Mixin в `AE2-Things-1.4.2-beta.jar` подтвердила использование `RuntimeInvisibleAnnotations` для `@Mixin`.

## Основание

- Crash-report: `LockedSlotKeeper.ignoredSelectedHotbarSlot` → `NonNullList.get` → `Index 38 out of bounds for length 38`.
- Путь вызова: `MultiPlayerGameMode.handlePickItem` → `Minecraft.pickBlock`.
- IPN: `2.2.5`; libIPN: `6.6.3`; Minecraft: `1.21.1`; NeoForge: `21.1.250`.
- Исходник тега IPN `v2.2.5` подтверждает доступ к `vanillaContainer.slots[slotId]` без проверки размера при `slotId = selectedSlot + 36`.

## Выполненные проверки

- `python -m py_compile scripts/build_ipn_pick_block_fix.py` — успешно.
- `python scripts/build_ipn_pick_block_fix.py` — успешно.
- `python scripts/build_ipn_pick_block_fix.py --check` — существующий JAR побайтово совпал с повторной сборкой.
- `zipfile.ZipFile.testzip()` — ошибок CRC нет.
- Состав JAR — 6 файлов: manifest, mods.toml, Mixin JSON, 2 class file, LICENSE.
- Пользовательский валидатор подтвердил Java 21 class file и точную цель; `@Mixin` находится только в `RuntimeInvisibleAnnotations`, `@Inject` — только в `RuntimeVisibleAnnotations`.
- Независимый парсер `jawa` прочитал оба класса и подтвердил те атрибуты; конструкторы корректны, обработчик — `aload_2`, `invokevirtual CallbackInfo.cancel`, `return`.

## Не выполнялось

- Запуск Minecraft/NeoForge.
- Подключение к серверу и игровой pick-block.
- Проверка совместимости с другими версиями IPN — поэтому зависимость ограничена `[2.2.5]`.

## SHA-256

`e9c574d0915c7f22dfcd3d165df567c800f74ec0bb582a707330b2e9b73f6d81` — `IPN-PickBlock-Fix-neoforge-1.21.1-1.0.1.jar`.
