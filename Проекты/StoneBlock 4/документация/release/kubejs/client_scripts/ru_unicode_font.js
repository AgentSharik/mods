// Apotheosis: русский текст может исчезать при использовании стандартного
// растрового шрифта. Подтверждённый workaround — Force Unicode Font:
// https://github.com/Shadows-of-Fire/Apotheosis/issues/1756
// https://github.com/Shadows-of-Fire/Apotheosis/issues/1808
//
// Не заменяем options.txt игрока. Меняем только forceUnicodeFont через
// штатный OptionInstance: Minecraft сам обновляет шрифт и размер интерфейса.
const $Minecraft = Java.loadClass('net.minecraft.client.Minecraft')

ClientEvents.loggedIn(event => {
  const options = $Minecraft.getInstance().options
  if (String(options.languageCode) !== 'ru_ru') return

  const forceUnicode = options.forceUnicodeFont()
  if (forceUnicode.get()) return

  forceUnicode.set(true)
  options.save()
  console.log('[StoneBlock 4 RU] Force Unicode Font включён для русского языка.')
})
