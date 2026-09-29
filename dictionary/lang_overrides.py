# -*- coding: utf-8 -*-
"""Точечные переводы UI-ключей, которые движок имён не берёт.

Это короткие служебные подписи (кнопки, тултипы, переключатели),
которые не проходят через общий словарь названий. Применяются
поверх собранных JAR.

Запуск: python3 lang_overrides.py
"""
import json, zipfile, shutil, pathlib, re, sys

CYR = re.compile(r'[А-Яа-яЁё]')

OVERRIDES = {
    'collapsible_groups.ore_editor.settings.back_icon.desc': 'iconIds[1]',
    'collapsible_groups.ore_editor.settings.front_icon.desc': 'iconIds[0]',

    'collapsible_groups.config.val.off': 'ВЫКЛ',
    'collapsible_groups.config.val.on': 'ВКЛ',
    'collapsible_groups.editor.rules.picker.confirm': 'ОК',
    'collapsible_groups.editor.unsupported_node.all.label': 'ВСЕ',
    'collapsible_groups.editor.unsupported_node.not.label': 'НЕТ',
    'item.tempad.location_card.id': 'ID: %s',
    'modularrouters.guiText.tooltip.activator.entityMode': 'Сущность',
    'units.bookshelf.tick': 'Тик',
    'xnet.enum.items.extractmode.rnd': 'Случ.',
    'xnet.item.meta.label': 'Мета',

    'affix.irons_apothic:elemental/school_blood/necromancers': 'Некромансера',
    'affix.irons_apothic:elemental/school_evocation/conjurer': 'Колдуна',
    'affix.irons_apothic:elemental/school_fire/pyromancers': 'Пиромансера',
    'affix.irons_apothic:elemental/school_geo/geomancers': 'Геомансера',
    'affix.irons_apothic:elemental/school_holy/clerics': 'Жреца',
    'affix.irons_apothic:elemental/school_hydro/hydromancer': 'Гидромансера',
    'affix.irons_apothic:elemental/school_ice/cryomancers': 'Криомансера',
    'affix.irons_apothic:elemental/school_lightning/electromancers': 'Электромансера',
    'affix.irons_apothic:elemental/school_nature/terraformer': 'Терраформера',
    'affix.irons_apothic:elemental/school_necro/lich': 'Лича',
    'affix.irons_apothic:elemental/school_ritual/ritualist': 'Ритуалиста',
    'affix.irons_apothic:elemental/school_shadow/warlocks': 'Чародея',
    'affix.irons_apothic:elemental/school_symmetry/equilibrium': 'Равновесия',
    'affix.irons_apothic:elemental/school_technomancy/technocrat': 'Технократа',
    'affix.irons_apothic:elemental/school_wind/aeromancer': 'Аэромансера',
    'app.tempad.portal_setup.angle': 'Угол:',
    'collapsible_groups.editor.rules.picker.search': 'Поиск...',
    'collapsible_groups.editor.rules.reference.item_search': 'Поиск...',
    'collapsible_groups.editor.search_hint': 'Поиск...',
    'commands.toolkit.hand.handempty': '--РУКА ПУСТА--',
    'ftbbackups3.lang.autosave.disabled': 'Отключено!',
    'gui.structurify.structures.structure.biomes.title': 'Биомы',
    'message.rftoolspower.blazing_rod.duration': 'Длительность:',
    'message.rftoolspower.blazing_rod.infused': 'Пропитано:',
    'message.rftoolspower.coalgenerator.info': 'Производство:',
    'message.rftoolsutility.blindness_module.power': 'Использования:',
    'message.rftoolsutility.counter_module.info': 'Счётчик:',
    'message.rftoolsutility.counterplus_module.info': 'Счётчик:',
    'message.rftoolsutility.energy_module.info': 'Наблюдение:',
    'message.rftoolsutility.energyplus_module.info': 'Наблюдение:',
    'message.rftoolsutility.featherfalling_module.power': 'Использования:',
    'message.rftoolsutility.featherfallingplus_module.power': 'Использования:',
    'message.rftoolsutility.flight_module.power': 'Использования:',
    'message.rftoolsutility.fluid_module.info': 'Наблюдение:',
    'message.rftoolsutility.fluidplus_module.info': 'Наблюдение:',
    'message.rftoolsutility.glowing_module.power': 'Использования:',
    'message.rftoolsutility.haste_module.power': 'Использования:',
    'message.rftoolsutility.hasteplus_module.power': 'Использования:',
    'message.rftoolsutility.inventory_module.info': 'Наблюдение:',
    'message.rftoolsutility.inventoryplus_module.info': 'Наблюдение:',
    'message.rftoolsutility.luck_module.power': 'Использования:',
    'message.rftoolsutility.machineinformation_module.info': 'Наблюдение:',
    'message.rftoolsutility.matter_transmitter.once': 'Один раз:',
    'message.rftoolsutility.nightvision_module.power': 'Использования:',
    'message.rftoolsutility.noteleport_module.power': 'Использования:',
    'message.rftoolsutility.peaceful_module.power': 'Использования:',
    'message.rftoolsutility.poison_module.power': 'Использования:',
    'message.rftoolsutility.redstone_information.channels': 'Каналы:',
    'message.rftoolsutility.redstone_module.info': 'Наблюдение:',
    'message.rftoolsutility.regeneration_module.power': 'Использования:',
    'message.rftoolsutility.regenerationplus_module.power': 'Использования:',
    'message.rftoolsutility.saturation_module.power': 'Использования:',
    'message.rftoolsutility.saturationplus_module.power': 'Использования:',
    'message.rftoolsutility.screen_link.info': 'Экран:',
    'message.rftoolsutility.simple_dialer.once': 'Один раз:',
    'message.rftoolsutility.slowness_module.power': 'Использования:',
    'message.rftoolsutility.speed_module.power': 'Использования:',
    'message.rftoolsutility.speedplus_module.power': 'Использования:',
    'message.rftoolsutility.syringe.level': 'Уровень:',
    'message.rftoolsutility.waterbreathing_module.power': 'Использования:',
    'message.rftoolsutility.weakness_module.power': 'Использования:',
    'message.xnet.antenna.one': 'Один',
    'message.xnet.antenna.two': 'Два',
    'message.xnet.facade.info': 'Подмена:',
    'modularrouters.itemText.augments': 'Усиления:',
    'modularrouters.itemText.misc.flags': 'Флаги',
    'sound.xycraft_machines.subtitle.soaryn_box_deposit': 'Упс!',
    'xnet.directions.label': 'Направления:',
    'xnet.positon.label': 'Позиция:',

    # Единственная переводимая здесь строка — игровой текст.
    'booklet.actuallyadditions.chapter.engineer_house.text.2': '<i>     Станки Primus',
    'mod_gui.brandonscore.energy_bar.input':   'Вход',
    'mod_gui.brandonscore.energy_bar.output':  'Выход',
    'mod_gui.brandonscore.entity_filter.search': 'Поиск...',
    'main_screen.side_screen.radius_edit':      'Радиус:',
    'main_screen.side_screen.title':            'Заголовок:',
    'main_screen.side_screen.x':                'X: %s',
    'main_screen.side_screen.y':                'Y: %s',
    'main_screen.side_screen.z':                'Z: %s',
    'kiwi.config.debug':                        'Отладка',
    'lychee.config.debug':                      'Отладка',
    'rgp_client.gui.loading_entries':           'Загрузка...',
    'dimension.minecraft.overworld':            'Обычный мир',
    'postAction.lychee.drop_xp':                '%s опыта',
    'contextual.lychee.secret':                 '§o???',
    'tooltip.ironfurnaces.gui_input_output': 'Вход/Выход',
    'tooltip.ironfurnaces.gui_none':          'НЕТ',
    'tooltip.ironfurnaces.heaterX':          'X:',
    'tooltip.ironfurnaces.heaterY':          'Y:',
    'tooltip.ironfurnaces.heaterZ':          'Z:',
    'tooltip.irons_lib.transmog_option.title': 'Трансморф Patreon:',
    'transmog.irons_lib.witchhunter':         'Ведьма-охотница',
    'laserio.tooltip.item.card.Filter':       'Фильтр:',
    'block.mob_grinding_utils.solid_xp_block.tooltip_1': 'Пружинит.',
    'message.rftoolsbuilder.shape_card.dimension': 'Измерение:',
    'bonsaitrees4.tooltip.and_more':          '+%d',
}

