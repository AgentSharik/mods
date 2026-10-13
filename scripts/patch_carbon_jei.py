#!/usr/bin/env python3
"""
Патч CarbonConfig (плагин JEI): локализация экрана настроек JEI.

Зачем: Carbon Config рисует экран настроек JEI через свой плагин
(carbonconfiglib/plugins/jei). Подписи настроек берутся из JEI
(entry.getLocalizedName() — настоящие ключи перевода, ок), но:
  * имена категорий строятся IConfigNode.createLabel(category.getName()) —
    литерал из сырого имени ("Ingredient List" из "ingredientList");
  * значения enum — Suggestion.value(serialize(enum)) — литерал имени
    константы, показывается через Component.literal(suggestion.getName()).

Патч (только байткод, без JDK):
  * JEIHelpers: добавлены два статических метода:
      localizeEnumSuggestions(List, Component, DataType) — для enum
        подменяет имя показа подсказки на перевод по ключу
        <ключ настройки>.<enum_lowercase> (Component.translatableWithFallback,
        фолбэк — сырое сериализованное значение), value не трогает;
      jeiCategoryLabel(String) — пробует ключи jei.config.client./colors./
        debug./modIdFormat. + имя, иначе прежний createLabel.
  * JEIValue.getSuggestions / JEIArray.getSuggestions — вызов помощника.
  * JEINode.getName — вызов помощника категорий.

Использование: patch_carbon_jei.py <input.jar> <output_dir>
В output_dir складываются пропатченные .class (пути как в jar).
"""
import struct
import sys
import zipfile
import os

# ---------------------------------------------------------------- class file

class CPEntry:
    __slots__ = ("tag", "data")
    def __init__(self, tag, data):
        self.tag = tag
        self.data = data  # payload bytes (без тега)
    def raw(self):
        return bytes([self.tag]) + self.data
    def key(self):
        return self.raw()

