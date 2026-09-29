# -*- coding: utf-8 -*-
"""Вшивает переводы из class_ru.CLASS в .class внутри JAR."""
import glob, os, shutil, sys, zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import class_rw
from class_ru import CLASS

if __name__ == '__main__':
    total = 0
    for p in sorted(glob.glob('out_party1/*.jar') + glob.glob('out_party3/*.jar')):
        jar = os.path.basename(p)
        zin = zipfile.ZipFile(p)
        hits, files = 0, 0
        items = []
        for info in zin.infolist():
            data = zin.read(info.filename)
            if info.filename.endswith('.class'):
                new, ch = class_rw.patch_class(data, CLASS)
                if ch:
                    data, hits, files = new, hits + ch, files + 1
            items.append((info, data))
        if not hits:
            continue
        tmp = p + '.tmp'
        with zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as zo:
            for info, data in items:
                zi = zipfile.ZipInfo(info.filename, date_time=info.date_time)
                zi.compress_type = zipfile.ZIP_DEFLATED
                zi.external_attr = info.external_attr
                zo.writestr(zi, data)
        zin.close()
        os.replace(tmp, p)
        total += hits
        print(f'{jar:46s} заменено строк {hits:3d} в {files} классах')
    print('всего замен:', total)
