#!/usr/bin/env python3
"""Патч ширин кнопок экрана выбора уровня мира Apotheosis.

В классе WorldTierSelectScreen.init() ширины заданы байт-кодом (SimpleTexButton):
- кнопка активации: size(60, 24)  -> size(100, 24)
- кнопка подробностей: size(80, 20) -> size(127, 20)
Английские подписи («Activated», «Detailed Info») в эти рамки влезают,
русские («Активировано», «Подробная информация») обрезаются. Операции
bipush имеют одинаковую длину, поэтому байт-код меняется без сдвига
смещений переходов. Каждая пара встречается в классе ровно один раз
(проверяется перед патчем).

Использование: python3 scripts/patch_apotheosis_worldtier_buttons.py <jar>
"""
import shutil
import sys
import zipfile

CLASS = "dev/shadowsoffire/apotheosis/client/WorldTierSelectScreen.class"
PATCHES = [
    (bytes([0x10, 60, 0x10, 24]), bytes([0x10, 100, 0x10, 24]), "activate 60->100"),
    (bytes([0x10, 80, 0x10, 20]), bytes([0x10, 127, 0x10, 20]), "details 80->127"),
]


def main(path: str) -> None:
    with zipfile.ZipFile(path) as z:
        data = bytearray(z.read(CLASS))
        for old, new, label in PATCHES:
            hits = data.count(old)
            if hits != 1:
                raise SystemExit(f"{label}: ожидалась 1 пара байтов, найдено {hits}")
            data[data.find(old):data.find(old) + 4] = new
            print(f"patched {label}")
        tmp = path + ".tmp"
        with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
            for info in z.infolist():
                blob = bytes(data) if info.filename == CLASS else z.read(info.filename)
                out.writestr(info, blob)
    shutil.move(tmp, path)
    print("done:", path)


if __name__ == "__main__":
    main(sys.argv[1])
