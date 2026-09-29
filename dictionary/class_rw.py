# -*- coding: utf-8 -*-
"""Замена строк в .class.

Ключевая идея: строки в constant pool заменяются строго 1:1, порядок и количество
записей не меняются, значит все индексы (this_class, name_index, attribute_name_index…)
остаются валидными. В формате class нет абсолютных смещений — всё относительное,
поэтому после пересобранного constant pool остальной файл копируется байт в байт.
"""
import struct

FIXED = {7: 2, 8: 2, 16: 2, 19: 2, 20: 2, 15: 3, 3: 4, 4: 4, 9: 4, 10: 4, 11: 4,
         12: 4, 17: 4, 18: 4}


def parse_pool(data):
    """→ [(tag, payload_bytes), …] — payload для UTF8 это содержимое, для прочих сырые байты."""
    if data[:4] != b'\xca\xfe\xba\xbe':
        raise ValueError('не class-файл')
    n = struct.unpack('>H', data[8:10])[0]
    off = 10
    i = 1
    out = []
    while i < n:
        tag = data[off]
        off += 1
        if tag == 1:
            ln = struct.unpack('>H', data[off:off + 2])[0]
            off += 2
            out.append((tag, data[off:off + ln]))
            off += ln
        elif tag in (5, 6):
            out.append((tag, data[off:off + 8]))
            off += 8
            i += 1
        elif tag in FIXED:
            out.append((tag, data[off:off + FIXED[tag]]))
            off += FIXED[tag]
        else:
            raise ValueError(f'неизвестный тег {tag} на {off}')
        i += 1
    return out, off


def render_pool(entries, head):
    """head — первые 10 байт оригинала: магия, minor, major, constant_pool_count."""
    out = bytearray(head)
    for tag, payload in entries:
        if tag == 1:
            out += bytes((1,)) + struct.pack('>H', len(payload)) + payload
        else:
            out += bytes((tag,)) + payload
    return bytes(out)


def pool_strings(data):
    entries, _ = parse_pool(data)
    for tag, payload in entries:
        if tag == 1:
            try:
                yield payload.decode('utf-8')
            except UnicodeDecodeError:
                pass


def patch_class(data, table):
    """table: {английская строка: русская} → (новые байты, число замен)."""
    entries, end = parse_pool(data)
    changed = 0
    for i, (tag, payload) in enumerate(entries):
        if tag != 1:
            continue
        try:
            s = payload.decode('utf-8')
        except UnicodeDecodeError:
            continue
        if s in table:
            entries[i] = (1, table[s].encode('utf-8'))
            changed += 1
    return render_pool(entries, data[:10]) + data[end:], changed
