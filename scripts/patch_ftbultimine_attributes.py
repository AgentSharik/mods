#!/usr/bin/env python3
"""Патч границ атрибутов FTB Ultimine.

ModAttributes регистрирует все четыре модификатора (max_blocks, cooldown,
exhaustion, experience) как RangedAttribute с границами
-Double.MAX_VALUE..Double.MAX_VALUE. Экран атрибутов ApothicAttributes
печатает эти границы как есть — подсказка превращается в 308-значные числа
на весь экран (скриншот из игры). Скрипт заменяет double-константы
константного пула на -1_000_000..1_000_000: для реальных значений
модификаторов запас многократный, а подсказка снова читается.

Использование: patch_ftbultimine_attributes.py <ModAttributes.class>
(файл перезаписывается на месте)
"""
import struct
import sys

LIMIT = 1_000_000.0
TWO_SLOT = {5, 6}
FIXED = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
         11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}


def patch(data: bytes) -> bytes:
    assert data[:4] == b'\xca\xfe\xba\xbe', 'not a class file'
    pos = 8
    (cp_count,) = struct.unpack_from('>H', data, pos)
    pos += 2
    out = bytearray(data[:pos])
    replaced = 0
    i = 1
    while i < cp_count:
        tag = data[pos]
        if tag == 1:
            (length,) = struct.unpack_from('>H', data, pos + 1)
            out += data[pos:pos + 3 + length]
            pos += 3 + length
            i += 1
        elif tag == 6:  # CONSTANT_Double
            (value,) = struct.unpack_from('>d', data, pos + 1)
            if value in (float('inf'),) or abs(value) == 1.7976931348623157e308:
                new = LIMIT if value > 0 else -LIMIT
                out += b'\x06' + struct.pack('>d', new)
                replaced += 1
            else:
                out += data[pos:pos + 9]
            pos += 9
            i += 2
        elif tag in FIXED:
            size = 1 + FIXED[tag]
            out += data[pos:pos + size]
            pos += size
            i += 2 if tag in TWO_SLOT else 1
        else:
            raise ValueError(f'unknown constant pool tag {tag} at {pos}')
    out += data[pos:]
    assert replaced == 2, f'expected 2 double bounds, replaced {replaced}'
    print(f'  replaced {replaced} double bounds -> +/-{LIMIT:g}')
    return bytes(out)


if __name__ == '__main__':
    for path in sys.argv[1:]:
        print(path)
        patched = patch(open(path, 'rb').read())
        open(path, 'wb').write(patched)
