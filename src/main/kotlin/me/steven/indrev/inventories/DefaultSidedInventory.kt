package me.steven.indrev.inventories

import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class DefaultSidedInventory(amount: Int, val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true }) : BasicInventory(amount), SidedInventory {
    override fun getInvAvailableSlots(var1: Direction?): IntArray? = IntArray(invSize) { i -> i }

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, direction: Direction?): Boolean = isValidInvStack(slot, stack)

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = isValidInvStack(slot, stack)

    override fun isValidInvStack(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack)
}