# Технические и служебные подписи остаются на английском (правило пользователя).
# ── ручные исправления партий 1 и 3 (сверка с ОРИГИНАЛЬНЫМ en_us из модпака) ──
try:
    from fix13_map import FIX as _FIX13
    OVERRIDES.update(_FIX13)
    print(f'📝 применено ручных исправлений: {len(_FIX13)}')
except Exception as _e:                      # noqa: BLE001
    print('⚠️  fix13_map не загружен:', _e)

INTENTIONAL = {
    'block.rep_ae2_bridge.repae2bridge', 'item.rep_ae2_bridge.repae2bridge',
    'c_i',
    'c_ii',
    'c_iii',
    'c_iv',
    'c_v',
    'c_vi',
    'c_vii',
    'c_viii',
    'c_ix',
    'c_x',
    'c_xi',
    'c_xii',
    'c_xiii',
    'c_xiv',
    'c_xv',
    'c_xvi',
    'c_xvii',
    'c_xviii',
    'c_xix',
    'c_xx',
    'c_c',
    'c_ci',
    'c_cii',
    'c_ciii',
    'c_civ',
    'c_cv',
    'c_cvi',
    'c_cvii',
    'c_cviii',
    'c_cix',
    'c_cx',
    'c_cxi',
    'c_cxii',
    'c_cxiii',
    'c_cxiv',
    'c_cxv',
    'c_cxvi',
    'c_cxvii',
    'c_cxviii',
    'c_cxix',
    'c_cxx',
    'info.actuallyadditions.gui.disabled',
    'info.actuallyadditions.gui.inbound',
    'info.actuallyadditions.gui.outbound',
    'tooltip.actuallyadditions.item_filling_wand.selected_block.none',
    'tooltip.actuallyadditions.meta.desc',
    'tooltip.actuallyadditions.noOredictNameAvail.desc',
    'tooltip.actuallyadditions.onSuffix.desc',
    'chisel.tooltip.fuzzy.disabled',
    'chisel.tooltip.fuzzy.enabled',
    'rechiseled.chiseling.connecting.on',
    'rechiseled.chiseling.connecting.off',
    '_comment', 'booklet.actuallyadditions.fontSize.large',
    'booklet.actuallyadditions.fontSize.medium',
    'booklet.actuallyadditions.fontSize.small',
    'container.actuallyadditions.inputter',
    'info.actuallyadditions.gui.the',
    'misc.actuallyadditions.energy', 'misc.actuallyadditions.energy_tick',
    'misc.actuallyadditions.power_double_short',
    'misc.actuallyadditions.power_name_short',
    'misc.actuallyadditions.power_single_short',
    'tooltip.actuallyadditions.nbt.desc',
    'a.lang.author.name',
    'container.chisel.hitech', 'item.chisel.hitech_chisel',
    'chisel.tooltip.power.pertick', 'chisel.tooltip.power.stored',
    'mod_gui.brandonscore.energy_bar.io', 'mod_gui.brandonscore.energy_bar.op',
    'mod_gui.brandonscore.energy_bar.rf', 'op.brandonscore.op',
    'contextual.lychee', 'postAction.lychee.set_block', 'result.lychee.default',
    'tooltip.ironfurnaces.spooky1', 'tooltip.irons_lib.shift_tooltip',
    'itemGroup.buildinggadgets2', 'idlecinematics.settings.fov',
    'idlecinematics.settings.page.hud', 'bonsaitrees4.tooltip.and_more',
    'tip.lychee.sec', 'result.lychee.true', 'result.lychee.false',
    'contextual.lychee.secret', 'contextual.lychee',
    'postAction.lychee.set_block', 'result.lychee.default',
    'mod_gui.brandonscore.energy_bar.io', 'mod_gui.brandonscore.energy_bar.op',
    'mod_gui.brandonscore.energy_bar.rf', 'op.brandonscore.op',
    'main_screen.side_screen.x',
    'main_screen.side_screen.y',
    'main_screen.side_screen.z',
    'idlecinematics.settings.emotecraft_status',
    'tooltip.ironfurnaces.heaterX',
    'tooltip.ironfurnaces.heaterY',
    'tooltip.ironfurnaces.heaterZ',
    'mob_grinding_utils.jei.any_experience',
}


