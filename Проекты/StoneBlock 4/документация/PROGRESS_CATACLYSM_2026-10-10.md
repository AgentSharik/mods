# Cataclysm (L_Enders Cataclysm 3.33) — журнал перевода и вычитки, 10.10.2026

Мод №2 в порядке работ. Исходник: `L_Ender's Cataclysm 1.21.1-3.33 curseforge.jar`
(73 375 661 байт, SHA-256 `371956d0c2e3b56729ee93b955e936875d210e372fe734cdb74326d89ef79875`).
Результат: `ru_1.21.1/L_Ender's Cataclysm 1.21.1-3.33.jar`, 73 229 820 байт,
SHA-256 `0a9be3915e8bb5c0060046872fdef8bf7fe40bb9cbf6f8ccb1435aba6123802e`.

## 1. Объём

| Показатель | Значение |
|---|---:|
| Ключей в `en_us.json` | **735** |
| Было переведено в `ru_ru.json` | 677 (664 с кириллицей) |
| **Добавлено новых переводов** | **58** |
| **Исправлено при вычитке** | **115** |
| Итого затронуто ключей | 173 из 735 (23,5 %) |
| Локалей в JAR: было → стало | 19 → 2 (`en_us` и `ru_ru`) |

Группы ключей: `item.cataclysm.*` — 236, `block.cataclysm.*` — 133, `entity.cataclysm.*` — 102,
звуковые подписи (`*.sub`) — 159, `advancements.*` — 42, `death.attack.*` — 16, `effect.*` — 15,
`notice.*` — 7, `curios.*` — 8, `attribute.*` — 2, `ability.*` — 2, `key.*` — 5,
`itemGroup.*` — 2, `trim_material.*` — 4, `cataclysm.*` — 2.

Книг и гайдбуков в моде нет: весь текст живет в lang-файле и в коде. Метаданных для перевода нет —
поля `description` в `META-INF/neoforge.mods.toml` у мода отсутствует.

## 2. Схема сборки (как у остальных модов)

- `ru_ru.json` — вычитанный русский текст, порядок ключей приведён к `en_us.json`.
- `en_us.json` — та же русская строка, чтобы русский текст показывался и при английском языке игры.
- Остальные 17 локалей (`es_ar`, `es_cl`, `es_es`, `es_mx`, `fr_ca`, `fr_fr`, `hu_hu`, `it_it`,
  `ja_jp`, `ko_kr`, `pl_pl`, `pt_br`, `tr_tr`, `uk_ua`, `vi_vn`, `zh_cn`, `zh_tw`) удалены.
  Отдельно отмечено: `fr_fr` и `pl_pl` в исходнике не читаются строгим `json.loads` — ещё одна причина
  не тащить их в сборку.
- Все остальные 4 217 записей архива (модели, текстуры, звуки, классы) скопированы байт в байт.

## 3. Добавленные переводы (58 ключей)

### Предметы
- `item.cataclysm.brontes` — **Бронт** (мифический циклоп Бронт; имя оставлено транслитерацией, как
  `Maledictus` → Маледиктус, `Wadjet` → Уаджит).
- `brontes.desc` «You Can throw it» → «Его можно бросить»; `brontes.desc2` → «(К тому же это кирка)».
- `item.cataclysm.onyx_spawn_egg` → «Яйцо призыва Оникса» (по шаблону остальных яиц призыва).
- `item.cataclysm.shift_desc` «Hold [SHIFT] for details» → «Удерживайте [SHIFT] для подробностей»
  (`[SHIFT]` сохранён в оригинале по общему правилу).

### Блоки (12) — пурпур и обсидиан, продолжена существующая схема
`chiseled_purpur_block` = «Резной пурпур», `purpur_wall` = «Пурпурная ограда», поэтому:
`obsidian_fence` → «Обсидиановая ограда», `purpur_tiles` → «Пурпурная плитка»,
`purpur_tile_slab/_pillar/_stairs/_wall` → «Плита / Колонна / Ступеньки / Ограда из пурпурной плитки»,
`void_purpur_tiles` → «Пустотная пурпурная плитка» (по `void_infused_end_stone_bricks` = «Пустотные …»),
`obsidian_pillar` → «Обсидиановая колонна»,
`polished_obsidian_slab/_stairs/_wall` → «Плита / Ступеньки / Ограда из полированного обсидиана»,
`chorus_trapdoor` → «Хорусовый люк» (по `chorus_planks` = «Хорусовые доски»).

