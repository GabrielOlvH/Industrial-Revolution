package me.steven.indrev.inventories

import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class DefaultSidedInventory(amount: Int) : BasicInventory(amount), SidedInventory {
    override fun getInvAvailableSlots(var1: Direction?): IntArray? = IntArray(invSize).mapIndexed { index, _ -> index }.toIntArray()

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, direction: Direction?): Boolean = true

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = dir != Direction.UP
}