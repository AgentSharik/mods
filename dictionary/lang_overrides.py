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
}

# Технические и служебные подписи остаются на английском (правило пользователя).
INTENTIONAL = {
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


def audit(path):
    left = []
    with zipfile.ZipFile(path) as z:
        for n in z.namelist():
            if not n.endswith('ru_ru.json'):
                continue
            d = json.loads(z.read(n).decode('utf-8'))
            for k, v in d.items():
                if isinstance(v, str) and v.strip() and not CYR.search(v) and k not in INTENTIONAL:
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