### Сущности и эффекты
`entity.cataclysm.old_netherite_monstrosity` → «Старое Незеритовое чудище»,
`entity.cataclysm.onyx` → «Оникс»,
`effect.cataclysm.lightning_hex` → «Молниеносное проклятие»,
`effect.cataclysm.over_gravity` → «Повышенная гравитация»,
`effect.cataclysm.dragon_wound` → «Рана дракона».

### Звуковые подписи (19) — по уже устоявшимся в файле шаблонам
«<Моб> ранен / погибает / рычит / воет / булькает», музыка — «Играет музыкальная тема <босса>»:
`flamethrower.sub` → «Огнемёт», `ignis_shield_break` → «Щит Игниса сломан» (по соседнему
`ignis_armor_break` = «Броня Игниса повреждена»), `abyss_blast_only_charge.sub` →
«Левиафан готовит несколько взрывов бездны», `abyss_blast_only_shoot.sub` →
«Левиафан выпускает взрыв бездны», `parry.sub` → «Щит парирует», `heavy_smash.sub` → «Тяжёлый удар»,
`hippocamtus_idle/_hurt/_death.sub` → «Гиппокамт булькает / ранен / погибает»,
`cindaria_hurt/_death.sub` → «Синдария ранена / погибает»,
`scylla_music.sub` → «Играет музыкальная тема Сциллы», `scylla_music_disc.sub` →
«Играет оригинальная музыкальная тема Сциллы», `scylla_hurt.sub` → «Сцилла ранена»,
`scylla_roar.sub` → «Сцилла воет», `scylla_death.sub` → «Сцилла погибает»,
`super_lightning_strike.sub` → «Удар молнии»,
`the_cataclysmfarer.sub` → «Играет «Странник Катаклизма»».

### Прочее
- `death.attack.cataclysm.storm_bringer` «%s was hunted by %s» → «%s был выслежен %s».
- Curios-слоты по образцу `feet` = «Ноги» / «При ношении на ногах:»:
  `rings` → «Кольца» / «При ношении на кольцах:», `talisman` → «Талисман» / «При ношении талисмана:»,
  `waist` → «Пояс» / «При ношении на поясе:».
- Атрибуты: «% Critical Damage» → «% критического урона», «% Nature Heal» → «% природного исцеления»
  (ведущий `%` сохранён как служебный символ).
- `ability.cataclysm.amethyst_cluster` → «Скопление аметистов» (так же называется и снаряд),
  `.desc` → «Выпускает вокруг вас скопления аметистов, наносящие урон существам.»
- Рекламный блок `notice.*` (7 ключей, ссылка на MakeShip): «Вам интересен плюшевый Игнис из
  Катаклизма?» / «Да» / «Проверить ссылку» / «Перейти на сайт MakeShip» / «Больше не показывать
  объявления» / «Открыть сайт MakeShip» / «Объявления отключены. Их можно снова включить в конфиге.»

## 4. Что было исправлено при вычитке (115 ключей)

