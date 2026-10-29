#!/usr/bin/env python3
"""Собирает и статически проверяет отдельный NeoForge-фикс IPN pick-block.

JDK и внешние Python-пакеты не нужны. Два небольших класса создаются напрямую
в формате class file; рядом в проекте лежит их читаемый эквивалент на Java.

  python3 scripts/build_ipn_pick_block_fix.py
  python3 scripts/build_ipn_pick_block_fix.py --check
"""
from __future__ import annotations

import argparse
import io
import json
import struct
import tomllib
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROJECT = ROOT / "Проекты" / "Исправления" / "IPN Pick Block Fix"
RESOURCES = PROJECT / "src" / "main" / "resources"
JAR_PATH = PROJECT / "IPN-PickBlock-Fix-neoforge-1.21.1-1.0.0.jar"
MOD_ID = "ipnpickblockfix"
TARGET = "org.anti_ad.mc.ipnext.event.LockedSlotKeeper"
TARGET_METHOD = "ignoredSelectedHotbarSlot(I)V"
CALLBACK = "org/spongepowered/asm/mixin/injection/callback/CallbackInfo"
MIXIN_CLASS = "com/agentsharik/ipnpickblockfix/LockedSlotKeeperMixin"
MOD_CLASS = "com/agentsharik/ipnpickblockfix/IpnPickBlockFixMod"


def u1(value: int) -> bytes:
    return struct.pack(">B", value)


def u2(value: int) -> bytes:
    return struct.pack(">H", value)


def u4(value: int) -> bytes:
    return struct.pack(">I", value)


class ConstantPool:
    def __init__(self) -> None:
        self.entries: list[tuple] = []
        self.cache: dict[tuple, int] = {}

    def add(self, key: tuple, data: bytes) -> int:
        if key in self.cache:
            return self.cache[key]
        self.entries.append((key, data))
        index = len(self.entries)
        self.cache[key] = index
        return index

    def utf8(self, value: str) -> int:
        raw = value.encode("utf-8")
        return self.add(("utf8", value), u1(1) + u2(len(raw)) + raw)

    def integer(self, value: int) -> int:
        return self.add(("integer", value), u1(3) + u4(value))

    def cls(self, name: str) -> int:
        return self.add(("class", name), u1(7) + u2(self.utf8(name)))

    def name_type(self, name: str, descriptor: str) -> int:
        return self.add(
            ("name_type", name, descriptor),
            u1(12) + u2(self.utf8(name)) + u2(self.utf8(descriptor)),
        )

    def methodref(self, owner: str, name: str, descriptor: str) -> int:
        return self.add(
            ("methodref", owner, name, descriptor),
            u1(10) + u2(self.cls(owner)) + u2(self.name_type(name, descriptor)),
        )

    def resolve(self, index: int) -> tuple:
        return self.entries[index - 1][0]

    def value(self, index: int):
        kind, *values = self.resolve(index)
        if kind in {"utf8", "integer"}:
            return values[0]
        if kind == "class":
            return self.value(struct.unpack(">H", self.entries[index - 1][1][2:])[0])
        raise ValueError(f"Нельзя получить простое значение CP {kind}")

    def method(self, index: int) -> tuple[str, str, str]:
        kind, owner, name, descriptor = self.resolve(index)
        assert kind == "methodref"
        return owner, name, descriptor

    def serialize(self) -> bytes:
        return u2(len(self.entries) + 1) + b"".join(data for _, data in self.entries)


class ClassBuilder:
    def __init__(self, cp: ConstantPool, name: str, access: int, source: str) -> None:
        self.cp = cp
        self.this = cp.cls(name)
        self.super = cp.cls("java/lang/Object")
        self.access = access
        self.source = source
        self.methods: list[tuple[int, int, int, list[tuple[int, bytes]]]] = []
        self.annotations: list[bytes] = []

    def attribute(self, name: str, data: bytes) -> tuple[int, bytes]:
        return self.cp.utf8(name), data

    def add_method(
        self,
        name: str,
        descriptor: str,
        access: int,
        code: bytes,
        max_stack: int,
        max_locals: int,
        annotations: list[bytes] | None = None,
    ) -> None:
        code_data = (
            u2(max_stack)
            + u2(max_locals)
            + u4(len(code))
            + code
            + u2(0)  # exception_table_length
            + u2(0)  # Code attributes_count
        )
        attributes = [self.attribute("Code", code_data)]
        if annotations:
            attributes.append(
                self.attribute("RuntimeVisibleAnnotations", u2(len(annotations)) + b"".join(annotations))
            )
        self.methods.append((access, self.cp.utf8(name), self.cp.utf8(descriptor), attributes))

    def add_annotation(self, annotation: bytes) -> None:
        self.annotations.append(annotation)

    @staticmethod
    def serialize_attributes(attributes: list[tuple[int, bytes]]) -> bytes:
        return u2(len(attributes)) + b"".join(
            u2(name) + u4(len(data)) + data for name, data in attributes
        )

    def build(self) -> bytes:
        methods = b"".join(
            u2(access) + u2(name) + u2(descriptor) + self.serialize_attributes(attributes)
            for access, name, descriptor, attributes in self.methods
        )
        class_attributes = [self.attribute("SourceFile", u2(self.cp.utf8(self.source)))]
        if self.annotations:
            class_attributes.append(
                self.attribute(
                    "RuntimeVisibleAnnotations",
                    u2(len(self.annotations)) + b"".join(self.annotations),
                )
            )
        # После этой точки ConstantPool больше не пополняется.
        constant_pool = self.cp.serialize()
        return (
            u4(0xCAFEBABE)
            + u2(0)
            + u2(65)  # Java 21
            + constant_pool
            + u2(self.access)
            + u2(self.this)
            + u2(self.super)
            + u2(0)  # interfaces_count
            + u2(0)  # fields_count
            + u2(len(self.methods))
            + methods
            + self.serialize_attributes(class_attributes)
        )


