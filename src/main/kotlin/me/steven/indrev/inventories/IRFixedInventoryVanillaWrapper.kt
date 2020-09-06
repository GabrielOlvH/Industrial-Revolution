package me.steven.indrev.inventories

import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class IRFixedInventoryVanillaWrapper(private val inventory: IRInventory, private val direction: Direction) : FixedInventoryVanillaWrapper(inventory) {
    override fun canInsert(slot: Int, newStack: ItemStack?): Boolean = inventory.canInsert(slot, newStack, direction)
    override fun canExtract(slot: Int, extractedStack: ItemStack?): Boolean = inventory.canExtract(slot, extractedStack, direction)
}