### 4.1 Смысл перевёрнут или потерян (самая тяжёлая группа)
| Ключ | Было | Стало | Английский оригинал |
|---|---|---|---|
| `item.cataclysm.infernal_forge` | Незеритовый горн | **Инфернальный горн** | Infernal Forge |
| `hammertime.sub` | Удар незеритового горна | **Удар инфернального горна** | Infernal Forge smashes |
| `item.cataclysm.infernal_forge.desc` | …щелкнуть ПКМ… АоЕ-урон | **Держа в основной руке, нажмите ПКМ по блоку, чтобы нанести урон по площади** | …right-click on a block for AoE damage, Disables shields within 4 blocks… |
| `item.cataclysm.cursium_upgrade.applies_to.desc` | Незеритовая броня | **Незеритовое снаряжение** | Netherite Equipment |
| `item.cataclysm.incinerator.desc` | Если вы прекратите кастовать заклинание через 3 секунды | **Удерживайте ПКМ 3 секунды для зарядки** | Hold right click for 3 seconds to charge |
| `item.cataclysm.incinerator2.desc` | перед вами вырвутся столбы огня | **При отпускании призывает столб огня впереди по направлению взгляда** | On release summons Flame Strike pierce forward |
| `item.cataclysm.incinerator3.desc` | ОСТОРОЖНО: столбы огня могут взорваться… | **ОСТОРОЖНО: при взрыве столба огня предметы могут быть потеряны** | CAUTION: Flame Strike may destroy dropped items |
| `item.cataclysm.bulwark_of_the_flame.desc` | Если вы перестанете его использовать во время приседания… | **При приседании отпустите кнопку, чтобы рвануться вперёд и нанести урон существам перед вами** | While sneaking release the right click to dash forward, dealing damage to entities in front |
| `item.cataclysm.gauntlet_of_bulwark.desc` | После зарядки в течение 1 секунды отбрасывает… | **Удерживайте ПКМ 1 секунду…** | Holding right click for 1 second… |
| `item.cataclysm.gauntlet_of_bulwark.desc2` | Когда вы перестаёте её использовать, она заряжается… | **При отпускании совершает рывок вперёд…** | Dash forward, dealing damage to entities in front when you release |
| `item.cataclysm.cursed_bow2.desc` | Если использовать не обычную стрелу, то выстрелит только две | **Если стрела особая, будет выпущено две** | If the arrow is special, it will fire two |
| `item.cataclysm.monstrous_helm.desc` / `.desc2` | предложение разорвано на две части, вторая без.subject | **два самостоятельных описания** | When worn, if your health drops below 50%, it knocks back nearby entities / and increases defense, knockback resistance and regen |
| `item.cataclysm.tidal_claws.desc` | ПКМ, чтобы использовать как крюк для передвижения, | **Нажмите ПКМ, чтобы выстрелить крюком-кошкой** | Right-click to fire a grappling hook |
| `item.cataclysm.meat_shredder.desc` | Удерживайте ПКМ… | **Нажмите ПКМ…** | Right-click to damage entities in front |
| `entity.cataclysm.ignis.defeat_message` | Мертвецы Незера пробуждаются | **В крепостях Незера пробудились огненные берсерки…** | Within Nether Fortresses, Ignited Beserkers have awakend… |
| `entity.cataclysm.the_leviathan.defeat_message` | Бездна смотрит на вас (хвост отброшен) | **Бездна смотрит на вас, и теперь в океане появятся коралловые големы…** | The Abyss gazes into you, Coral Golems will now appear in the ocean… |
| `entity.cataclysm.wither_smoke_effect` | Отложенное иссушение | **Дым иссушения** | Wither Smoke |
| `entity.cataclysm.abyss_mine` | Глубинная шахта | **Глубинная мина** | Abyss Mine |
| `entity.cataclysm.the_prowler` (+5 подписей) | Провожатый | **Соглядатай** | The Prowler |
| `entity.cataclysm.bolt_strike` | Болтовой Удар | **Удар молнии** | Bolt Strike |
| `entity.cataclysm.lightning_storm` | Молнии | **Шторм молний** | Lightning Storm |
| `entity.cataclysm.ancient_desert_stele` | Древние пустынный столб | **Древняя пустынная стела** | Ancient Desert Stele |
| `entity.cataclysm.cindaria` (+6 ключей) | Эолия | **Синдария** | Cindaria |
| `portal_abyss_blast.sub` | Смертельные лазеры извергаются из разломов | **Взрыв бездны поднимается** | Abyss Blast rises |
| `ignis_poke.sub` | Игнис засовывает врага в испепелитель | **Оружие наносит колющий удар** | Weapon stabs |
| `monstrosityland.sub` | Босс побеждён | **Незеритовое чудище теряет сознание** | Netherite Monstrosity faints |
| `entity.cataclysm.drowned_host` | Утопленник Хост | **Утопленник-хост** | Drowned Host |
| `item.cataclysm.the_immolator` | Жертвенник | **Иммолатор** | The Immolator |
| `item.cataclysm.remnant_skull` | Останки черепа | **Череп Останков** | Remnant Skull |
| `key.cataclysm.chestplate_ability` | Способности **поножи** | **Способности нагрудника** | Chestplate Ability |

### 4.2 Неточные числа и факты
- `cursium_chestplate.desc2`: «до **7** здоровья» → «до **5** здоровья» (EN: to 5 health).
- `final_fractal.desc`: «бонусного урона» → «дополнительного урона» (3 % от макс. HP).
- `zweiender.desc`: «двойной урон» → «**200 %** урона» (EN: 200% damage).
- `monstrous_helm2.desc`: «сопротивление ударам» → «сопротивление отбрасыванию» (knockback resistance).
- `sandstorm_in_a_bottle.desc`: «2 пустынных шторма» → «две песчаные бури, которые будут кружиться
  вокруг вас» (EN: summons 2 sandstorms to orbit you).