class Annotations:
    def __init__(self, cp: ConstantPool) -> None:
        self.cp = cp

    def string(self, value: str) -> bytes:
        return b"s" + u2(self.cp.utf8(value))

    def boolean(self, value: bool) -> bytes:
        return b"Z" + u2(self.cp.integer(int(value)))

    def array(self, values: list[bytes]) -> bytes:
        return b"[" + u2(len(values)) + b"".join(values)

    def annotation(self, descriptor: str, pairs: list[tuple[str, bytes]]) -> bytes:
        return (
            u2(self.cp.utf8(descriptor))
            + u2(len(pairs))
            + b"".join(u2(self.cp.utf8(name)) + value for name, value in pairs)
        )

    def nested(self, descriptor: str, pairs: list[tuple[str, bytes]]) -> bytes:
        return b"@" + self.annotation(descriptor, pairs)


def add_constructor(builder: ClassBuilder) -> None:
    ref = builder.cp.methodref("java/lang/Object", "<init>", "()V")
    builder.add_method("<init>", "()V", 0x0001, b"\x2a\xb7" + u2(ref) + b"\xb1", 1, 1)


def build_mixin_class() -> bytes:
    cp = ConstantPool()
    annotations = Annotations(cp)
    builder = ClassBuilder(cp, MIXIN_CLASS, 0x0021, "LockedSlotKeeperMixin.java")
    add_constructor(builder)

    cancel = cp.methodref(CALLBACK, "cancel", "()V")
    inject = annotations.annotation(
        "Lorg/spongepowered/asm/mixin/injection/Inject;",
        [
            ("method", annotations.array([annotations.string(TARGET_METHOD)])),
            (
                "at",
                annotations.nested(
                    "Lorg/spongepowered/asm/mixin/injection/At;",
                    [("value", annotations.string("HEAD"))],
                ),
            ),
            ("cancellable", annotations.boolean(True)),
            ("remap", annotations.boolean(False)),
        ],
    )
    builder.add_method(
        "ipnPickBlockFix$cancel",
        f"(IL{CALLBACK};)V",
        0x0002,
        b"\x2c\xb6" + u2(cancel) + b"\xb1",
        1,
        3,
        [inject],
    )
    builder.add_annotation(
        annotations.annotation(
            "Lorg/spongepowered/asm/mixin/Mixin;",
            [
                ("targets", annotations.array([annotations.string(TARGET)])),
                ("remap", annotations.boolean(False)),
            ],
        )
    )
    return builder.build()


def build_mod_class() -> bytes:
    cp = ConstantPool()
    annotations = Annotations(cp)
    builder = ClassBuilder(cp, MOD_CLASS, 0x0031, "IpnPickBlockFixMod.java")
    add_constructor(builder)
    builder.add_annotation(
        annotations.annotation(
            "Lnet/neoforged/fml/common/Mod;",
            [("value", annotations.string(MOD_ID))],
        )
    )
    return builder.build()


class Reader:
    def __init__(self, data: bytes) -> None:
        self.data = data
        self.pos = 0

    def take(self, size: int) -> bytes:
        result = self.data[self.pos : self.pos + size]
        assert len(result) == size, "Неожиданный конец class file"
        self.pos += size
        return result

    def u1(self) -> int:
        return self.take(1)[0]

    def u2(self) -> int:
        return struct.unpack(">H", self.take(2))[0]

    def u4(self) -> int:
        return struct.unpack(">I", self.take(4))[0]


