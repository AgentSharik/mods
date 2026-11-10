// v1.1.37: Исток Ars Nouveau в JEI/EMI как обычная перетаскиваемая запись.
// arseng хранит Исток отдельным AEKey, а не предметом. AE2 уже умеет оборачивать
// любой AEKey в ae2:wrapped_generic_stack; при перетаскивании в шаблон или фильтр
// AE2 разворачивает обёртку обратно в GenericStack(SourceKey.KEY, ...).

function logSourceEntryError(stage, error) {
    if (typeof console !== 'undefined' && console.error) {
        console.error('[StoneBlock 4 RU] ' + stage + ': ' + error)
    }
}

var sourceEntry = null

try {
    var $GenericStack = Java.loadClass('appeng.api.stacks.GenericStack')
    var $SourceKey = Java.loadClass('gripe._90.arseng.me.key.SourceKey')

    // 1000 Source — одна операция SourceKeyType в arseng 2.1.1-beta.
    var sourceStack = $GenericStack.wrapInItemStack($SourceKey.KEY, 1000)

    try {
        sourceEntry = Item.of(sourceStack).name('Исток')
    } catch (error) {
        logSourceEntryError('Не удалось задать имя записи Истока', error)
        sourceEntry = sourceStack
    }

    RecipeViewerEvents.addEntries('item', event => {
        event.add(sourceEntry)
    })
} catch (error) {
    logSourceEntryError('Не удалось добавить Исток в JEI/EMI', error)
}

try {
    RecipeViewerEvents.registerSubtypes('item', event => {
        event.useComponents('ae2:wrapped_generic_stack', 'ae2:wrapped_stack')
    })
} catch (error) {
    logSourceEntryError('Не удалось зарегистрировать подтип обёртки Истока', error)
}
