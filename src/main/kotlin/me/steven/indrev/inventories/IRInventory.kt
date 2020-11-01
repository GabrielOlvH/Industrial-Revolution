package me.steven.indrev.inventories

import me.steven.indrev.components.InventoryComponent
import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class IRInventory(
    size: Int,
    val inputSlots: IntArray,
    val outputSlots: IntArray,
    val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true }
) : SimpleInventory(size), SidedInventory {

    var component: InventoryComponent? = null

    override fun getAvailableSlots(var1: Direction?): IntArray? = IntArray(size()) { i -> i }

    override fun canExtract(slot: Int, stack: ItemStack?, direction: Direction?): Boolean =
        outputSlots.contains(slot) && component?.itemConfig?.get(direction)?.output == true

    override fun canInsert(slot: Int, stack: ItemStack?, direction: Direction?): Boolean =
        inputSlots.contains(slot) && component?.itemConfig?.get(direction)?.input == true

    override fun isValid(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack) || stack?.isEmpty == true

    fun getInputInventory() = SimpleInventory(*inputSlots.map { getStack(it) }.toTypedArray())

    fun getOutputInventory() = SimpleInventory(*outputSlots.map { getStack(it) }.toTypedArray())

    fun fits(stack: Item, outputSlot: Int): Boolean {
        val outStack = getStack(outputSlot)
        if (outStack.isEmpty || (stack == outStack.item && outStack.tag?.isEmpty != false))
            return true
        return false
    }

    fun output(itemStack: ItemStack): Boolean =
        outputSlots.any { slot ->
            val existing = getStack(slot)
            if (existing.isEmpty || (existing.item == itemStack.item && existing.count + itemStack.count < maxCountPerStack)) {
                setStack(slot, ItemStack(itemStack.item, existing.count + itemStack.count))
                true
            } else false
        }

    private fun addToExistingSlot(stack: ItemStack) {
        for (i in 0 until size()) {
            val itemStack = getStack(i)
            if (canCombine(itemStack, stack)) {
                transfer(stack, itemStack)
                if (stack.isEmpty) {
                    return
                }
            }
        }
    }

    private fun canCombine(one: ItemStack, two: ItemStack): Boolean {
        return one.item === two.item && ItemStack.areTagsEqual(one, two)
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

    fun smartOutput(stack: ItemStack): Boolean {
        val itemStack = stack.copy()
        addToExistingSlot(itemStack)
        return if (itemStack.isEmpty) true else output(stack)
    }
}