class ParsedClass:
    def __init__(self, data: bytes) -> None:
        reader = Reader(data)
        assert reader.u4() == 0xCAFEBABE
        assert reader.u2() == 0
        self.major = reader.u2()
        count = reader.u2()
        self.cp = ConstantPool()
        for _ in range(count - 1):
            tag = reader.u1()
            if tag == 1:
                raw = reader.take(reader.u2())
                key = ("utf8", raw.decode("utf-8"))
                encoded = u1(tag) + u2(len(raw)) + raw
            elif tag == 3:
                raw = reader.take(4)
                key = ("integer", struct.unpack(">I", raw)[0])
                encoded = u1(tag) + raw
            elif tag == 7:
                index = reader.u2()
                key = ("class_ref", index)
                encoded = u1(tag) + u2(index)
            elif tag == 10:
                class_index, name_type = reader.u2(), reader.u2()
                key = ("methodref_ref", class_index, name_type)
                encoded = u1(tag) + u2(class_index) + u2(name_type)
            elif tag == 12:
                name, descriptor = reader.u2(), reader.u2()
                key = ("name_type_ref", name, descriptor)
                encoded = u1(tag) + u2(name) + u2(descriptor)
            else:
                raise AssertionError(f"Неожиданный CP tag {tag} в созданном классе")
            self.cp.entries.append((key, encoded))
        self.access = reader.u2()
        self.this = reader.u2()
        self.super = reader.u2()
        for _ in range(reader.u2()):
            reader.u2()
        for _ in range(reader.u2()):
            reader.take(6)
            self.read_attributes(reader)
        self.methods = {}
        for _ in range(reader.u2()):
            access, name, descriptor = reader.u2(), self.utf8(reader.u2()), self.utf8(reader.u2())
            attributes = self.read_attributes(reader)
            self.methods[(name, descriptor)] = (access, attributes)
        self.attributes = self.read_attributes(reader)
        assert reader.pos == len(data), "Лишние байты после class file"

    def utf8(self, index: int) -> str:
        kind, value = self.cp.resolve(index)
        assert kind == "utf8"
        return value

    def class_name(self, index: int) -> str:
        kind, name_index = self.cp.resolve(index)
        assert kind == "class_ref"
        return self.utf8(name_index)

    def methodref(self, index: int) -> tuple[str, str, str]:
        kind, class_index, name_type_index = self.cp.resolve(index)
        assert kind == "methodref_ref"
        kind2, name_index, descriptor_index = self.cp.resolve(name_type_index)
        assert kind2 == "name_type_ref"
        return self.class_name(class_index), self.utf8(name_index), self.utf8(descriptor_index)

    @staticmethod
    def read_attributes(reader: Reader) -> dict[str, bytes]:
        result = {}
        for _ in range(reader.u2()):
            name_index, size = reader.u2(), reader.u4()
            result[name_index] = reader.take(size)
        # Индексы заменяются именами вызывающим кодом при необходимости.
        return result

    def named_attributes(self, attributes: dict[int, bytes]) -> dict[str, bytes]:
        return {self.utf8(index): data for index, data in attributes.items()}

    def annotations(self, attributes: dict[int, bytes]) -> list[tuple[str, dict]]:
        named = self.named_attributes(attributes)
        if "RuntimeVisibleAnnotations" not in named:
            return []
        reader = Reader(named["RuntimeVisibleAnnotations"])
        result = [self.read_annotation(reader) for _ in range(reader.u2())]
        assert reader.pos == len(reader.data)
        return result

    def read_annotation(self, reader: Reader) -> tuple[str, dict]:
        descriptor = self.utf8(reader.u2())
        pairs = {}
        for _ in range(reader.u2()):
            name = self.utf8(reader.u2())
            pairs[name] = self.read_element(reader)
        return descriptor, pairs

    def read_element(self, reader: Reader):
        tag = chr(reader.u1())
        if tag == "s":
            return self.utf8(reader.u2())
        if tag == "Z":
            kind, value = self.cp.resolve(reader.u2())
            assert kind == "integer"
            return bool(value)
        if tag == "[":
            return [self.read_element(reader) for _ in range(reader.u2())]
        if tag == "@":
            return self.read_annotation(reader)
        raise AssertionError(f"Неожиданный annotation element tag {tag}")

    def code(self, attributes: dict[int, bytes]) -> tuple[int, int, bytes]:
        data = self.named_attributes(attributes)["Code"]
        reader = Reader(data)
        max_stack, max_locals, size = reader.u2(), reader.u2(), reader.u4()
        code = reader.take(size)
        for _ in range(reader.u2()):
            reader.take(8)
        self.read_attributes(reader)
        assert reader.pos == len(data)
        return max_stack, max_locals, code


