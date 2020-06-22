package me.steven.indrev.inventories

import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class DefaultSidedInventory(
    size: Int,
    val inputSlots: IntArray,
    val outputSlots: IntArray,
    val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true }
) : SimpleInventory(size), SidedInventory {

    override fun getAvailableSlots(var1: Direction?): IntArray? = IntArray(size()) { i -> i }

    override fun canExtract(slot: Int, stack: ItemStack?, direction: Direction?): Boolean = outputSlots.contains(slot)

    override fun canInsert(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = inputSlots.contains(slot)

    override fun isValid(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack) || stack?.isEmpty == true

    fun getInputInventory() = SimpleInventory(*inputSlots.map { getStack(it) }.toTypedArray())

    fun getOutputInventory() = SimpleInventory(*outputSlots.map { getStack(it) }.toTypedArray())

    override fun addStack(itemStack: ItemStack?): ItemStack {
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
        for (i in 0 until size()) {
            val itemStack = getStack(i)
            if (itemStack.isEmpty && isValid(i, itemStack) && outputSlots.contains(i)) {
                setStack(i, stack.copy())
                stack.count = 0
                return
            }
        }
    }

    private fun addToExistingSlot(stack: ItemStack) {
        for (i in 0 until size()) {
            val itemStack = getStack(i)
            if (ItemStack.areItemsEqualIgnoreDamage(itemStack, stack)) {
                transfer(stack, itemStack)
                if (stack.isEmpty) {
                    return
                }
            }
        }
    }

    private fun transfer(source: ItemStack, target: ItemStack) {
        val i = this.maxCountPerStack.coerceAtMost(target.maxCount)
        val j = source.count.coerceAtMost(i - target.count)
        if (j > 0) {
            target.increment(j)
            source.decrement(j)
            markDirty()
        }
    }
}