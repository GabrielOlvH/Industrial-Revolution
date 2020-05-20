package me.steven.indrev.inventories

import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class DefaultSidedInventory(vararg stacks: ItemStack) : BasicInventory(*stacks), SidedInventory {
    override fun getInvAvailableSlots(var1: Direction?): IntArray? {
        val result = IntArray(invSize)
        for (i in result.indices) {
            result[i] = i
        }
        return result
    }

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, direction: Direction?): Boolean {
        return true
    }

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = dir != Direction.UP

}