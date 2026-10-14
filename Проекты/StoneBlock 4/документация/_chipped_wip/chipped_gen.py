# -*- coding: utf-8 -*-
import json, sys
sys.path.insert(0, '/tmp')
from chipped_bases import BASE
from chipped_vars import VAR

BASE.update({
    'crate': ('Ящик', 'm'),
    'glass': ('Стекло', 'n'),
    'leaded_glass': ('Свинцовое стекло', 'n'),
    'waxed_exposed_copper': ('Вощёная потускневшая медь', 'f'),
    'lily_pads': ('Кувшинки', 'p'),
    'autumnkin': ('Осенняя тыква', 'f'),
    'dewkin': ('Росистая тыква', 'f'),
    'goldkin': ('Золотая тыква', 'f'),
    'kabotchkin': ('Тыква кабоча', 'f'),
    'lumpkin': ('Комковатая тыква', 'f'),
    'pimpkin': ('Нарядная тыква', 'f'),
    'rosekin': ('Розовая тыква', 'f'),
})

def infl(word, gender):
    """Прилагательное м.р. -> форма по роду базы (m/f/n/p)."""
    if gender == 'm':
        return word
    parts = word.split('-')
    w = parts[-1]
    if w.endswith('шийся') or w.endswith('вшийся'):
        stem = w[:-5]
        parts[-1] = stem + {'f': 'шаяся', 'n': 'шееся', 'p': 'шиеся'}[gender]
    elif w.endswith('щийся'):
        stem = w[:-4]
        parts[-1] = stem + {'f': 'аяся', 'n': 'ееся', 'p': 'иеся'}[gender]
    elif w.endswith('ший'):
        parts[-1] = w[:-3] + {'f': 'шая', 'n': 'шее', 'p': 'шие'}[gender]
    elif w.endswith('ый') or w.endswith('ой'):
        parts[-1] = w[:-2] + {'f': 'ая', 'n': 'ое', 'p': 'ые'}[gender]
    elif w.endswith('ий'):
        stem = w[:-2]
        last = stem[-1] if stem else ''
        if last in 'гкх':
            parts[-1] = stem + {'f': 'ая', 'n': 'ое', 'p': 'ие'}[gender]
        elif last in 'жшщч':
            parts[-1] = stem + {'f': 'ая', 'n': 'ее', 'p': 'ие'}[gender]
        else:
            parts[-1] = stem + {'f': 'яя', 'n': 'ее', 'p': 'ие'}[gender]
    else:
        raise ValueError('no inflection: ' + w)
    return '-'.join(parts)

def adj(token, gender):
    v = VAR[token]
    if isinstance(v, tuple):
        return v[1]
    if isinstance(v, dict):
        return v[gender]
    return infl(v, gender)

def make_name(prefixes, base_id, suffixes):
    base_ru, gender = BASE[base_id]
    words = []
    for t in prefixes:
        a = adj(t, gender)
        if a: words.append(a)
    for t in suffixes:
        a = adj(t, gender)
        if a: words.append(a)
    if words:
        base_ru = base_ru[0].lower() + base_ru[1:]
    words.append(base_ru)
    s = ' '.join(words)
    return s[0].upper() + s[1:]

D = json.load(open('/tmp/chipped_dec2.json'))
decomp = {k: (v[0], v[1], v[2]) for k, v in D['decomp'].items()}

WOODS = ['acacia','birch','crimson','dark_oak','jungle','mangrove','oak','spruce','warped']
for w in WOODS:
    decomp[f'{w}_crate'] = (w.split('_'), 'crate', [])
    decomp[f'reinforced_{w}_crate'] = (['reinforced'] + w.split('_'), 'crate', [])
for pre in ['ancient','chipped','embossed','engraved','layered','plated','pressed','reinforced','sheet','shuttered','stacked']:
    decomp[f'{pre}_waxed_exposed_copper'] = ([pre], 'waxed_exposed_copper', [])
for pre in ['clear','fancy','ornate','raster','square','vertical']:
    decomp[f'{pre}_leaded_glass'] = ([pre], 'leaded_glass', [])
decomp['arched_leaded_glass_pillar'] = (['arched'], 'leaded_glass', ['pillar'])
decomp['large_diamond_leaded_glass'] = (['large','diamond'], 'leaded_glass', [])
decomp['small_diamond_leaded_glass'] = (['small','diamond'], 'leaded_glass', [])
decomp['lead_woven_glass'] = (['lead','woven'], 'glass', [])
decomp['circle_oak_glass'] = (['circle','oak'], 'glass', [])
decomp['square_oak_glass'] = (['square','oak'], 'glass', [])
for mid in ['bared','bordered','diamond_bordered','horizontal_lined','large_diamond','line_bared','ornate_bared','snowflake','woven']:
    toks = mid.split('_')
    decomp[f'oak_{mid}_glass'] = (['oak']+toks, 'glass', [])
