#!/usr/bin/env python3
"""Исправляет два устаревших NBT-item в русской книге DimDungeons.

В Minecraft 1.21.1 `dimdungeons:item_secret_bell{upgrade:1}` больше не
разбирается Patchouli: после идентификатора предмета нужен формат компонентов.
Из-за исключения Patchouli заменяет содержимое всей книги на пустое.

Компонент и обязательные поля подтверждены по классам мода:
  DimDungeons.SECRET_BELL_DATA -> dimdungeons:secret_bell_data
  SecretBellDataComponent -> upgrade, secret_x, secret_y, secret_z (INT)

Использование:
  python3 scripts/patch_dimdungeons_patchouli_components.py <jar>
  python3 scripts/patch_dimdungeons_patchouli_components.py <jar> --check
"""
from __future__ import annotations

import json
import re
import shutil
import sys
import zipfile
from pathlib import Path

BASE = "assets/dimdungeons/patchouli_books/guide_book/ru_ru/entries"
LEGACY_ITEM_NBT = re.compile(r"^[^\[{]+\{")
REPLACEMENTS = {
    f"{BASE}/artifact_secret_bell_1.json": (
        b"dimdungeons:item_secret_bell{upgrade:1}",
        b"dimdungeons:item_secret_bell[dimdungeons:secret_bell_data="
        b"{upgrade:1,secret_x:0,secret_y:0,secret_z:0}]",
    ),
    f"{BASE}/artifact_secret_bell_2.json": (
        b"dimdungeons:item_secret_bell{upgrade:2}",
        b"dimdungeons:item_secret_bell[dimdungeons:secret_bell_data="
        b"{upgrade:2,secret_x:0,secret_y:0,secret_z:0}]",
    ),
}


def patch_entries(entries: list[tuple[zipfile.ZipInfo, bytes]]) -> tuple[list[tuple[zipfile.ZipInfo, bytes]], int]:
    result = []
    changed = 0
    for info, data in entries:
        if info.filename in REPLACEMENTS:
            old, new = REPLACEMENTS[info.filename]
            if old in data:
                assert data.count(old) == 1, f"Дублирование старого item в {info.filename}"
                data = data.replace(old, new)
                changed += 1
            else:
                assert data.count(new) == 1, f"Не найден ожидаемый item в {info.filename}"
            parsed = json.loads(data)
            item = parsed["pages"][0]["item"].encode("utf-8")
            assert item == new, f"Изменён не тот item в {info.filename}"
        result.append((info, data))
    return result, changed


def audit(entries: list[tuple[zipfile.ZipInfo, bytes]]) -> None:
    names = [info.filename for info, _ in entries]
    assert len(names) == len(set(names)), "Дублирующиеся имена ZIP-записей"
    for info, data in entries:
        if info.filename in REPLACEMENTS:
            old, new = REPLACEMENTS[info.filename]
            assert old not in data, f"Остался legacy NBT в {info.filename}"
            assert data.count(new) == 1, f"Нет нового component item в {info.filename}"
        if "/patchouli_books/" in info.filename and "/ru_ru/" in info.filename and info.filename.endswith(".json"):
            book_json = json.loads(data)

            def walk(value) -> None:
                if isinstance(value, dict):
                    for key, child in value.items():
                        if key in {"item", "icon"} and isinstance(child, str):
                            assert not LEGACY_ITEM_NBT.match(child), (
                                f"Legacy NBT в {info.filename}: {key}={child}"
                            )
                        walk(child)
                elif isinstance(value, list):
                    for child in value:
                        walk(child)

            walk(book_json)


def main(path: Path, check: bool) -> None:
    with zipfile.ZipFile(path) as source:
        assert source.testzip() is None, "Повреждён исходный JAR"
        original = [(info, source.read(info.filename)) for info in source.infolist()]

    patched, changed = patch_entries(original)
    audit(patched)
    if check:
        assert changed == 0, "JAR ещё содержит старые item-строки"
        print(f"OK: обе страницы используют data components; других legacy NBT в ru_ru Patchouli нет: {path}")
        return

    if changed:
        temporary = path.with_suffix(path.suffix + ".tmp")
        with zipfile.ZipFile(temporary, "w", zipfile.ZIP_DEFLATED) as output:
            for info, data in patched:
                output.writestr(info, data)
        with zipfile.ZipFile(temporary) as result:
            assert result.testzip() is None, "Повреждён результирующий JAR"
            actual = [(info, result.read(info.filename)) for info in result.infolist()]
        audit(actual)
        assert [info.filename for info, _ in actual] == [info.filename for info, _ in original]
        before = {info.filename: data for info, data in original}
        for info, data in actual:
            if info.filename not in REPLACEMENTS:
                assert data == before[info.filename], f"Постороннее изменение: {info.filename}"
        shutil.move(temporary, path)
    print(f"Готово: изменено записей {changed}; остальное содержимое JAR сохранено: {path}")


if __name__ == "__main__":
    if len(sys.argv) not in {2, 3} or (len(sys.argv) == 3 and sys.argv[2] != "--check"):
        raise SystemExit(__doc__)
    main(Path(sys.argv[1]), len(sys.argv) == 3)
