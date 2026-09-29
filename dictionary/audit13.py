# -*- coding: utf-8 -*-
"""Сверка партии 1 и 3: оригинальный en_us из модпака vs текущий ru_ru из релиза.

Ищет три класса дефектов:
  A) НЕ ПЕРЕВЕДЕНО   — строка побайтово совпадает с оригиналом
  B) ЧАСТИЧНО       — русский текст с остатками английских слов
  C) ПОДОЗРИТЕЛЬНО   — артефакты склейки («Only allows для», «Холод[Shift]Для»)
"""
import zipfile, json, re, io, glob, os, sys, collections

CY = re.compile(r'[А-Яа-яЁё]')
LAT = re.compile(r'[A-Za-z]')
MODS = ([l.strip() for l in open('party1.txt') if l.strip()]
        + [l.strip() for l in open('party3.txt') if l.strip()])
ORIG = '/home/user/dl/pack/mods'
REL = ['release/mods-ru-1.21.1-2026-09-28.zip', 'release/mods-ru-batch3-1.21.1.zip']

# английские слова, которые почти всегда означают «не доделано».
# бренды, единицы, идентификаторы и служебные токены сюда НЕ входят
STALE = re.compile(r'\b('
    r'insert|augment|augments|allows|smoking|recipes?|right|left|middle|click|details?|'
    r'copy|apply|settings|only|new|old|any|all|none|yes|no|and|or|but|with|without|'
    r'should|would|could|can|will|shall|does|did|have|has|was|were|been|being|'
    r'the|this|that|these|those|you|your|it|its|to|for|from|into|out|up|down|'
    r'block|blocks|item|items|entity|entities|player|players|mob|mobs|world|'
    r'craft|crafting|smelt|smelting|blast|smok|furnace|anvil|augment_|'
    r'use|used|using|make|made|get|give|add|remove|set|show|hide|open|close|'
    r'enabled?|disabled?|default|normal|vanilla|mode|type|level|tier|value|amount|'
    r'and/or|etc|vs|more|less|best|first|second|third|last|next|previous|'
    r'itemlink|recipefor|blockimage|subpages|position|parent|navigation|icon'
    r')\b', re.I)

# артефакты склейки русского и английского
GLUE = re.compile(
    r'[а-яё]{1,4}\s+(?:для|или|и|в|на|с)\s+[a-z]{3,}'      # «Only allows для smoking»
    r'|(?:ПКМ|ЛКМ|СКМ|Shift)\s+[a-z]{3,}'                    # «ПКМ insert augment»
    r'|§[0-9a-f][А-Яа-яЁёA-Za-z]{1,3}(?=§|$)'                # «Холод[Shift]»
    r'|\b(?:для|или)\b.*\b(?:the|and|with)\b', re.I)


def load_orig(base):
    for p in glob.glob(f'{ORIG}/*.jar'):
        if os.path.basename(p).startswith(base):
            z = zipfile.ZipFile(p)
            for n in z.namelist():
                if n.lower().endswith('en_us.json'):
                    return json.loads(z.read(n).decode('utf-8'))
    return None


def load_rel(base):
    for d in ('out_party1', 'out_party3'):
        for p in glob.glob(f'{d}/{base}*.jar'):
            inner = zipfile.ZipFile(p)
            for m in inner.namelist():
                if m.lower().endswith('ru_ru.json'):
                    return json.loads(inner.read(m).decode('utf-8')), p
    for rz in REL:
        if not os.path.exists(rz):
            continue
        z = zipfile.ZipFile(rz)
        for n in z.namelist():
            if not n.endswith('.jar') or not os.path.basename(n).startswith(base):
                continue
            inner = zipfile.ZipFile(io.BytesIO(z.read(n)))
            for m in inner.namelist():
                if m.lower().endswith('ru_ru.json'):
                    return json.loads(inner.read(m).decode('utf-8')), n
    return None, None


def main():
    A = []   # не переведено
    B = []   # частично
    C = []   # подозрительно
    tot = cyr = 0
    for base in MODS:
        en = load_orig(base)
        if en is None:
            print('!! нет оригинала:', base)
            continue
        ru, jar = load_rel(base)
        if ru is None:
            print('!! нет в релизе:', base)
            continue
        for k, v in en.items():
            if not isinstance(v, str):
                continue
            r = ru.get(k)
            if not isinstance(r, str):
                A.append((base, k, v, '<НЕТ КЛЮЧА>'))
                continue
            tot += 1
            if CY.search(r):
                cyr += 1
            if r == v and LAT.search(r):
                A.append((base, k, v, r))
                continue
            core = re.sub(r'§[0-9a-fk-orA-FK-OR]', '', r)
            core = re.sub(r'%[(\d\$]*[-#+ 0]*[0-9.*]*[a-zA-Z]|\{[^}]*\}', '', core)
            core = re.sub(r'<[^>]*>|\[[^\]]*\]\([^)]*\)|`[^`]*`|~~[^~]*~~', '', core)
            core = re.sub(r'[\w\-.]+:[a-z0-9_./]+', '', core)   # resource-location
            if CY.search(r) and STALE.search(core):
                B.append((base, k, v, r))
            if GLUE.search(core):
                C.append((base, k, v, r))
    print(f'\n=== ключей {tot} | с кириллицей {cyr} ({cyr*100//max(tot,1)}%)')
    for name, lst in (('A) НЕ ПЕРЕВЕДЕНО (строка == оригинал)', A),
                      ('B) ПЕРЕВЕДЕНО ЧАСТИЧНО (остались англ. слова)', B),
                      ('C) ПОДОЗРИТЕЛЬНО (артефакты склейки)', C)):
        print(f'\n########## {name}: {len(lst)}')
        seen = set()
        for base, k, v, r in lst:
            sig = (base, r)
            if sig in seen:
                continue
            seen.add(sig)
            print(f'  [{base}] {k}')
            print(f'      EN: {v[:150]}')
            print(f'      RU: {r[:150]}')


if __name__ == '__main__':
    main()
