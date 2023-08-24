package me.steven.indrev.extensions

import me.steven.indrev.utils.ItemStackCallback
import net.minecraft.entity.Entity

interface EntityExtension {
    var `indrev$inventoryRedirect`: ItemStackCallback?
}

var Entity.inventoryRedirect: ItemStackCallback?
    set(value) {
        (this as EntityExtension).`indrev$inventoryRedirect` = value
    }
    get() = (this as EntityExtension).`indrev$inventoryRedirect`

inline fun Entity.redirectDrops(block: () -> Unit, callback: ItemStackCallback) {
    this.inventoryRedirect = callback
    block()
    this.inventoryRedirect = null
}