class ClassFile:
    def __init__(self, data: bytes):
        self.minor, self.major = struct.unpack(">HH", data[4:8])
        self.cp = [None]  # 1-based
        i = 8
        count = struct.unpack(">H", data[i:i+2])[0]; i += 2
        n = 1
        while n < count:
            tag = data[i]; i += 1
            if tag == 1:
                ln = struct.unpack(">H", data[i:i+2])[0]
                payload = data[i:i+2+ln]; i += 2 + ln
            elif tag in (3, 4):
                payload = data[i:i+4]; i += 4
            elif tag in (5, 6):
                payload = data[i:i+8]; i += 8
            elif tag == 7:
                payload = data[i:i+2]; i += 2
            elif tag == 8:
                payload = data[i:i+2]; i += 2
            elif tag in (9, 10, 11, 12, 17, 18):
                payload = data[i:i+4]; i += 4
            elif tag == 15:
                payload = data[i:i+3]; i += 3
            elif tag == 16:
                payload = data[i:i+2]; i += 2
            elif tag in (19, 20):
                payload = data[i:i+2]; i += 2
            else:
                raise ValueError(f"unknown cp tag {tag}")
            self.cp.append(CPEntry(tag, payload))
            if tag in (5, 6):
                self.cp.append(None)
            n += 2 if tag in (5, 6) else 1
        self.cp_count = count
        self.rest_after_cp = i
        # хвост файла храним как есть; методы/поля парсим лениво поиском ниже
        self.tail = data[i:]
        self._parse_members()

    # --- постоянные обращения к пулу (разрешение по старым индексам) ---
    def utf(self, idx):
        e = self.cp[idx]
        assert e is not None and e.tag == 1, f"#{idx} not utf8"
        ln = struct.unpack(">H", e.data[:2])[0]
        return e.data[2:2+ln].decode("utf-8")

    def _add_raw(self, entry: CPEntry) -> int:
        raw = entry.key()
        for idx in range(1, len(self.cp)):
            e = self.cp[idx]
            if e is not None and e.key() == raw:
                return idx
        self.cp.append(entry)
        self.cp_count += 1
        return len(self.cp) - 1

    def add_utf8(self, s: str) -> int:
        b = s.encode("utf-8")
        return self._add_raw(CPEntry(1, struct.pack(">H", len(b)) + b))

    def add_class(self, internal: str) -> int:
        return self._add_raw(CPEntry(7, struct.pack(">H", self.add_utf8(internal))))

    def add_string(self, s: str) -> int:
        return self._add_raw(CPEntry(8, struct.pack(">H", self.add_utf8(s))))

    def _add_ref(self, tag: int, cls_internal: str, name: str, desc: str) -> int:
        nt = self._add_raw(CPEntry(12, struct.pack(">HH", self.add_utf8(name), self.add_utf8(desc))))
        return self._add_raw(CPEntry(tag, struct.pack(">HH", self.add_class(cls_internal), nt)))

    def add_fieldref(self, cls, name, desc):
        return self._add_ref(9, cls, name, desc)

    def add_methodref(self, cls, name, desc):
        return self._add_ref(10, cls, name, desc)

    def add_imethodref(self, cls, name, desc):
        return self._add_ref(11, cls, name, desc)

    # --- поля и методы (сырые блоки атрибутов сохраняем) ---
    def _parse_members(self):
        data = self.tail
        i = 0
        self.access, self.this_idx, self.super_idx = struct.unpack(">HHH", data[i:i+6]); i += 6
        icount = struct.unpack(">H", data[i:i+2])[0]; i += 2
        self.interfaces = [struct.unpack(">H", data[i+2*k:i+2*k+2])[0] for k in range(icount)]
        i += 2 * icount
        def read_members(i):
            count = struct.unpack(">H", data[i:i+2])[0]; i += 2
            members = []
            for _ in range(count):
                acc, ni, di = struct.unpack(">HHH", data[i:i+6]); i += 6
                acount = struct.unpack(">H", data[i:i+2])[0]; i += 2
                attrs = []
                for _ in range(acount):
                    an = struct.unpack(">H", data[i:i+2])[0]
                    ln = struct.unpack(">I", data[i+2:i+6])[0]
                    attrs.append((an, data[i+6:i+6+ln]))
                    i += 6 + ln
                members.append({"access": acc, "name": ni, "desc": di, "attrs": attrs})
            return members, i
        self.fields, i = read_members(i)
        self.methods, i = read_members(i)
        acount = struct.unpack(">H", data[i:i+2])[0]; i += 2
        self.class_attrs = []
        for _ in range(acount):
            an = struct.unpack(">H", data[i:i+2])[0]
            ln = struct.unpack(">I", data[i+2:i+6])[0]
            self.class_attrs.append((an, data[i+6:i+6+ln]))
            i += 6 + ln
        assert i == len(data), "trailing bytes"

    def find_method(self, name: str, desc: str):
        for m in self.methods:
            if self.utf(m["name"]) == name and self.utf(m["desc"]) == desc:
                return m
        raise KeyError(f"method {name}{desc} not found")

    def set_method_code(self, method, code: bytes, max_stack: int, max_locals: int,
                        stackmap: bytes | None):
        code_name = self.add_utf8("Code")
        body = struct.pack(">HHI", max_stack, max_locals, len(code)) + code
        body += struct.pack(">H", 0)  # exception table
        if stackmap is not None:
            sm_name = self.add_utf8("StackMapTable")
            attrs = [(code_name, None)]  # placeholder
            sm = struct.pack(">H", sm_name) + struct.pack(">I", len(stackmap)) + stackmap
            body += struct.pack(">H", 1) + sm
        else:
            body += struct.pack(">H", 0)
        method["attrs"] = [(code_name, body)]

    def add_method(self, access: int, name: str, desc: str, code: bytes,
                   max_stack: int, max_locals: int, stackmap: bytes | None):
        m = {"access": access, "name": self.add_utf8(name),
             "desc": self.add_utf8(desc), "attrs": []}
        self.methods.append(m)
        self.set_method_code(m, code, max_stack, max_locals, stackmap)

    def serialize(self) -> bytes:
        out = [b"\xca\xfe\xba\xbe", struct.pack(">HHH", self.minor, self.major, self.cp_count)]
        for e in self.cp[1:]:
            if e is not None:
                out.append(e.raw())
        out.append(struct.pack(">HHH", self.access, self.this_idx, self.super_idx))
        out.append(struct.pack(">H", len(self.interfaces)))
        for x in self.interfaces:
            out.append(struct.pack(">H", x))
        def write_members(members):
            b = [struct.pack(">H", len(members))]
            for m in members:
                b.append(struct.pack(">HHHH", m["access"], m["name"], m["desc"], len(m["attrs"])))
                for an, payload in m["attrs"]:
                    b.append(struct.pack(">HI", an, len(payload)) + payload)
            return b"".join(b)
        out.append(write_members(self.fields))
        out.append(write_members(self.methods))
        out.append(struct.pack(">H", len(self.class_attrs)))
        for an, payload in self.class_attrs:
            out.append(struct.pack(">HI", an, len(payload)) + payload)
        return b"".join(out)