- `necklace_of_the_desert.desc`: «оно может что-то разбудить.....» → «этим можно что-то
  разбудить......» (шесть точек как в оригинале).
- `music_disc_the_leviathan.desc`: «Predator of **The** Abyss» → «Predator of the Abyss» (название
  трека приводится к оригиналу).

### 4.3 Орфография, грамматика, опечатки
`annihilator2.desc` «в **друх** руках» → «в двух руках»; `ceraunus2.desc` «в **присяде**» → «в
приседе»; `death.attack.cataclysm.flame_strike` «**поглащён**» → «поглощён»; `emp` «был
**превращен**» → «был превращён»; `block.cataclysm.blackstone_pillar` «Тёмная **коллона**» → «Тёмная
колонна»; `block.cataclysm.door_of_seal_part` «**Часит** Двери Печати» → «Часть Двери Печати»;
`endermaptera_ambient.sub` «Эндер-мафте**ры** визжит» → «Эндер-мафтера визжит»;
`entity.cataclysm.octo_ink` «Окто чернила» → «Окто-чернила»;
`emp_activated.sub` «ЭМИ активирова**лась**» → «ЭМИ активировано»;
`remnant_idle.sub` «Древние Останки **рычат**» → «стонут» (groans);
`remnant_stomp.sub` «Земля измельчается» → «Земля крошится»;
`advancements.cataclysm.kill_leviathan.title` «**Межизмеренченский** хищник» → «Многомерный хищник»
(Multidimensional Predator);
`advancements.cataclysm.kill_remnant.description` — обрезано и «сразите Древнего Останки» → полный
перевод: «Раскопайте подозрительный песок в проклятой пирамиде, чтобы пробудить и сразить Древние
Останки».

### 4.4 Обращение на «вы» и регистр
- `entity.cataclysm.you_cant_escape`: «Тебе не сбежать» → «**Вам** не сбежать».
- Ключи управления: `key.categories.cataclysm` «Cataclysm» → «Катаклизм»;
  `itemGroup.cataclysm.item` / `.block` «Cataclysm Предметы/Блоки» → «Предметы / Блоки Cataclysm»;
  `advancements.cataclysm.root.title` «Cataclysm» → «Катаклизм».
- Регистр: весь блок «лазурного морского камня» (9 ключей) приведён к нижнему регистру — было
  «из Лазурного Морского Камня»; `frosted_stone_brick_stairs` «Лестница из…» → «Ступеньки из…»
  (как у всех прочих ступенек); `phantom_arrow`, `axe_blade`, `maledictus_spear` — лишние прописные
  сняты; `frosted_prison.description` «Найдите Замороженную тюрьму» → «Найдите замороженную тюрьму».
- Материал **Cursium** унифицирован как «проклятый»: броня называлась «Призрачной», а слиток, блок,
  улучшение и ковка — «Проклятыми». Теперь: «Проклятый шлем / нагрудник», «Проклятые поножи /
  ботинки» (4 ключа). Призрачными в моде остаются только эффекты: `ghost_form`, `ghost_sickness`,
  `ghost_vision`, `ghost_dodge`, `ghostly_weightless`.
- `item.cataclysm.wadjet_spawn_egg`: «Яйцо призыва Уаджиты» → «Яйцо призыва Уаджита» (по названию
  сущности «Уаджит»); `koboleton_bone` «Кобольдовая кость» → «Кость кобольда».
- `entity.cataclysm.abyss_portal` — убран висячий пробел в конце строки.

