#!/usr/bin/env python3
"""Замена строковой константы в .class файле (constant pool, CONSTANT_Utf8).

Использование: patch_class_string.py <in.class> <out.class> <старая строка> <новая строка>
Меняет только байты нужной Utf8-константы и её длину; индексы constant pool не сдвигаются,
поэтому остальные структуры класса остаются валидными. Подходит для строк, заданных
через Component.literal("...") и подобных хардкодов, которые нельзя перевести lang-файлом.
"""
import struct
import sys

TWO_SLOT = {5, 6}          # Long, Double
FIXED = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4, 11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}


def patch(data: bytes, old: str, new: str) -> bytes:
    assert data[:4] == b"\xca\xfe\xba\xbe", "not a class file"
    pos = 8  # magic + minor + major
    (cp_count,) = struct.unpack_from(">H", data, pos)
    pos += 2
    out = bytearray(data[:pos])
    replaced = 0
    i = 1
    while i < cp_count:
        tag = data[pos]
        if tag == 1:  # Utf8
            (length,) = struct.unpack_from(">H", data, pos + 1)
            raw = data[pos + 3: pos + 3 + length]
            text = raw.decode("utf-8")
            if text == old:
                encoded = new.encode("utf-8")
                out += b"\x01" + struct.pack(">H", len(encoded)) + encoded
                replaced += 1
            else:
                out += data[pos: pos + 3 + length]
            pos += 3 + length
            i += 1
        elif tag in FIXED:
            size = 1 + FIXED[tag]
            out += data[pos: pos + size]
            pos += size
            i += 2 if tag in TWO_SLOT else 1
        else:
            raise ValueError(f"unknown constant pool tag {tag} at {pos}")
    out += data[pos:]
    assert replaced, f"string not found: {old!r}"
    print(f"replaced {replaced} occurrence(s)")
    return bytes(out)


if __name__ == "__main__":
    src, dst, old, new = sys.argv[1:5]
    result = patch(open(src, "rb").read(), old, new)
    open(dst, "wb").write(result)
    print(f"{src} -> {dst} ({len(result)} bytes)")
