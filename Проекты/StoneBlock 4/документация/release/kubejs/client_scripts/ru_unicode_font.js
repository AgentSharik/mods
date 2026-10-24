// Возврат обычного шрифта после v1.1.27. Имя файла сохранено,
// чтобы при обновлении перезаписать прошлый вариант скрипта.
//
// Файловый API здесь использовать нельзя: KubeJS ограничивает доступ к классам.
// Флаг живёт только в памяти и не создаёт никаких файлов. Обычный шрифт
// восстанавливается при первом входе в мир за текущий запуск игры.
const $Minecraft = Java.loadClass('net.minecraft.client.Minecraft')
let restoreDefaultFont = true

ClientEvents.loggedIn(event => {
  if (!restoreDefaultFont) return
  restoreDefaultFont = false

  const options = $Minecraft.getInstance().options
  const forceUnicode = options.forceUnicodeFont()
  if (!forceUnicode.get()) return

  forceUnicode.set(false)
  options.save()
  console.log('[StoneBlock 4 RU] Восстановлен обычный шрифт Minecraft.')
})