# ---------------------------------------------------------------- assembler

class Asm:
    """Крошечный ассемблер: список инструкций с метками, ветвления s2."""
    def __init__(self):
        self.items = []  # ("label", name) | ("bytes", bytearray, branch_label|None)

    def label(self, name):
        self.items.append(("label", name))
        return self

    def b(self, *bs):
        self.items.append(("bytes", bytearray(bs), None))
        return self

    def branch(self, opcode, label):
        self.items.append(("bytes", bytearray([opcode, 0, 0]), label))
        return self

    def build(self):
        offsets = {}
        pc = 0
        for kind, *rest in self.items:
            if kind == "label":
                offsets[rest[0]] = pc
            else:
                pc += len(rest[0])
        out = bytearray()
        pc = 0
        for kind, *rest in self.items:
            if kind == "label":
                continue
            data, label = rest
            if label is not None:
                rel = offsets[label] - pc
                data[1:3] = struct.pack(">h", rel)
            out += data
            pc += len(data)
        return bytes(out), offsets

# opcode helpers ------------------------------------------------------------
def aload(a, n):  return a.b(0x2A + n) if n < 4 else a.b(0x19, n)
def astore(a, n): return a.b(0x4B + n) if n < 4 else a.b(0x3A, n)
def u2(a, opcode, idx): return a.b(opcode, (idx >> 8) & 0xFF, idx & 0xFF)
def ldc(a, idx):
    return a.b(0x12, idx) if idx <= 255 else a.b(0x13, (idx >> 8) & 0xFF, idx & 0xFF)

# verification types для StackMapTable
def vref(cf, internal): return b"\x07" + struct.pack(">H", cf.add_class(internal))
VINT = b"\x01"

def frame_same(delta):
    if delta <= 63:
        return bytes([delta])
    return b"\xfb" + struct.pack(">H", delta)

def frame_full(delta, locals_types, stack_types=()):
    b = b"\xff" + struct.pack(">H", delta) + struct.pack(">H", len(locals_types))
    b += b"".join(locals_types)
    b += struct.pack(">H", len(stack_types)) + b"".join(stack_types)
    return b

def frame_append(delta, added_types):
    # append_frame: tag = 251 + k, k = 1..3
    k = len(added_types)
    assert 1 <= k <= 3
    return bytes([251 + k]) + struct.pack(">H", delta) + b"".join(added_types)

def frame_chop(delta, k):
    assert 1 <= k <= 3
    return bytes([251 - k]) + struct.pack(">H", delta)

def stackmap(frames):  # frames: list[(offset, encoded_bytes_without_delta_fixed)]
    return struct.pack(">H", len(frames)) + b"".join(frames)

# ---------------------------------------------------------------- patches

LIST = "java/util/List"
COMP = "net/minecraft/network/chat/Component"
DTYPE = "carbonconfiglib/gui/api/types/DataType"
SUGG = "carbonconfiglib/api/ISuggestionProvider$Suggestion"
JEIH = "carbonconfiglib/plugins/jei/configs/JEIHelpers"