decomp['micro_lily_pads'] = (['micro'], 'lily_pads', [])
decomp['dark_micro_lily_pads'] = (['dark','micro'], 'lily_pads', [])
for pk in ['autumnkin','dewkin','goldkin','lumpkin','pimpkin','rosekin']:
    decomp[f'dirty_{pk}'] = (['dirty'], pk, []) if pk in ('dewkin','goldkin','rosekin') else decomp.get(f'dirty_{pk}')
for pk in ['autumnkin','lumpkin','pimpkin']:
    decomp[f'overgrown_{pk}'] = (['overgrown'], pk, [])

MANUAL = {
 'block.chipped.alchemy_bench': 'Алхимический верстак',
 'block.chipped.botanist_workbench': 'Верстак ботаника',
 'block.chipped.carpenters_table': 'Верстак плотника',
 'block.chipped.glassblower': 'Верстак стеклодува',
 'block.chipped.loom_table': 'Ткацкий верстак',
 'block.chipped.mason_table': 'Верстак каменщика',
 'block.chipped.tinkering_table': 'Верстак изобретателя',
 'block.chipped.netherite_safe': 'Незеритовый сейф',
 'block.chipped.autumnkin': 'Осенняя тыква',
 'block.chipped.dewkin': 'Росистая тыква',
 'block.chipped.goldkin': 'Золотая тыква',
 'block.chipped.kabotchkin': 'Тыква кабоча',
 'block.chipped.lumpkin': 'Комковатая тыква',
 'block.chipped.pimpkin': 'Нарядная тыква',
 'block.chipped.rosekin': 'Розовая тыква',
 'container.chipped.alchemy_bench': 'Алхимический верстак',
 'container.chipped.botanist_workbench': 'Верстак ботаника',
 'container.chipped.carpenters_table': 'Верстак плотника',
 'container.chipped.glassblower': 'Верстак стеклодува',
 'container.chipped.loom_table': 'Верстак пастуха',
 'container.chipped.mason_table': 'Верстак каменщика',
 'container.chipped.tinkering_table': 'Верстак изобретателя',
 'container.chipped.workbench': 'Верстак',
 'item.chipped.alchemy_book': 'Книга алхимии',
 'item.chipped.chisel': 'Стамеска',
 'item.chipped.multimeter': 'Мультиметр',
 'item.chipped.needles': 'Иглы',
 'item.chipped.saw': 'Пила',
 'item.chipped.watering_can': 'Лейка',
 'itemGroup.chipped.main': 'Chipped',
 'text.chipped.craft': 'Создать',
 'block.chipped.bad_time_carved_pumpkin': 'Вырезанная тыква «Плохое время»',
 'text.chipped.craft_all': 'Создать всё',
 'text.chipped.horizontal': 'Горизонтально',
 'text.chipped.vertical': 'Вертикально',
 'text.chipped.preview': 'Предпросмотр',
 'text.chipped.single': 'Одиночный',
 'text.chipped.two_by_two': '2×2',
}

PLURAL_HEAD = {
 'Блок': 'Блоки', 'ковёр': 'ковры', 'бетон': 'бетон', 'терракота': 'терракота', 'шерсть': 'шерсть',
 'стекло': 'стёкла', 'панель': 'панели', 'дверь': 'двери', 'люк': 'люки', 'бревно': 'брёвна',
 'бочка': 'бочки', 'факел': 'факелы', 'книжная полка': 'книжные полки', 'тыква': 'тыквы',
 'фонарь': 'фонари', 'лианы': 'лианы', 'трава': 'травы', 'листва': 'листва', 'грибосвет': 'грибосвет',
 'квампа': 'квампы', 'магнетит': 'магнетиты', 'Эндерняк': 'эндерняк', 'тростник': 'тростник',
 'песчаник': 'песчаник', 'халцедон': 'халцедон', 'плита': 'плиты', 'кирпичи': 'кирпичи',
 'кирпич': 'кирпичи', 'доски': 'доски', 'доска': 'доски', 'стебель': 'стебли', 'сноп': 'снопы',
 'тропинка': 'тропинки', 'Ящик': 'ящики', 'Кувшинки': 'кувшинки', 'медь': 'медь',
 'светильник': 'светильники', 'Светильник': 'светильники', 'грязь': 'грязь', 'земля': 'земля', 'блок': 'блоки', 'Бочка': 'бочки', 'гриб': 'грибы', 'обломки': 'обломки', 'Кирпичи': 'Кирпичи', 'лёд': 'лёд',
 'песок': 'песок', 'камень': 'камень', 'лава': 'лава', 'саженец': 'саженцы', 'сахарный': None,
}
MULTIWORD_PLURAL = {
 'Книжная полка': 'Книжные полки', 'Светильник Джека': 'Светильники Джека',
 'Обтёсанное бревно': 'Обтёсанные брёвна', 'Укреплённая яма': None,
}

def plural_adj(w):
    if w.endswith('ая'): return w[:-2]+'ые'
    if w.endswith('яя'): return w[:-2]+'ие'
    if w.endswith('ое'): return w[:-2]+'ые'
    if w.endswith('ее'): return w[:-2]+'ие'
    if w.endswith('ый'): return w[:-2]+'ые'
    if w.endswith('ой'): return w[:-2]+'ые'
    if w.endswith('ий'): return w[:-2]+'ие'
    if w.endswith('щееся'): return w[:-5]+'щиеся'
    return w

