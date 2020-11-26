package me.steven.indrev.inventories

import me.steven.indrev.components.InventoryComponent
import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class IRInventory(
    dsl: IRInventoryDSL,
    size: Int,
    val inputSlots: IntArray,
    val outputSlots: IntArray,
    val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true }
) : SimpleInventory(size), SidedInventory {

    var component: InventoryComponent? = null

    private var availableSlots = inputSlots.plus(outputSlots)

    init {
        if (dsl.coolerSlot != null) availableSlots = availableSlots.plus(dsl.coolerSlot!!)
        availableSlots = availableSlots.distinct().toIntArray()
    }

    override fun getAvailableSlots(var1: Direction?): IntArray? = availableSlots

    override fun canExtract(slot: Int, stack: ItemStack?, direction: Direction?): Boolean =
        outputSlots.contains(slot) && component?.itemConfig?.get(direction)?.output == true

    override fun canInsert(slot: Int, stack: ItemStack?, direction: Direction?): Boolean =
        inputSlots.contains(slot) && component?.itemConfig?.get(direction)?.input == true && isValid(slot, stack)

    override fun isValid(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack) || stack?.isEmpty == true

    fun getInputInventory() = SimpleInventory(*inputSlots.map { getStack(it) }.toTypedArray())

    fun getOutputInventory() = SimpleInventory(*outputSlots.map { getStack(it) }.toTypedArray())

    fun fits(stack: Item, outputSlot: Int): Boolean {
        val outStack = getStack(outputSlot)
        if (outStack.isEmpty || (stack == outStack.item && outStack.tag?.isEmpty != false))
            return true
        return false
    }

    fun output(stack: ItemStack): Boolean {
        for (i in 0 until size()) {
            val itemStack = getStack(i)
            if (canCombine(itemStack, stack)) {
                transfer(stack, itemStack)
                if (stack.isEmpty) {
                    return true
                }
            }
        }
        addToOutputSlot(stack)
        return true
    }

    private fun canCombine(one: ItemStack, two: ItemStack): Boolean
            = one.item === two.item && ItemStack.areTagsEqual(one, two)

    private fun transfer(source: ItemStack, target: ItemStack) {
        val i = this.maxCountPerStack.coerceAtMost(target.maxCount)
        val j = source.count.coerceAtMost(i - target.count)
        if (j > 0) {
            target.increment(j)
            source.decrement(j)
            markDirty()
        }
    }

    private fun addToOutputSlot(stack: ItemStack) {
        for (i in outputSlots) {
            val itemStack = getStack(i)
            if (itemStack.isEmpty) {
                setStack(i, stack.copy())
                stack.count = 0
                return
            }
        }
    }
}