def patch_helpers(cf: ClassFile):
    """JEIHelpers: + localizeEnumSuggestions, + jeiCategoryLabel."""
    # ---- localizeEnumSuggestions(List, Component, DataType) -> List ----
    enum_f = cf.add_fieldref(DTYPE, "ENUM", f"L{DTYPE};")
    getcontents = cf.add_imethodref(COMP, "getContents", "()Lnet/minecraft/network/chat/ComponentContents;")
    tc_cls = cf.add_class("net/minecraft/network/chat/contents/TranslatableContents")
    getkey = cf.add_methodref("net/minecraft/network/chat/contents/TranslatableContents",
                              "getKey", "()Ljava/lang/String;")
    al_cls = cf.add_class("java/util/ArrayList")
    al_init = cf.add_methodref("java/util/ArrayList", "<init>", "(I)V")
    list_size = cf.add_imethodref(LIST, "size", "()I")
    list_iter = cf.add_imethodref(LIST, "iterator", "()Ljava/util/Iterator;")
    list_add = cf.add_imethodref(LIST, "add", "(Ljava/lang/Object;)Z")
    it_hasnext = cf.add_imethodref("java/util/Iterator", "hasNext", "()Z")
    it_next = cf.add_imethodref("java/util/Iterator", "next", "()Ljava/lang/Object;")
    sugg_cls = cf.add_class(SUGG)
    sugg_getvalue = cf.add_methodref(SUGG, "getValue", "()Ljava/lang/String;")
    sugg_gettype = cf.add_methodref(SUGG, "getType", "()Ljava/lang/Object;")
    sugg_named = cf.add_methodref(SUGG, "namedTypeValue",
                                  "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)L" + SUGG + ";")
    sb_cls = cf.add_class("java/lang/StringBuilder")
    sb_init = cf.add_methodref("java/lang/StringBuilder", "<init>", "()V")
    sb_append = cf.add_methodref("java/lang/StringBuilder", "append",
                                 "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
    sb_tostr = cf.add_methodref("java/lang/StringBuilder", "toString", "()Ljava/lang/String;")
    dot = cf.add_string(".")
    locale_root = cf.add_fieldref("java/util/Locale", "ROOT", "Ljava/util/Locale;")
    tolower = cf.add_methodref("java/lang/String", "toLowerCase", "(Ljava/util/Locale;)Ljava/lang/String;")
    twf = cf.add_imethodref(COMP, "translatableWithFallback",
                            "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    getstr = cf.add_imethodref(COMP, "getString", "()Ljava/lang/String;")

    a = Asm()
    aload(a, 2); u2(a, 0xB2, enum_f); a.branch(0xA6, "Lret")          # if type != ENUM -> return
    aload(a, 1); u2(a, 0xB9, getcontents); a.b(1, 0)                   # name.getContents()
    astore(a, 3)
    aload(a, 3); u2(a, 0xC1, tc_cls); a.branch(0x99, "Lret")           # instanceof
    aload(a, 3); u2(a, 0xC0, tc_cls); u2(a, 0xB6, getkey); astore(a, 4)
    u2(a, 0xBB, al_cls); a.b(0x59); aload(a, 0); u2(a, 0xB9, list_size); a.b(1, 0)
    u2(a, 0xB7, al_init); astore(a, 5)                                  # out = new ArrayList(size)
    aload(a, 0); u2(a, 0xB9, list_iter); a.b(1, 0); astore(a, 6)        # iter
    a.label("Lloop")
    aload(a, 6); u2(a, 0xB9, it_hasnext); a.b(1, 0); a.branch(0x99, "Ldone")
    aload(a, 6); u2(a, 0xB9, it_next); a.b(1, 0); u2(a, 0xC0, sugg_cls); astore(a, 7)
    aload(a, 5)                                                         # out
    u2(a, 0xBB, sb_cls); a.b(0x59); u2(a, 0xB7, sb_init)
    aload(a, 4); u2(a, 0xB6, sb_append)                                 # base
    ldc(a, dot); u2(a, 0xB6, sb_append)                                 # "."
    aload(a, 7); u2(a, 0xB6, sugg_getvalue)
    u2(a, 0xB2, locale_root); u2(a, 0xB6, tolower)
    u2(a, 0xB6, sb_append); u2(a, 0xB6, sb_tostr)                       # key
    aload(a, 7); u2(a, 0xB6, sugg_getvalue)                             # fallback = value
    u2(a, 0xB8, twf); u2(a, 0xB9, getstr); a.b(1, 0)                    # display
    aload(a, 7); u2(a, 0xB6, sugg_getvalue)                             # value
    aload(a, 7); u2(a, 0xB6, sugg_gettype)                              # type
    u2(a, 0xB8, sugg_named)
    u2(a, 0xB9, list_add); a.b(2, 0)                                    # out.add(...)
    a.b(0x57)                                                           # pop
    a.branch(0xA7, "Lloop")
    a.label("Ldone")
    aload(a, 5); a.b(0xB0)
    a.label("Lret")
    aload(a, 0); a.b(0xB0)
    code, off = a.build()

    t_list, t_comp, t_dt = vref(cf, LIST), vref(cf, COMP), vref(cf, DTYPE)
    t_contents = vref(cf, "net/minecraft/network/chat/ComponentContents")
    t_str, t_iter = vref(cf, "java/lang/String"), vref(cf, "java/util/Iterator")
    loop_locals = [t_list, t_comp, t_dt, t_contents, t_str, t_list, t_iter]
    frames = [
        frame_full(off["Lloop"], loop_locals),
        frame_same(off["Ldone"] - off["Lloop"] - 1) if off["Ldone"] - off["Lloop"] - 1 <= 63
        else b"\xfb" + struct.pack(">H", off["Ldone"] - off["Lloop"] - 1),
        frame_full(off["Lret"] - off["Ldone"] - 1, [t_list, t_comp, t_dt]),
    ]
    cf.add_method(0x0009, "localizeEnumSuggestions",
                  f"(L{LIST};L{COMP};L{DTYPE};)L{LIST};",
                  code, 4, 8, stackmap(frames))

    # ---- jeiCategoryLabel(String) -> Component ----
    i18n_exists = cf.add_methodref("net/minecraft/client/resources/language/I18n",
                                   "exists", "(Ljava/lang/String;)Z")
    translatable = cf.add_imethodref(COMP, "translatable",
                                     "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    createlabel = cf.add_imethodref("carbonconfiglib/gui/api/node/IConfigNode",
                                    "createLabel", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    str_cls = cf.add_class("java/lang/String")
    prefixes = ["jei.config.client.", "jei.config.colors.", "jei.config.debug.",
                "jei.config.modIdFormat."]
    a = Asm()
    a.b(0x07)  # iconst_4
    u2(a, 0xBD, str_cls)                                                # anewarray String
    for idx, p in enumerate(prefixes):
        a.b(0x59); a.b(0x03 + idx); ldc(a, cf.add_string(p)); a.b(0x53)
    astore(a, 1)
    a.b(0x03); a.b(0x3D)                                                # i = 0 -> istore_2
    a.label("Lloop")
    a.b(0x1C); aload(a, 1); a.b(0xBE); a.branch(0xA4, "Lfallback")      # if i >= len -> fallback
    aload(a, 1); a.b(0x1C); a.b(0x32); astore(a, 3)                     # prefix = arr[i]
    u2(a, 0xBB, sb_cls); a.b(0x59); u2(a, 0xB7, sb_init)
    aload(a, 3); u2(a, 0xB6, sb_append)
    aload(a, 0); u2(a, 0xB6, sb_append); u2(a, 0xB6, sb_tostr)
    astore(a, 4)                                                        # key
    aload(a, 4); u2(a, 0xB8, i18n_exists); a.branch(0x99, "Lnext")
    aload(a, 4); u2(a, 0xB8, translatable); a.b(0xB0)
    a.label("Lnext")
    a.b(0x84, 2, 1)                                                     # iinc 2, 1
    a.branch(0xA7, "Lloop")
    a.label("Lfallback")
    aload(a, 0); u2(a, 0xB8, createlabel); a.b(0xB0)
    code, off = a.build()

    t_strarr = b"\x07" + struct.pack(">H", cf.add_class("[Ljava/lang/String;"))
    frames = [
        frame_append(off["Lloop"], [t_strarr, VINT]),
        frame_append(off["Lnext"] - off["Lloop"] - 1, [t_str, t_str]),
        frame_chop(off["Lfallback"] - off["Lnext"] - 1, 2),
    ]
    cf.add_method(0x0009, "jeiCategoryLabel", f"(Ljava/lang/String;)L{COMP};",
                  code, 4, 5, stackmap(frames))

def patch_value_class(cf: ClassFile, internal: str):
    """getSuggestions() -> JEIHelpers.localizeEnumSuggestions(suggestions.get(), name, type)."""
    sugg_f = None; name_f = None; type_f = None
    # ищем fieldref'ы по разрешённым именам
    def resolve_ref(idx):
        e = cf.cp[idx]
        cls_idx, nt_idx = struct.unpack(">HH", e.data)
        cls_name = cf.utf(struct.unpack(">H", cf.cp[cls_idx].data)[0])
        nt = cf.cp[nt_idx]
        nm, ds = struct.unpack(">HH", nt.data)
        return cls_name, cf.utf(nm), cf.utf(ds)
    for i in range(1, len(cf.cp)):
        e = cf.cp[i]
        if e is not None and e.tag == 9:
            cls_name, nm, ds = resolve_ref(i)
            if cls_name == internal:
                if nm == "suggestions": sugg_f = i
                elif nm == "name": name_f = i
                elif nm == "type": type_f = i
    assert sugg_f and name_f and type_f, "fields not found"
    supplier_get = None
    for i in range(1, len(cf.cp)):
        e = cf.cp[i]
        if e is not None and e.tag == 11:
            cls_name, nm, ds = resolve_ref(i)
            if cls_name == "java/util/function/Supplier" and nm == "get":
                supplier_get = i
    assert supplier_get
    list_cls = cf.add_class(LIST)
    helper = cf.add_methodref(JEIH, "localizeEnumSuggestions",
                              f"(L{LIST};L{COMP};L{DTYPE};)L{LIST};")
    a = Asm()
    aload(a, 0); u2(a, 0xB4, sugg_f)
    u2(a, 0xB9, supplier_get); a.b(1, 0)
    u2(a, 0xC0, list_cls)
    aload(a, 0); u2(a, 0xB4, name_f)
    aload(a, 0); u2(a, 0xB4, type_f)
    u2(a, 0xB8, helper)
    a.b(0xB0)
    code, _ = a.build()
    m = cf.find_method("getSuggestions", f"()L{LIST};")
    cf.set_method_code(m, code, 3, 1, None)

def patch_node_class(cf: ClassFile):
    internal = "carbonconfiglib/plugins/jei/configs/JEINode"
    def resolve_ref(idx):
        e = cf.cp[idx]
        cls_idx, nt_idx = struct.unpack(">HH", e.data)
        cls_name = cf.utf(struct.unpack(">H", cf.cp[cls_idx].data)[0])
        nt = cf.cp[nt_idx]
        nm, ds = struct.unpack(">HH", nt.data)
        return cls_name, cf.utf(nm), cf.utf(ds)
    cat_f = None; cat_getname = None
    for i in range(1, len(cf.cp)):
        e = cf.cp[i]
        if e is None: continue
        if e.tag == 9:
            cls_name, nm, ds = resolve_ref(i)
            if cls_name == internal and nm == "category": cat_f = i
        if e.tag == 11:
            cls_name, nm, ds = resolve_ref(i)
            if cls_name == "mezz/jei/api/runtime/config/IJeiConfigCategory" and nm == "getName":
                cat_getname = i
    assert cat_f and cat_getname
    helper = cf.add_methodref(JEIH, "jeiCategoryLabel", f"(Ljava/lang/String;)L{COMP};")
    a = Asm()
    aload(a, 0); u2(a, 0xB4, cat_f)
    u2(a, 0xB9, cat_getname); a.b(1, 0)
    u2(a, 0xB8, helper)
    a.b(0xB0)
    code, _ = a.build()
    m = cf.find_method("getName", f"()L{COMP};")
    cf.set_method_code(m, code, 1, 1, None)

# ---------------------------------------------------------------- main

TARGETS = {
    "carbonconfiglib/plugins/jei/configs/JEIHelpers.class": "helpers",
    "carbonconfiglib/plugins/jei/configs/JEIValue.class": "value:carbonconfiglib/plugins/jei/configs/JEIValue",
    "carbonconfiglib/plugins/jei/configs/JEIArray.class": "value:carbonconfiglib/plugins/jei/configs/JEIArray",
    "carbonconfiglib/plugins/jei/configs/JEINode.class": "node",
}

def main():
    src_jar, out_dir = sys.argv[1], sys.argv[2]
    os.makedirs(out_dir, exist_ok=True)
    z = zipfile.ZipFile(src_jar)
    for name, kind in TARGETS.items():
        cf = ClassFile(z.read(name))
        if kind == "helpers":
            patch_helpers(cf)
        elif kind == "node":
            patch_node_class(cf)
        else:
            patch_value_class(cf, kind.split(":", 1)[1])
        out_path = os.path.join(out_dir, name)
        os.makedirs(os.path.dirname(out_path), exist_ok=True)
        with open(out_path, "wb") as f:
            f.write(cf.serialize())
        print("patched:", name)
    print("OK")

if __name__ == "__main__":
    main()
