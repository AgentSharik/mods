# -*- coding: utf-8 -*-
# Базы Chipped: id -> (русское имя, род главного слова: m/f/n/p)
WOODS = {
 'acacia': 'Акациевые', 'birch': 'Берёзовые', 'cherry': 'Вишнёвые',
 'crimson': 'Багровые', 'jungle': 'Тропические', 'mangrove': 'Мангровые',
 'spruce': 'Еловые', 'warped': 'Искажённые',
}
BASE = {}
def _wood(w, adj):
    BASE[f'{w}_planks'] = (f'{adj} доски', 'p')
    BASE[f'{w}_door'] = (f'{adj[:-2]}ая дверь', 'f')
    BASE[f'{w}_trapdoor'] = (f"{adj[:-2]}{'ий' if adj.endswith('ические') else 'ый'} люк", 'm')
for w,a in WOODS.items(): _wood(w,a)
BASE.update({
 'oak_planks': ('Дубовые доски','p'), 'oak_door': ('Дубовая дверь','f'), 'oak_trapdoor': ('Дубовый люк','m'),
 'dark_oak_planks': ('Доски из тёмного дуба','p'), 'dark_oak_door': ('Дверь из тёмного дуба','f'), 'dark_oak_trapdoor': ('Люк из тёмного дуба','m'),
 'bamboo_planks': ('Бамбуковые доски','p'), 'bamboo_door': ('Бамбуковая дверь','f'), 'bamboo_trapdoor': ('Бамбуковый люк','m'),
 'acacia_log': ('Акациевое бревно','n'), 'birch_log': ('Берёзовое бревно','n'), 'cherry_log': ('Вишнёвое бревно','n'),
 'jungle_log': ('Тропическое бревно','n'), 'mangrove_log': ('Мангровое бревно','n'), 'oak_log': ('Дубовое бревно','n'),
 'dark_oak_log': ('Бревно тёмного дуба','n'), 'spruce_log': ('Еловое бревно','n'),
 'acacia_leaves': ('Акациевая листва','f'), 'birch_leaves': ('Берёзовая листва','f'), 'dark_oak_leaves': ('Листва тёмного дуба','f'),
 'jungle_leaves': ('Тропическая листва','f'), 'oak_leaves': ('Дубовая листва','f'), 'spruce_leaves': ('Еловая листва','f'),
 'crimson_stem': ('Багровый стебель','m'), 'warped_stem': ('Искажённый стебель','m'),
 'stripped_acacia_log': ('Обтёсанное акациевое бревно','n'), 'stripped_birch_log': ('Обтёсанное берёзовое бревно','n'),
 'stripped_cherry_log': ('Обтёсанное вишнёвое бревно','n'), 'stripped_dark_oak_log': ('Обтёсанное бревно тёмного дуба','n'),
 'stripped_jungle_log': ('Обтёсанное тропическое бревно','n'), 'stripped_mangrove_log': ('Обтёсанное мангровое бревно','n'),
 'stripped_oak_log': ('Обтёсанное дубовое бревно','n'), 'stripped_spruce_log': ('Обтёсанное еловое бревно','n'),
 'stripped_crimson_stem': ('Обтёсанный багровый стебель','m'), 'stripped_warped_stem': ('Обтёсанный искажённый стебель','m'),
 'barrel': ('Бочка','f'), 'torch': ('Факел','m'), 'bookshelf': ('Книжная полка','f'),
 'amethyst_block': ('Аметистовый блок','m'), 'ancient_debris': ('Древние обломки','p'),
 'andesite': ('Андезит','m'), 'basalt': ('Базальт','m'), 'blackstone': ('Чернокамень','m'),
 'bone_block': ('Блок костей','m'), 'calcite': ('Кальцит','m'), 'clay': ('Глина','f'),
 'coal_block': ('Угольный блок','m'), 'cobblestone': ('Булыжник','m'), 'cobweb': ('Паутина','f'),
 'crying_obsidian': ('Плачущий обсидиан','m'), 'deepslate': ('Глубинный сланец','m'),
 'diamond_block': ('Алмазный блок','m'), 'diorite': ('Диорит','m'), 'dirt': ('Земля','f'),
 'dried_kelp_block': ('Блок сушёной ламинарии','m'), 'dripstone_block': ('Блок натёчного камня','m'),
 'emerald_block': ('Изумрудный блок','m'), 'end_stone': ('Эндерняк','m'),
 'gilded_blackstone': ('Позолоченный чернокамень','m'), 'glowstone': ('Светокамень','m'),
 'gold_block': ('Золотой блок','m'), 'granite': ('Гранит','m'), 'gravel': ('Гравий','m'),
 'hay_block': ('Сноп сена','m'), 'ice': ('Лёд','m'), 'blue_ice': ('Синий лёд','m'), 'packed_ice': ('Плотный лёд','m'),
 'iron_bars': ('Железные прутья','p'), 'iron_block': ('Железный блок','m'),
 'lapis_block': ('Лазуритовый блок','m'), 'lodestone': ('Магнетит','m'),
 'magma_block': ('Блок магмы','m'), 'netherite_block': ('Незеритовый блок','m'),
 'netherrack': ('Незерак','m'), 'obsidian': ('Обсидиан','m'), 'prismarine': ('Призмарин','m'),
 'dark_prismarine': ('Тёмный призмарин','m'), 'purpur_block': ('Блок пурпура','m'),
 'quartz_block': ('Блок кварца','m'), 'redstone_block': ('Блок красного камня','m'),
 'raw_copper_block': ('Блок необработанной меди','m'), 'raw_gold_block': ('Блок необработанного золота','m'),
 'raw_iron_block': ('Блок необработанного железа','m'), 'sand': ('Песок','m'), 'sandstone': ('Песчаник','m'),
 'red_sandstone': ('Красный песчаник','m'), 'smooth_stone': ('Гладкий камень','m'), 'stone': ('Камень','m'),
 'snow_block': ('Блок снега','m'), 'soul_sand': ('Песок душ','m'), 'sponge': ('Губка','f'),
 'tuff': ('Туф','m'), 'mud': ('Грязь','f'), 'packed_mud': ('Уплотнённая грязь','f'),
 'mud_bricks': ('Грязевые кирпичи','p'), 'bricks': ('Кирпичи','p'), 'borderless_bricks': ('Кирпичи без кромок','p'),
 'nether_bricks': ('Незер-кирпичи','p'), 'red_nether_bricks': ('Красные незер-кирпичи','p'),
 'mossy_cobblestone': ('Замшелый булыжник','m'), 'mossy_stone_bricks': ('Замшелые каменные кирпичи','p'),
 'brown_mushroom': ('Коричневый гриб','m'), 'red_mushroom': ('Красный гриб','m'),
 'brown_mushroom_block': ('Блок коричневого гриба','m'), 'red_mushroom_block': ('Блок красного гриба','m'),
 'mushroom_stem': ('Ножка гриба','f'), 'crimson_fungus': ('Багровый гриб','m'), 'warped_fungus': ('Искажённый гриб','m'),
 'crimson_roots': ('Багровые корни','p'), 'warped_roots': ('Искажённые корни','p'), 'mangrove_roots': ('Мангровые корни','p'),
 'nether_sprouts': ('Незер-рассада','f'), 'nether_wart_block': ('Блок адского нароста','m'),
 'warped_wart_block': ('Блок искажённого нароста','m'), 'pumpkin': ('Тыква','f'), 'carved_pumpkin': ('Вырезанная тыква','f'),
 'jack_o_lantern': ('Светильник Джека','m'), 'melon': ('Арбуз','m'), 'moss_block': ('Блок мха','m'),
 'pointed_dripstone': ('Натёчный камень','m'), 'ladder': ('Лестница','f'), 'lantern': ('Фонарь','m'),
 'sea_lantern': ('Морской фонарь','m'), 'soul_lantern': ('Фонарь душ','m'), 'lily_pad': ('Кувшинка','f'),
 'shroomlight': ('Грибосвет','m'), 'vine': ('Лиана','f'), 'glass_pane': ('Стеклянная панель','f'),
 'ochre_froglight': ('Охровая квампа','f'), 'verdant_froglight': ('Зелёная квампа','f'),
 'pearlescent_froglight': ('Перламутровая квампа','f'), 'redstone_lamp': ('Редстоун-лампа','f'),
 'redstone_torch': ('Редстоун-факел','m'),
 'waxed_copper_block': ('Вощёный медный блок','m'), 'waxed_oxidized_copper': ('Вощёная окисленная медь','f'),
 'waxed_weathered_copper': ('Вощёная потемневшая медь','f'), 'waxed_exposed_copper': ('Вощёная потускневшая медь','f'),
})
COLORS = {
 'black': 'Чёрный', 'white': 'Белый', 'blue': 'Синий', 'brown': 'Коричневый', 'cyan': 'Бирюзовый',
 'gray': 'Серый', 'green': 'Зелёный', 'lime': 'Лаймовый', 'magenta': 'Пурпурный', 'orange': 'Оранжевый',
 'pink': 'Розовый', 'purple': 'Фиолетовый', 'red': 'Красный', 'yellow': 'Жёлтый',
 'light_blue': 'Светло-синий', 'light_gray': 'Светло-серый',
}
def _fem(masc):
    if masc.endswith('ый'): return masc[:-2]+'ая'
    if masc.endswith('ой'): return masc[:-2]+'ая'
    if masc.endswith('ий'): return masc[:-2]+'яя'
    return masc
def _neu(masc):
    if masc.endswith('ый'): return masc[:-2]+'ое'
    if masc.endswith('ой'): return masc[:-2]+'ое'
    if masc.endswith('ий'): return masc[:-2]+'ее'
    return masc
for c, masc in COLORS.items():
    BASE[f'{c}_carpet'] = (f'{masc} ковёр', 'm')
    BASE[f'{c}_concrete'] = (f'{masc} бетон', 'm')
    BASE[f'{c}_wool'] = (f'{_fem(masc)} шерсть', 'f')
    BASE[f'{c}_terracotta'] = (f'{_fem(masc)} терракота', 'f')
    BASE[f'{c}_glazed_terracotta'] = (f'{_fem(masc)} глазурованная терракота', 'f')
    BASE[f'{c}_stained_glass'] = (f'{_neu(masc)} окрашенное стекло', 'n')
    BASE[f'{c}_stained_glass_pane'] = (f'{_fem(masc)} стеклянная панель', 'f')
BASE['terracotta'] = ('Терракота', 'f')
