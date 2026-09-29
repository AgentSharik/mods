# -*- coding: utf-8 -*-
"""Перевод встроенной документации мода: *.md и *.mdx.

Такие файлы видны прямо в игре (Oracle Index в SimplySwords, гайдбуки
SimpleTeleporters) и по объёму сопоставимы с lang-файлами.

Правила безопасности — ничего из этого трогать нельзя:
  * YAML-ключи и значения служебных полей: icon:, id=, location=, parent:,
    item_ids:, slots={...}, type:, name:, recipe:, advancements:;
  * блоки кода ``` … ``` и инлайновый `код`;
  * ссылки Markdown: [текст](url) — переводится только текст ссылки;
  * идентификаторы: forge:experience, minecraft:stone, modid:путь;
  * плейсхолдеры %s / {0} / $1 и разметка §.

Переводится только обычный прозаический текст.

Запуск: import doc_tr; doc_tr.translate_doc(text) -> (перевод, статистика)
"""
import re

# --- что переводить вообще -----------------------------------------------
_PROSE_MIN_WORDS = 2
_WORD = re.compile(r'[A-Za-z][A-Za-z\'-]{2,}')
_PROTECT = re.compile(
    r'(?P<code>```.*?```)'          # блок кода
    r'|(?P<inline>`[^`\n]+`)'        # инлайновый код
    r'|(?P<url>https?://\S+)'       # ссылки
    r'|(?P<ids>\b[a-z0-9_.-]+:[a-z0-9_./-]+)'  # resource location
    r'|(?P<ph>\{\d+[^}]*\})'        # плейсхолдеры {0}, {0,choice,...}
    r'|(?P<ph2>\$\{?\d+\}?)'        # $1, ${1}
    r'|(?P<secs>§[0-9a-fk-or])'     # §-коды
    r'|(?P<md>!?\[[^\]\n]*\]\([^)\n]*\))'   # markdown-ссылки
    , re.S | re.M)

# строки, которые почти всегда служебные — не трогаем
_SKIP_LINE = re.compile(
    r'^\s*(?:#{1,6}\s*$|-{3,}\s*$|={3,}\s*$|\*{3,}\s*$|\.{3,}\s*$)')

_YAML_LINE = re.compile(
    r'^\s*[A-Za-z_][\w.\-]*\s*:\s*$'          # ключ без значения
    r'|^\s*(?:icon|id|location|parent|item_ids|slots|type|name|recipe'
    r'|advancements|iconId|itemId|category|args|value|amount|min|max)\s*:')


def split_protected(text):
    """-> (куски: ('t', текст) | ('k', защищённое))"""
    out, pos = [], 0
    for m in _PROTECT.finditer(text):
        if m.start() > pos:
            out.append(('t', text[pos:m.start()]))
        out.append(('k', m.group(0)))
        pos = m.end()
    if pos < len(text):
        out.append(('t', text[pos:]))
    return out


_YAML_RE = _YAML_LINE


def looks_like_prose(s):
    """Похоже ли это на обычный английский текст, а не на код/идентификатор."""
    if not s or len(s.strip()) < 3:
        return False
    if _SKIP_LINE.match(s):
        return False
    if _YAML_LINE.match(s) or _YAML_RE.search(s):
        return False
    words = _WORD.findall(s)
    if len(words) < _PROSE_MIN_WORDS:
        return False
    # доля «латиница» и отсутствие служебных символов
    if s.count('{') or s.count('}') or s.count('=') and '://' in s:
        return False
    return True


def translate_doc(text, translator=None, stats=None):
    """Переводит прозаические куски, оставляя всё служебное как есть.

    translator(строка) -> строка; по умолчанию — None (только подсчёт).
    """
    if translator is None:
        def translator(s):
            return s
    result = []
    for kind, chunk in split_protected(text):
        if kind == 'k':
            result.append(chunk)
            continue
        # кусок режем на строки: переводим только «разговорные»
        for line in chunk.split('\n'):
            if looks_like_prose(line):
                result.append(translator(line))
                if stats is not None:
                    stats['lines'] = stats.get('lines', 0) + 1
            else:
                result.append(line)
    return ''.join(result)


def scan_doc(jar_path):
    """-> список (имя файла, текст) всех .md/.mdx в JAR."""
    import zipfile
    out = []
    with zipfile.ZipFile(jar_path) as z:
        for n in z.namelist():
            if n.lower().endswith(('.md', '.mdx')):
                try:
                    out.append((n, z.read(n).decode('utf-8')))
                except UnicodeDecodeError:
                    try:
                        out.append((n, z.read(n).decode('latin-1')))
                    except Exception:
                        pass
    return out


if __name__ == '__main__':
    import sys, glob, os
    pats = sys.argv[1:] or ['*']
    total = 0
    for pat in pats:
        for p in sorted(glob.glob(f'/home/user/dl/pack/mods/{pat}*.jar')):
            docs = scan_doc(p)
            if docs:
                st = {}
                for n, t in docs:
                    translate_doc(t, stats=st)
                print(f'  {os.path.basename(p)[:46]:46s} файлов {len(docs):3d}  '
                      f'строк прозы {st.get("lines",0):5d}')
                total += len(docs)
    print(f'\nвсего файлов документации: {total}')