def verify_classes(mixin_data: bytes, mod_data: bytes) -> None:
    mixin = ParsedClass(mixin_data)
    assert mixin.major == 65
    assert mixin.class_name(mixin.this) == MIXIN_CLASS
    class_annotations = mixin.annotations(mixin.attributes)
    assert len(class_annotations) == 1
    descriptor, values = class_annotations[0]
    assert descriptor == "Lorg/spongepowered/asm/mixin/Mixin;"
    assert values == {"targets": [TARGET], "remap": False}

    handler = mixin.methods[("ipnPickBlockFix$cancel", f"(IL{CALLBACK};)V")]
    method_annotations = mixin.annotations(handler[1])
    assert len(method_annotations) == 1
    descriptor, values = method_annotations[0]
    assert descriptor == "Lorg/spongepowered/asm/mixin/injection/Inject;"
    assert values["method"] == [TARGET_METHOD]
    assert values["at"] == ("Lorg/spongepowered/asm/mixin/injection/At;", {"value": "HEAD"})
    assert values["cancellable"] is True and values["remap"] is False
    max_stack, max_locals, code = mixin.code(handler[1])
    assert (max_stack, max_locals) == (1, 3)
    assert code[:2] == b"\x2c\xb6" and code[-1:] == b"\xb1" and len(code) == 5
    assert mixin.methodref(struct.unpack(">H", code[2:4])[0]) == (CALLBACK, "cancel", "()V")

    mod = ParsedClass(mod_data)
    assert mod.major == 65 and mod.class_name(mod.this) == MOD_CLASS
    assert mod.annotations(mod.attributes) == [
        ("Lnet/neoforged/fml/common/Mod;", {"value": MOD_ID})
    ]


def manifest() -> bytes:
    return (
        "Manifest-Version: 1.0\r\n"
        "Created-By: build_ipn_pick_block_fix.py\r\n"
        "Implementation-Title: IPN Pick Block Fix\r\n"
        "Implementation-Version: 1.0.0\r\n"
        "\r\n"
    ).encode("utf-8")


def jar_entries() -> dict[str, bytes]:
    return {
        "META-INF/MANIFEST.MF": manifest(),
        "META-INF/neoforge.mods.toml": (RESOURCES / "META-INF" / "neoforge.mods.toml").read_bytes(),
        "ipnpickblockfix.mixins.json": (RESOURCES / "ipnpickblockfix.mixins.json").read_bytes(),
        f"{MIXIN_CLASS}.class": build_mixin_class(),
        f"{MOD_CLASS}.class": build_mod_class(),
        "LICENSE": (PROJECT / "LICENSE").read_bytes(),
    }


def build_jar(target: io.BufferedIOBase | io.BytesIO) -> None:
    entries = jar_entries()
    with zipfile.ZipFile(target, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as jar:
        for name in sorted(entries):
            info = zipfile.ZipInfo(name, (2026, 10, 29, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.create_system = 3
            info.external_attr = 0o100644 << 16
            jar.writestr(info, entries[name])


def verify_jar(data: bytes) -> None:
    with zipfile.ZipFile(io.BytesIO(data)) as jar:
        assert jar.testzip() is None
        assert set(jar.namelist()) == set(jar_entries())
        metadata = tomllib.loads(jar.read("META-INF/neoforge.mods.toml").decode("utf-8"))
        assert metadata["mods"] == [
            {
                "modId": MOD_ID,
                "version": "1.0.0",
                "displayName": "IPN Pick Block Fix",
                "authors": "StoneBlock 4 RU",
                "displayTest": "IGNORE_SERVER_VERSION",
                "description": metadata["mods"][0]["description"],
            }
        ]
        assert metadata["mixins"] == [{"config": "ipnpickblockfix.mixins.json"}]
        dependencies = metadata["dependencies"][MOD_ID]
        assert any(
            dep["modId"] == "inventoryprofilesnext"
            and dep["versionRange"] == "[2.2.5]"
            and dep["type"] == "required"
            and dep["side"] == "CLIENT"
            for dep in dependencies
        )
        mixins = json.loads(jar.read("ipnpickblockfix.mixins.json"))
        assert mixins["required"] is True
        assert mixins["client"] == ["LockedSlotKeeperMixin"]
        assert mixins["injectors"]["defaultRequire"] == 1
        verify_classes(jar.read(f"{MIXIN_CLASS}.class"), jar.read(f"{MOD_CLASS}.class"))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=JAR_PATH)
    parser.add_argument("--check", action="store_true", help="сверить существующий JAR с воспроизводимой сборкой")
    args = parser.parse_args()

    buffer = io.BytesIO()
    build_jar(buffer)
    expected = buffer.getvalue()
    verify_jar(expected)
    if args.check:
        actual = args.output.read_bytes()
        verify_jar(actual)
        assert actual == expected, f"JAR отличается от воспроизводимой сборки: {args.output}"
        print(f"OK: {args.output} воспроизводим; class file, Mixin, TOML, JSON и ZIP проверены")
        return
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(expected)
    print(f"Собран и проверен {args.output} ({len(expected)} байт)")


if __name__ == "__main__":
    main()
