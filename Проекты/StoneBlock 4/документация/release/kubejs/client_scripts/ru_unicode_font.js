// Миграция с v1.1.27: возвращаем обычный шрифт после неудачного
// автоматического включения Force Unicode Font. Имя файла сохранено,
// чтобы при обновлении перезаписать прошлый вариант скрипта.
//
// Отключение выполняется только один раз. После этого игрок может сам
// включать или выключать Unicode-шрифт — скрипт больше не вмешивается.
const $Minecraft = Java.loadClass('net.minecraft.client.Minecraft')
const $File = Java.loadClass('java.io.File')
const $restoreMarker = new $File('kubejs/data/stoneblock4_default_font_restored.marker')

ClientEvents.loggedIn(event => {
  if ($restoreMarker.exists()) return

  const options = $Minecraft.getInstance().options
  const forceUnicode = options.forceUnicodeFont()
  if (forceUnicode.get()) {
    forceUnicode.set(false)
    options.save()
    console.log('[StoneBlock 4 RU] Восстановлен обычный шрифт Minecraft.')
  }

  const parent = $restoreMarker.getParentFile()
  if (parent !== null) parent.mkdirs()
  $restoreMarker.createNewFile()
})
