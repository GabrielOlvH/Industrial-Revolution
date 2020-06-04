package me.steven.indrev.inventories

import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class DefaultSidedInventory(
    size: Int,
    val inputSlots: IntArray,
    val outputSlots: IntArray,
    val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true }
) : BasicInventory(size), SidedInventory {

    override fun getInvAvailableSlots(var1: Direction?): IntArray? = IntArray(invSize) { i -> i }

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, direction: Direction?): Boolean = outputSlots.contains(slot)

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = inputSlots.contains(slot)

    override fun isValidInvStack(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack)

    fun getInputInventory() = BasicInventory(*inputSlots.map { getInvStack(it) }.toTypedArray())

    fun getOutputInventory() = BasicInventory(*outputSlots.map { getInvStack(it) }.toTypedArray())

    override fun add(itemStack: ItemStack?): ItemStack {
        val itemStack2 = itemStack!!.copy()
        addToExistingSlot(itemStack2)
        return if (itemStack2.isEmpty) {
            ItemStack.EMPTY
        } else {
            addToNewSlot(itemStack2)
            if (itemStack2.isEmpty) ItemStack.EMPTY else itemStack2
        }
    }

    private fun addToNewSlot(stack: ItemStack) {
        for (i in 0 until invSize) {
            val itemStack = getInvStack(i)
            if (itemStack.isEmpty && isValidInvStack(i, itemStack)) {
                setInvStack(i, stack.copy())
                stack.count = 0
                return
            }
        }
    }

    private fun addToExistingSlot(stack: ItemStack) {
        for (i in 0 until invSize) {
            val itemStack = getInvStack(i)
            if (ItemStack.areItemsEqualIgnoreDamage(itemStack, stack)) {
                transfer(stack, itemStack)
                if (stack.isEmpty) {
                    return
                }
            }
        }
    }

    private fun transfer(source: ItemStack, target: ItemStack) {
        val i = this.invMaxStackAmount.coerceAtMost(target.maxCount)
        val j = source.count.coerceAtMost(i - target.count)
        if (j > 0) {
            target.increment(j)
            source.decrement(j)
            markDirty()
        }
    }
}