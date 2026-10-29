# DimDungeons: восстановление русской книги Patchouli

Дата: 2026-10-29. Изменён `dimdungeons-210-neoforge-1.21.1.jar`.

## Симптом и причина

В `latest.log` при входе на сервер:

```text
Error loading and compiling book dimdungeons:guide_book, using empty contents
Error building entry dimdungeons:artifact_secret_bell_1
CommandSyntaxException ... at position 28: ...item_secret_bell<--[HERE]
```

Это не краш клиента, но Patchouli отбрасывает содержимое всей книги. В русских страницах `artifact_secret_bell_1.json` и `artifact_secret_bell_2.json` остался формат предмета со старым NBT:

```text
dimdungeons:item_secret_bell{upgrade:1}
dimdungeons:item_secret_bell{upgrade:2}
```

Minecraft/Patchouli 1.21.1 ожидает data components, а не NBT сразу после идентификатора предмета. Английская страница первого колокола использует простой предмет и этой ошибки не вызывает; русские и китайские страницы содержат унаследованный NBT. Изменяется только `ru_ru`.

## Подтверждение формата по коду мода

Статический разбор class file:

- `DimDungeons.<clinit>` регистрирует `SECRET_BELL_DATA` как `dimdungeons:secret_bell_data`;
- `SecretBellDataComponent.lambda$static$0` задаёт четыре обязательных `Codec.INT.fieldOf`: `upgrade`, `secret_x`, `secret_y`, `secret_z`;
- `ItemSecretBell` читает и изменяет этот компонент.

Поэтому страницы получили полные значения с нулевыми координатами секрета:

```text
dimdungeons:item_secret_bell[dimdungeons:secret_bell_data={upgrade:1,secret_x:0,secret_y:0,secret_z:0}]
dimdungeons:item_secret_bell[dimdungeons:secret_bell_data={upgrade:2,secret_x:0,secret_y:0,secret_z:0}]
```

## Проверки

- Патчер сначала применён к временной копии, затем к рабочему JAR.
- Изменены ровно две ASCII-строки `pages[0].item`; русский текст сохранён побайтово.
- Имена и содержимое всех остальных ZIP-записей сравнены с исходным JAR: посторонних изменений нет.
- `ZipFile.testzip()` до и после — без ошибок.
- Все русские Patchouli JSON разобраны; других значений `item`/`icon` в форме `item{...}` нет.
- Повторный запуск с `--check` подтверждает обе новые строки.

Игровая проверка не выполнялась. После установки следует заново открыть книгу DimDungeons и убедиться, что страницы появились, а ошибка `using empty contents` исчезла из нового `latest.log`.
