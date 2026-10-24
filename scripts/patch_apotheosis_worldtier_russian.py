#!/usr/bin/env python3
"""Переводит экран выбора уровня мира Apotheosis на русский.

Работает поверх v1.1.30, где 30 ключей экрана намеренно оставлены
английскими. Меняет только assets/apotheosis/lang/ru_ru.json; классы,
шрифты Minecraft и остальной перевод Apotheosis не затрагиваются.

Использование: python3 scripts/patch_apotheosis_worldtier_russian.py <jar>
"""
import json
import shutil
import sys
import zipfile

RU = "assets/apotheosis/lang/ru_ru.json"
EN = "assets/apotheosis/lang/en_us.json"

TRANSLATIONS = {
    "title.apotheosis.select_world_tier": "Выбор уровня мира",
    "text.apotheosis.world_tier.haven": "Прибежище",
    "text.apotheosis.world_tier.haven.desc": "Начало вашего пути",
    "text.apotheosis.world_tier.frontier": "Рубеж",
    "text.apotheosis.world_tier.frontier.desc": "Отправляйтесь в опасный мир",
    "text.apotheosis.world_tier.ascent": "Подъём",
    "text.apotheosis.world_tier.ascent.desc": "Начните восхождение к могуществу",
    "text.apotheosis.world_tier.summit": "Вершина",
    "text.apotheosis.world_tier.summit.desc": "Вы достигли вершины, но это ещё не конец",
    "text.apotheosis.world_tier.pinnacle": "Пик",
    "text.apotheosis.world_tier.pinnacle.desc": "Затеряйтесь среди грозовых горных вершин",
    "text.apotheosis.world_tier.difficulty": "Сложность:",
    "button.apotheosis.haven": "Прибежище",
    "button.apotheosis.frontier": "Рубеж",
    "button.apotheosis.ascent": "Подъём",
    "button.apotheosis.summit": "Вершина",
    "button.apotheosis.pinnacle": "Пик",
    "button.apotheosis.tier_locked": "%s (заблокировано)",
    "button.apotheosis.tier_advancement": "Выполните достижение %s, чтобы разблокировать.",
    "button.apotheosis.activate_tier": "Активировать уровень мира: %s",
    "button.apotheosis.already_activated": "Этот уровень мира сейчас активен",
    "button.apotheosis.activate": "Активировать",
    "button.apotheosis.activated": "Активировано",
    "button.apotheosis.disabled": "Отключено",
    "button.apotheosis.tier_changes_disabled": "Ручное изменение уровней мира отключено в настройках.",
    "button.apotheosis.show_detailed_info": "Подробности",
    "button.apotheosis.show_detailed_info.desc": "Просмотр усилений существ, усилений игроков и редкостей",
    "button.apotheosis.inactive": "Неактивно",
    "button.apotheosis.locked": "Этот уровень мира заблокирован",
    "button.apotheosis.open_world_tier_tutorial": "Открыть обучение по уровням мира",
}


def main(path: str) -> None:
    if len(TRANSLATIONS) != 30:
        raise SystemExit(f"В переводе должно быть 30 ключей, получено {len(TRANSLATIONS)}")

    with zipfile.ZipFile(path) as z:
        en = json.loads(z.read(EN))
        ru = json.loads(z.read(RU))
        missing = sorted((TRANSLATIONS.keys() - en.keys()) | (TRANSLATIONS.keys() - ru.keys()))
        if missing:
            raise SystemExit(f"Отсутствуют ключи: {missing}")

        not_english = [key for key in TRANSLATIONS if ru[key] != en[key]]
        if not_english:
            raise SystemExit(f"JAR не соответствует английской основе v1.1.30: {not_english}")

        before = dict(ru)
        ru.update(TRANSLATIONS)
        assert sum(before[key] != ru[key] for key in ru) == len(TRANSLATIONS)

        patched = (json.dumps(ru, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
        tmp = path + ".tmp"
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
            for info in z.infolist():
                out.writestr(info, patched if info.filename == RU else z.read(info.filename))

    shutil.move(tmp, path)
    print("Русский перевод экрана выбора уровня мира: 30 ключей; остальной ru_ru не изменён")


if __name__ == "__main__":
    main(sys.argv[1])
