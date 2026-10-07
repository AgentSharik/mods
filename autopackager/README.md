# Auto-Packager for Functional Storage

A standalone NeoForge 1.21.1 add-on that provides an Auto-Packager block. It does not modify or replace Functional Storage. The packager exposes standard NeoForge item and FE capabilities so item and energy pipes can automate it; Functional Storage can be used alongside it through compatible inventory/pipe setups.

## Features

- Uses the supplied Auto-Packager front and side textures.
- Reproduces the Auto-Packager modes: hybrid 2×2 then 3×3, reverse hybrid 3×3 then 2×2, 2×2 only, 3×3 only, hollow 3×3 (eight matching ingredients), and 1×1 only.
- Like the original, each operation uses one item type and crafts one recipe result. The hollow mode is the eight-ingredient pattern, not eight independent output operations.
- Hold an empty hand and sneak-right-click the block to cycle modes; ordinary empty-hand right-click displays the current mode.
- Takes pipe input from the left side relative to the block's front. The right side is output-only; other sides also accept item input. FE can be supplied through the NeoForge energy capability.
- Uses 1,000 FE per successful operation, with the original default 10-tick working interval and 200-tick idle retry interval.

## Recipe

The recipe follows the supplied Auto-Packager recipe, with the requested changes: the center crafting table is replaced by the vanilla Crafter, and the optional gold power coil is replaced by redstone.

```text
Iron Ingot | Piston   | Iron Ingot
Piston     | Crafter  | Piston
Iron Ingot | Redstone | Iron Ingot
```

## Installation

1. Use Minecraft 1.21.1 with NeoForge 21.1.256 or newer in the 21.1 line.
2. Put `autopackager-1.0.0.jar` in the instance's `mods` folder with Functional Storage and any desired item/energy pipe mod.

Functional Storage is declared as an optional mod dependency; the add-on does not bundle or alter its JAR.

## Build

From this directory, run `./gradlew build` with Java 21. The repository workflow also builds the JAR on pushes affecting this project and uploads it as the `autopackager-neoforge-1.21.1` workflow artifact.
