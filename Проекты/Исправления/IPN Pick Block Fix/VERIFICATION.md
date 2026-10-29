# Проверка IPN Pick Block Fix 1.0.0

Дата: 2026-10-29.

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
- Пользовательский валидатор подтвердил Java 21 class file, Mixin/Inject-аннотации и точную цель.
- Независимый парсер `jawa` прочитал оба класса и дизассемблировал методы: конструкторы корректны; обработчик — `aload_2`, `invokevirtual CallbackInfo.cancel`, `return`.

## Не выполнялось

- Запуск Minecraft/NeoForge.
- Подключение к серверу и игровой pick-block.
- Проверка совместимости с другими версиями IPN — поэтому зависимость ограничена `[2.2.5]`.

## SHA-256

`8fc313927cf4955823803ab14e15761a5ced0953ce9ae09c085581f97441f4db` — `IPN-PickBlock-Fix-neoforge-1.21.1-1.0.0.jar`.