def apply_to_jar(path):
    path = pathlib.Path(path)
    tmp = path.with_suffix('.tmp.jar')
    touched = []
    with zipfile.ZipFile(path) as zin, zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename.endswith(('ru_ru.json', 'en_us.json')):
                d = json.loads(data.decode('utf-8'))
                changed = False
                for k, v in OVERRIDES.items():
                    if k in d and d[k] != v:
                        d[k] = v
                        changed = True
                        touched.append(k)
                if changed:
                    data = json.dumps(d, ensure_ascii=False, indent=2).encode('utf-8')
            zout.writestr(item, data)
    shutil.move(tmp, path)
    return touched


_FMT_ONLY = re.compile(
    r"^[\s§&%\d.,:;\-–—|/\\*+\w\[\]{}()<>!'\"«»…×x=]*$")


_ROMAN = re.compile(r'^[IVXLCDM]+$')


def _is_format_only(v):
    """Строка без единой буквы (кроме служебных) — переводить нечего."""
    if _ROMAN.match(v.strip()):
        return True
    core = re.sub(r'§[0-9a-fk-or]', '', v)
    # сначала вырезаем сами плейсхолдеры целиком, иначе «%s» даст букву «s»
    core = re.sub(r'%(\(\d+\))?[-#+ 0]*[0-9.*]*[a-zA-Z]', '', core)
    core = re.sub(r'\$\{\d+[^}]*\}|\$\d+|\{\d+[^}]*\}', '', core)
    core = re.sub(r'[^\w\s]', '', core, flags=re.UNICODE)
    return not re.search(r'[A-Za-zА-Яа-яЁё]', core)


