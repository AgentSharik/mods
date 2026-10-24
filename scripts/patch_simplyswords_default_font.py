#!/usr/bin/env python3
"""Убирает повторное подключение стандартного шрифта в Simply Swords.

В Minecraft 1.21.1 FontManager сам объединяет default.json из всех паков.
Simply Swords должен добавлять только свои иконки, как в актуальном upstream,
а не повторно подключать include/space, include/default и include/unifont.

Использование: python3 scripts/patch_simplyswords_default_font.py <jar>
"""
import json
import shutil
import sys
import zipfile

FONT = "assets/minecraft/font/default.json"
EXPECTED_REFERENCES = [
    "minecraft:include/space",
    "minecraft:include/default",
    "minecraft:include/unifont",
]
EXPECTED_ICONS = {
    "icon_left_mouse.png",
    "icon_right_mouse.png",
    "icon_alt_key_extra.png",
    "icon_fire_magic.png",
    "icon_frost_magic.png",
    "icon_lightning_magic.png",
    "icon_soul_magic.png",
    "icon_arcane_magic.png",
    "icon_healing_magic.png",
    "icon_ctrl_key_extra.png",
    "icon_ctrl_alt_key_extra.png",
}


def main(path: str) -> None:
    with zipfile.ZipFile(path) as z:
        font = json.loads(z.read(FONT))
        providers = font["providers"]
        references = [p.get("id") for p in providers[:3]]
        if references != EXPECTED_REFERENCES:
            raise SystemExit(f"Неожиданные первые providers: {references!r}")

        icons = providers[3:]
        if len(icons) != 11 or any(p.get("type") != "bitmap" for p in icons):
            raise SystemExit("Ожидались ровно 11 bitmap-иконок Simply Swords")
        icon_files = {p["file"].removeprefix("simplyswords:icon/") for p in icons}
        if icon_files != EXPECTED_ICONS:
            raise SystemExit(f"Неожиданный набор иконок: {sorted(icon_files)!r}")
        for provider in icons:
            texture = "assets/" + provider["file"].replace(":", "/textures/", 1)
            if texture not in z.namelist():
                raise SystemExit(f"Отсутствует текстура иконки: {texture}")

        patched = (json.dumps({"providers": icons}, ensure_ascii=True, indent=2) + "\n").encode("utf-8")
        tmp = path + ".tmp"
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
            for info in z.infolist():
                out.writestr(info, patched if info.filename == FONT else z.read(info.filename))

    shutil.move(tmp, path)
    print("Удалены 3 повторные ссылки на шрифт Minecraft; 11 иконок сохранены:", path)


if __name__ == "__main__":
    main(sys.argv[1])
