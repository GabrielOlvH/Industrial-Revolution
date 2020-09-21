package me.steven.indrev.tools.modular

import net.minecraft.item.ItemStack

interface IRModularItem {
    fun getCompatibleModules(itemStack: ItemStack): Array<Module>
    fun getSlotLimit(): Int
}