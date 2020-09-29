package me.steven.indrev.inventories

import me.steven.indrev.components.InventoryComponent
import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
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

    fun fits(stack: ItemStack): Boolean {
        for (outputSlot in outputSlots) {
            val outStack = getStack(outputSlot)
            if (outStack.isEmpty || (stack.item == outStack.item && stack.tag == outStack.tag && stack.count + outStack.count < stack.maxCount))
                return true
        }
        return false
    }
}