### 4.5 Заголовки достижений, переведённые «отсебятиной» (заменены на перевод английского)
| Ключ | Было | Стало | Оригинал |
|---|---|---|---|
| `root.title` | Cataclysm | Катаклизм | Cataclysm |
| `find_ruined_citadel.title` | Разрушенная цитадель | **Ещё одна крепость?!** | Another Stronghold?! |
| `find_soul_black_smith.title` | Кузня душ | **Там, где куётся чудовище** | Where the Monstrous be Forged |
| `find_soul_black_smith.description` | Получите кузню душ | **Найдите кузню душ** | Find the Soul Forge |
| `ancient_factory.title` | Древняя цивилизация | **Не на своём месте** | Out-of-Place |
| `kill_harbinger.title` | Древняя цивилизация | **Порождение древней цивилизации** | The Thing of Ancient Civilization |
| `kill_clawdian.title` | Это мой морской конёк | **Не такой уж он простой** | Not so Shrimple Now |
| `kill_all_bosses.title` | А конец ли? | **Странник Катаклизма** | The Cataclysmfarer |
| `kill_maledictus.description` | Призовите и убейте Маледиктуса | **Пробудите и сразите Маледиктуса** | Summon and Defeat Maledictus |

«Странник Катаклизма» (The Cataclysmfarer) поддержан и в подписи музыкальной пластинки
`the_cataclysmfarer.sub`.

## 5. Устоявшаяся терминология мода (зафиксирована, дальше не меняется)

| Английский | Русский |
|---|---|
| Purpur | пурпур (прилагательное — пурпурная) |
| Wall / Fence / Slab / Stairs / Pillar | ограда / забор / плита / ступеньки / колонна |
| Chorus (блоки) | хорусовый |
| Polished | полированный |
| End Stone (эндерняк) | эндерняковый |
| Right-click / Left-click | ПКМ / ЛКМ |
| Hold right click / Right-click | удерживайте ПКМ / нажмите ПКМ |
| On release / While sneaking | при отпускании / в приседе |
| Blazing Brand | огненное клеймо |
| Flame Strike | столб огня |
| Abyss (Blast / Mine / Portal) | бездна, но в устойчивых названиях оставлено «глубинный» там, где оно уже стояло |
| Deepling / Draugr / Kobolediator / Wadjet | глубинник / драугр / коболедатор / уаджит |
| Cursium / Ignitium / Enderite / Witherite | проклятый / игнитовый / эндеритовый / визеритовый |
| Shield break / parry | щит сломан / щит парирует |

Спорные места, решённые выбором одного варианта:
- **Infernal Forge** — «Инфернальный горн» (не «кузня» и не «незеритовый»): оружие-кирка уровня
  незерита, падающее с Незеритового чудища; «infernal» = инфернальный.
- **The Immolator** — «Иммолатор» (транслитерация, как «Аннигилятор»), потому что «Испепелитель»
  уже занят предметом The Incinerator, а «Жертвенник» не имеет отношения к оружию.
- **The Prowler** — «Соглядатай» (не «провожатый» и не «хищник»): моб-сталкер с пилой.
- **Cindaria** — «Синдария» (транслитерация; прежняя «Эолия» — название из другого ряда).
- **Drowned Host** — «Утопленник-хост» (ряд: Октохост, Симбиокт; «host» = организм-носитель).

## 6. Протокол проверки и её результат

Проверено скриптом по итоговому JAR, сверка с оригинальным `en_us.json` из CurseForge-архива:

| Проверка | Результат |
|---|---|
| ZIP цел (`testzip`) | ОК |
| Число записей | 4 236 → 4 219 (удалены 17 локалей) |
| Ключей в `en_us` / `ru_ru` | 735 / 735 |
| Паритет ключей | полное совпадение, порядок приведён к `en_us` |
| `en_us == ru_ru` по значениям | да (обе локали русские) |
| Непереведённых ключей | 0 (было 58) |
| Потерянные плейсхолдеры `%s`, `%{n}`, `%d` | нет |
| Служебные коды `§` | сохранены во всех ключах |
| Двойные и висячие пробелы | нет |
| Пустые русские значения при непустом английском | нет |
| Локали в JAR | только `en_us.json` и `ru_ru.json` |

Экранная вычитка GUI/HUD не выполнялась: игра не запускается в этой среде. Все 735 пар
«английский ↔ русский» прочитаны глазами, отдельно прочитан русский текст без английского,
затем выполнена перекрёстная сверка.

## 7. Публикация

| Дата | Что сделано |
|---|---|
| 10.10.2026 | Инвентаризация 735 ключей, найдено 58 непереведённых. Прочитаны все группы: предметы, блоки, сущности, достижения, эффекты, смерти, curios, атрибуты, способности, все звуковые подписи. |
| 10.10.2026 | Добавлены 58 переводов, исправлены 115 ключей. Собрать JAR, техпроверка, коммит и пуш в `arena/1d03e390-mods`. |
