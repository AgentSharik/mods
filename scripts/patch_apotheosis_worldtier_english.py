#!/usr/bin/env python3
"""Оставляет английскими только надписи экрана выбора уровня мира Apotheosis.

Никакие шрифты, классы рендеринга и настройки Minecraft не изменяются.
Значения 30 ключей ru_ru берутся из en_us самого JAR; остальной русский
перевод Apotheosis и все прочие ZIP-записи сохраняются.

Использование: python3 scripts/patch_apotheosis_worldtier_english.py <jar>
"""
import json
import shutil
import sys
import zipfile

RU = "assets/apotheosis/lang/ru_ru.json"
EN = "assets/apotheosis/lang/en_us.json"

TIERS = ("haven", "frontier", "ascent", "summit", "pinnacle")
KEYS = {
    "title.apotheosis.select_world_tier",
    "text.apotheosis.world_tier.difficulty",
    *(f"text.apotheosis.world_tier.{tier}" for tier in TIERS),
    *(f"text.apotheosis.world_tier.{tier}.desc" for tier in TIERS),
    *(f"button.apotheosis.{tier}" for tier in TIERS),
    "button.apotheosis.tier_locked",
    "button.apotheosis.tier_advancement",
    "button.apotheosis.activate_tier",
    "button.apotheosis.already_activated",
    "button.apotheosis.activate",
    "button.apotheosis.activated",
    "button.apotheosis.disabled",
    "button.apotheosis.tier_changes_disabled",
    "button.apotheosis.show_detailed_info",
    "button.apotheosis.show_detailed_info.desc",
    "button.apotheosis.inactive",
    "button.apotheosis.locked",
    "button.apotheosis.open_world_tier_tutorial",
}


def main(path: str) -> None:
    if len(KEYS) != 30:
        raise SystemExit(f"В списке должно быть 30 ключей, получено {len(KEYS)}")

    with zipfile.ZipFile(path) as z:
        en = json.loads(z.read(EN))
        ru = json.loads(z.read(RU))
        missing = sorted((KEYS - en.keys()) | (KEYS - ru.keys()))
        if missing:
            raise SystemExit(f"Отсутствуют ключи: {missing}")

        changed = [key for key in sorted(KEYS) if ru[key] != en[key]]
        if changed and len(changed) != len(KEYS):
            raise SystemExit(f"Частично изменённый JAR не поддерживается: {changed}")
        if not changed:
            raise SystemExit("JAR уже содержит английские надписи экрана")

        before = dict(ru)
        for key in KEYS:
            ru[key] = en[key]
        assert sum(before[key] != ru[key] for key in ru) == len(KEYS)

        patched = (json.dumps(ru, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
        tmp = path + ".tmp"
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
            for info in z.infolist():
                out.writestr(info, patched if info.filename == RU else z.read(info.filename))

    shutil.move(tmp, path)
    print(f"Английские надписи экрана выбора уровня мира: {len(KEYS)} ключей; остальной ru_ru не изменён")


if __name__ == "__main__":
    main(sys.argv[1])