_TECH = re.compile(
    r'^(?:[A-Za-z] ?[A-Za-z]|[a-z]{1,2}|\d+ ?x|\d+ x %s|A Z|Z a|F/B:|L/R:|U/D:|'
    r'BL|UI|NBT|TEST|OP/Admin Tab|ID: %s|iconIds\[\d\]|%d mB|▶ %d x %s)$',
    re.I)
_OBF = re.compile(r'§k[0-9a-f]', re.I)
_NBT_KEY = re.compile(r'^§7 ?(?:BlockPos|BlockState) ?: ?$', re.I)


def _is_technical(v):
    """Единицы, оси, NBT-ключи, идентификаторы, §k-обфускация — не переводятся."""
    if _OBF.search(v) or _NBT_KEY.match(v.strip()):
        return True
    return bool(_TECH.match(v.strip()))


def audit(path):
    left = []
    with zipfile.ZipFile(path) as z:
        for n in z.namelist():
            if not n.endswith('ru_ru.json'):
                continue
            d = json.loads(z.read(n).decode('utf-8'))
            for k, v in d.items():
                if isinstance(v, str) and v.strip() and not CYR.search(v) \
                        and k not in INTENTIONAL and not _is_format_only(v) and not _is_technical(v):
                    left.append((k, v))
    return left


if __name__ == '__main__':
    out = pathlib.Path('/home/user/work/out')
    total = 0
    for jar in sorted(out.glob('*.jar')):
        t = apply_to_jar(jar)
        if t:
            total += len(t)
            print(f'  ✅ {jar.name}: {len(t)} ключей')
    print(f'\nприменено оверрайдов: {total}\n')
    bad = 0
    for jar in sorted(out.glob('*.jar')):
        for k, v in audit(jar):
            bad += 1
            print(f'  ⚠️  {jar.name}: {k!r} = {v!r}')
    print(f'непереведённых и не намеренных: {bad}')
    sys.exit(1 if bad else 0)