def pluralize(base_ru):
    if base_ru.startswith('Блок '):
        return 'Блоки ' + base_ru[5:]
    for mw, pl in MULTIWORD_PLURAL.items():
        if pl and base_ru.startswith(mw):
            return pl + base_ru[len(mw):]
    words = base_ru.split(' ')
    hi = None
    for i, w in enumerate(words):
        if w in PLURAL_HEAD:
            hi = i; break
    if hi is None:
        return 'P:' + base_ru
    if words[hi] in UNCOUNTABLE:
        return base_ru
    out = [plural_adj(w) for w in words[:hi]]
    out.append(PLURAL_HEAD[words[hi]] or words[hi])
    out.extend(words[hi+1:])
    return ' '.join(out)

UNCOUNTABLE = {'бетон','терракота','шерсть','листва','грязь','земля','песок','камень','медь','лава','тростник','песчаник','халцедон','грибосвет','Эндерняк','лёд','Андезит','Базальт','Чернокамень','обломки'}


TAG_OVERRIDE = {
 'andesite':'Андезит','basalt':'Базальт','blackstone':'Чернокамень','calcite':'Кальцит','clay':'Глина',
 'cobblestone':'Булыжник','cobweb':'Паутина','crimson_roots':'Багровые корни','crying_obsidian':'Плачущий обсидиан',
 'dark_oak_door':'Двери из тёмного дуба','dark_oak_leaves':'Листва тёмного дуба','dark_oak_log':'Брёвна тёмного дуба',
 'dark_oak_planks':'Доски из тёмного дуба','dark_oak_trapdoor':'Люки из тёмного дуба','dark_prismarine':'Тёмный призмарин',
 'deepslate':'Глубинный сланец','diorite':'Диорит','dirt':'Земля','gilded_blackstone':'Позолоченный чернокамень',
 'glowstone':'Светокамень','granite':'Гранит','gravel':'Гравий','hay_block':'Снопы сена','ice':'Лёд',
 'iron_bars':'Железные прутья','ladder':'Лестницы','lantern':'Фонари','lily_pad':'Кувшинки','lodestone':'Магнетит',
 'mangrove_roots':'Мангровые корни','melon':'Арбузы','mossy_cobblestone':'Замшелый булыжник','mud':'Грязь',
 'mushroom_stem':'Ножки грибов','nether_bricks':'Незер-кирпичи','nether_sprouts':'Незер-рассада','netherrack':'Незерак',
 'obsidian':'Обсидиан','prismarine':'Призмарин','pumpkin':'Тыквы','red_nether_bricks':'Красные незер-кирпичи',
 'redstone_lamp':'Редстоун-лампы','redstone_torch':'Редстоун-факелы','sand':'Песок','sandstone':'Песчаник',
 'shroomlight':'Грибосвет','soul_lantern':'Фонари душ','soul_sand':'Песок душ','sponge':'Губки','stone':'Камень',
 'terracotta':'Терракота','torch':'Факелы','tuff':'Туф','vine':'Лианы','warped_roots':'Искажённые корни',
 'special_lantern':'Особые фонари','special_soul_lantern':'Особые фонари душ',
 'waxed_exposed_copper_block':'Блоки вощёной потускневшей меди',
}

def tag_name(tag_id):
    if tag_id in TAG_OVERRIDE:
        return TAG_OVERRIDE[tag_id]
    base_ru, gender = BASE[tag_id]
    return pluralize(base_ru)

def main():
    en = json.load(open('/home/user/mods/Проекты/StoneBlock 4/документация/_chipped_wip/en_us.json'))
    ru = {}
    unresolved = []
    for key in en:
        if key in MANUAL:
            ru[key] = MANUAL[key]
        elif key.startswith('block.chipped.'):
            bid = key[len('block.chipped.'):]
            if bid in decomp:
                pre, base_id, suf = decomp[bid]
                try:
                    ru[key] = make_name(pre, base_id, suf)
                except Exception as e:
                    unresolved.append((key, str(e)))
            else:
                unresolved.append((key, 'no decomp'))
        elif key.startswith('tag.item.chipped.'):
            tid = key[len('tag.item.chipped.'):]
            if tid in TAG_OVERRIDE:
                ru[key] = TAG_OVERRIDE[tid]
            elif tid in BASE:
                ru[key] = tag_name(tid)
            else:
                unresolved.append((key, 'tag no base'))
        else:
            unresolved.append((key, 'other'))
    print('total', len(en), 'done', len(ru), 'unresolved', len(unresolved))
    for u in unresolved[:80]: print('UNRES:', u)
    json.dump(ru, open('/tmp/chipped_ru.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
    # теги для вычитки
    with open('/tmp/chipped_tags.txt', 'w', encoding='utf-8') as f:
        for key in en:
            if key.startswith('tag.'):
                f.write(f"{en[key]}  ->  {ru.get(key)}\n")

if __name__ == '__main__':
    main()
