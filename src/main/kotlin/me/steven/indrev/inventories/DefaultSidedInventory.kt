package me.steven.indrev.inventories

import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class DefaultSidedInventory(amount: Int, private val inputSlots: IntArray, private val outputSlots: IntArray, val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true })
    : BasicInventory(amount), SidedInventory {

    override fun getInvAvailableSlots(var1: Direction?): IntArray? = IntArray(invSize) { i -> i }

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, direction: Direction?): Boolean = outputSlots.contains(slot)

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = inputSlots.contains(slot)

    override fun isValidInvStack(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack)
}