import json, re, collections
en = json.load(open('work/betterblockz_en.json', encoding='utf-8'))
COLORS = {'White':('белый','белая','белое'),'Orange':('оранжевый','оранжевая','оранжевое'),
'Magenta':('Пурпурный','пурпурная','пурпурное'),'Light Blue':('голубой','голубая','голубое'),
'Yellow':('жёлтый','жёлтая','жёлтое'),'Lime':('Лаймовый','лаймовая','лаймовое'),
'Pink':('розовый','розовая','розовое'),'Gray':('серый','серая','серое'),
'Light Gray':('светло-серый','светло-серая','светло-серое'),'Cyan':('бирюзовый','бирюзовая','бирюзовое'),
'Purple':('фиолетовый','фиолетовая','фиолетовое'),'Blue':('синий','синяя','синее'),
'Brown':('коричневый','коричневая','коричневое'),'Green':('зелёный','зелёная','зелёное'),
'Red':('красный','красная','красное'),'Black':('чёрный','чёрная','чёрное')}
FAM = {  # имя -> (русское имя, род m/f/n, тип: n=обычное, p=фраза целиком)
 'Zeno':('Зено','m','n'),'Zeon':('Зеон','m','n'),'Zome':('Зом','m','n'),'Zoea':('Зоэа','f','n'),
 'Zoea Base':('Зоэа','f','b'),'Agon':('Агон','m','n'),'Armz':('Армз','m','n'),'Azur':('Азур','m','n'),
 'Bitt':('Битт','m','n'),'Cray':('Крей','m','n'),'Fort':('Форт','m','n'),'Iszm':('Изм','m','n'),
 'Jelt':('Джелт','m','n'),'Korp':('Корп','m','n'),'Kryp':('Крип','m','n'),'Lave':('Лейв','m','n'),
 'Myst':('Мист','m','n'),'Reed':('Рид','m','n'),'Vect':('Вект','m','n'),'Vena':('Вена','f','n'),
 'Zane':('Зейн','m','n'),'Ztyl':('Стайл','m','n'),'Sync':('Синк','m','n'),'Mint':('Минт','m','n'),
 'Eclipse':('Эклипс','m','n'),'Cyber':('Кибер','m','n'),
 'Lab Link':('лабораторное соединение','n','n'),'Reactor':('реактор','m','n'),
 'Amber Core':('янтарное ядро','n','n'),'Iron Slate':('железный сланец','m','n'),
 'Synth Alloy':('синтетический сплав','m','n'),'Tank':('бак','m','n'),
 'Cyber Light':('киберсвет','m','n'),'Cyber Light ◇':('киберсвет-ромб','m','n'),
 'Cyber Light Bar':('киберсветовая планка','f','n'),'Cyber Light Bar (Vertical)':('вертикальная киберсветовая планка','f','n'),
 'Cyber Light Flat':('плоский киберсвет','m','n'),'Cyber Light Panel':('киберсветовая панель','f','n'),
 'Cyber Light Secret':('секретный киберсвет','m','n'),
 'Forge Copper':('кованая медь','f','n'),'Number Decal':('цифровая наклейка','f','n'),
 'Tinted Glass':('тонированное стекло','n','n'),'Zeno Hex':('Зено-гекс','m','n'),
 'Luxe Iron':('люксовое железо','n','n'),'Luxe Copper':('люксовая медь','f','n'),
 'Luxe Diamond':('люксовый алмаз','m','n'),'Luxe Gold':('люксовое золото','n','n'),
 'Crimson Slate':('багровый сланец','m','n'),
 'Metal Zone Copper':('Метал-зона: медь','f','p'),'Metal Zone Iron':('Метал-зона: железо','n','p'),
 'Metal Zone Oxidized Copper':('Метал-зона: окисленная медь','f','p'),'Metal Zone Rusty Iron':('Метал-зона: ржавое железо','n','p'),
 'Lair Blood':('Логово: кровь','n','p'),'Lair Decay':('Логово: тлен','m','p'),
 'Lair Flesh':('Логово: плоть','f','p'),'Lair Husk':('Логово: оболочка','f','p'),'Lair Stain':('Логово: пятно','n','p'),
 'Diamond Frame Platform':('алмазная рамная платформа','f','n'),'Gold Frame Platform':('золотая рамная платформа','f','n'),
 'Iron Slate: Redstone':('Железный сланец: редстоун','m','p'),'Polished Stone':('Полированный камень','m','p'),
 'Full Glow Light Block':('Светоблок полной яркости','m','p'),
}
COLOR_LIST = sorted(COLORS, key=len, reverse=True)
def parse(v):
    s = v; out = {}
    for suf, key in [(' (CTM)','ctm'), (' [Pillar]','pillar')]:
        if s.endswith(suf): out[key] = True; s = s[:-len(suf)]
    if s.endswith(' Base'): out['base'] = True; s = s[:-5]
    m = re.search(r'\b(\d+)\b', s)
    if m: out['num'] = m.group(1)
    for c in COLOR_LIST:
        if re.search(r'\b' + re.escape(c) + r'\b', s): out['color'] = c; break
    fam = s
    if 'color' in out: fam = fam.replace(out['color'], '')
    if 'num' in out: fam = re.sub(r'\b' + out['num'] + r'\b', '', fam)
    out['fam'] = re.sub(r'\s+', ' ', fam).strip(' -')
    return out
def base_adj(g): return {'m':'Базовый','f':'Базовая','n':'Базовое'}[g]
ru = {}; unknown = []
for k, v in en.items():
    if not k.startswith('block.'): continue
    p = parse(v)
    f = FAM.get(p['fam'])
    if f is None: unknown.append((k, v, p['fam'])); continue
    name, g, typ = f
    if typ == 'p':
        parts = [name]
        if p.get('base'):
            parts.append('(' + base_adj(g).lower() + ')')
    else:
        words = []
        if p.get('base') or typ == 'b': words.append(base_adj(g))
        if 'color' in p: words.append(COLORS[p['color']][{'m':0,'f':1,'n':2}[g]])
        words.append(name)
        parts = [' '.join(words)]
    if 'num' in p: parts.append(p['num'])
    if p.get('ctm'): parts.append('(CTM)')
    if p.get('pillar'): parts.append('(столб)')
    r = ' '.join(parts)
    ru[k] = r[0].upper() + r[1:]
print('блоков переведено:', len(ru), '| неизвестных семей:', len(unknown))
for u in unknown[:20]: print('  ?', u)
json.dump(ru, open('work/bbz_blocks.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
