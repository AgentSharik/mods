# Релиз StoneBlock 4 RU v1.1.0 (2026-10-15)

Опубликован: https://github.com/AgentSharik/mods/releases/tag/StoneBlock4-ru-v1.1.0

## Ассеты
- `StoneBlock.4.zip` (509 МиБ) — 182 переведённых JAR в `mods/` +
  `config/ftbquests/quests/lang/ru_ru.snbt` (квесты FTB 2523/2523) +
  УСТАНОВКА.txt + СОСТАВ_ПЕРЕВОДА.md
- `StoneBlock.4.sha256.txt` — sha256 архива
- `Axiom-6.1.3-for-MC1.21.1.jar` — отдельным файлом (не переводится)

## Отличия от v1.0.0
- 182 JAR вместо 168 (новые моды после снимка v1.0.0)
- добавлен полный перевод квестов FTB (2523/2523, две волны вычитки)
- обновлённые правки модов: AE2, Ars Nouveau, Psi/Oracle Index, CarbonConfig,
  MFFS, CrashUtilities, Chipped и др.

## Доставка
Прямая загрузка ассетов из песочницы недоступна (uploads.github.com), поэтому
сборка и публикация выполняются workflow `.github/workflows/release-stoneblock4.yml`
(запуск — push в ветку `arena/88cc25d2-mods` по путям workflow/УСТАНОВКА.txt;
ассеты перезаливаются с --clobber, черновик публикуется автоматически).
