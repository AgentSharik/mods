# -*- coding: utf-8 -*-
"""Извлечение и применение переводов страниц Patchouli-книг.

Книги поставляются в assets/<ns>/patchouli_books/<book>/en_us/… — это
игровые страницы, которые читает игрок. Русской версии у модов нет, поэтому
en_us переводится на месте, а служебные поля (icon, recipe, item, category,
parent, id, type) не трогаются.
"""
import glob, json, os, re, zipfile

CY = re.compile(r'[А-Яа-яЁё]')
# поля, которые нельзя переводить
KEEP_KEYS = {'icon', 'recipe', 'item', 'category', 'parent', 'id', 'type', 'advancement',
             'item_id', 'template', 'model', 'texture', 'sound', 'nbt', 'command',
             'page_type', 'flag', 'license', 'author', 'modid', 'book_id', 'filename',
             'translation_key', 'display_name', 'title_page', 'sortnum', 'secret',
             'hide', 'unlock_recipe', 'event_id', 'tooltip', 'position', 'dimension',
             'filter', 'predicate', 'value', 'key', 'tag', 'entity', 'biome'}


def is_text_field(key):
    if key in KEEP_KEYS:
        return False
    if re.fullmatch(r'(x|y|z|page|index|sortnum|min|max|amount|size|count|order)', key, re.I):
        return False
    return key in ('text', 'name', 'description', 'title', 'subtitle', 'header', 'footer',
                   'caption', 'label', 'tooltip_text')


def walk(o, path, out):
    if isinstance(o, dict):
        for k, v in o.items():
            if is_text_field(k) and isinstance(v, str):
                out.append((f'{path}.{k}' if path else k, v))
            else:
                walk(v, f'{path}.{k}' if path else k, out)
    elif isinstance(o, list):
        for i, v in enumerate(o):
            walk(v, f'{path}[{i}]', out)


def collect(jars):
    """Возвращает {jar: [(путь, поле, текст)]} по всем книгам в en_us."""
    res = {}
    for p in sorted(jars):
        z = zipfile.ZipFile(p)
        got = []
        for n in z.namelist():
            if 'patchouli_books/' not in n or not n.endswith('.json'):
                continue
            parts = n.split('patchouli_books/')[1].split('/')
            if len(parts) < 3 or not parts[1].startswith('en_us'):
                continue
            try:
                d = json.loads(z.read(n).decode('utf-8'))
            except Exception:
                continue
            out = []
            walk(d, '', out)
            for field, txt in out:
                if re.search(r'[A-Za-z]{3}', txt) and not CY.search(txt):
                    if re.fullmatch(r'[\w.:/#\-$\(\)\{\} ]+', txt) and len(txt) < 12:
                        continue
                    got.append((n, field, txt))
        if got:
            res[p] = got
    return res


def apply_text(node, field, new):
    """Меняет текст по полному пути field вида 'a.b[0].c'."""
    cur = node
    toks = re.findall(r'[^.\[\]]+', field)
    for t in toks[:-1]:
        cur = cur[int(t)] if isinstance(cur, list) else cur[t]
    last = toks[-1]
    if isinstance(cur, list):
        cur[int(last)] = new
    else:
        cur[last] = new


def apply_jar(path, mapping):
    """mapping: {(путь файла, поле): новый текст}"""
    z = zipfile.ZipFile(path)
    buf = {}
    for n in z.namelist():
        if 'patchouli_books/' in n and n.endswith('.json'):
            buf[n] = z.read(n)
    changed = 0
    for (fname, field), new in mapping.items():
        if fname not in buf:
            continue
        try:
            d = json.loads(buf[fname].decode('utf-8'))
        except Exception:
            continue
        apply_text(d, field, new)
        buf[fname] = json.dumps(d, ensure_ascii=False, indent=2).encode('utf-8')
        changed += 1
    if not changed:
        return 0
    import io
    out = io.BytesIO()
    with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as dst:
        for it in z.infolist():
            dst.writestr(it, buf.get(it.filename, z.read(it.filename)))
    open(path, 'wb').write(out.getvalue())
    return changed


if __name__ == '__main__':
    jars = sorted(glob.glob('out_party1/*.jar') + glob.glob('out_party3/*.jar'))
    res = collect(jars)
    n = 0
    with open('book_pages.txt', 'w', encoding='utf-8') as f:
        for p, items in res.items():
            f.write(f'\n{"="*72}\n### {os.path.basename(p)}  ({len(items)} строк)\n{"="*72}\n')
            for fname, field, txt in items:
                n += 1
                f.write(f'FILE\t{fname}\nFIELD\t{field}\nTEXT\t{txt}\n\n')
    print('книг с английскими страницами:', len(res), '| строк:', n)
    for p, items in res.items():
        print(f'   {os.path.basename(p):46s} {len(items)}')
