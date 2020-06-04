package me.steven.indrev.inventories

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.InventoryListener
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeFinder
import net.minecraft.util.DefaultedList
import net.minecraft.util.math.Direction

class DefaultSidedInventory(
    private val size: Int,
    val inputSlots: IntArray,
    val outputSlots: IntArray,
    val slotPredicate: (Int, ItemStack?) -> Boolean = { _, _ -> true }
) : SidedInventory {

    var stackList: DefaultedList<ItemStack> = DefaultedList.ofSize(size, ItemStack.EMPTY)

    private val listeners: List<InventoryListener> = listOf()

    override fun getInvStack(slot: Int): ItemStack {
        return if (slot >= 0 && slot < stackList.size) stackList[slot] else ItemStack.EMPTY
    }

    override fun takeInvStack(slot: Int, amount: Int): ItemStack? {
        val itemStack = Inventories.splitStack(stackList, slot, amount)
        if (!itemStack.isEmpty) {
            markDirty()
        }
        return itemStack
    }

    fun poll(item: Item, count: Int): ItemStack? {
        val itemStack = ItemStack(item, 0)
        for (i in this.size - 1 downTo 0) {
            val itemStack2 = getInvStack(i)
            if (itemStack2.item == item) {
                val j = count - itemStack.count
                val itemStack3 = itemStack2.split(j)
                itemStack.increment(itemStack3.count)
                if (itemStack.count == count) {
                    break
                }
            }
        }
        if (!itemStack.isEmpty) {
            markDirty()
        }
        return itemStack
    }

    fun add(itemStack: ItemStack): ItemStack? {
        val itemStack2 = itemStack.copy()
        addToExistingSlot(itemStack2)
        return if (itemStack2.isEmpty) {
            ItemStack.EMPTY
        } else {
            addToNewSlot(itemStack2)
            if (itemStack2.isEmpty) ItemStack.EMPTY else itemStack2
        }
    }

    override fun removeInvStack(slot: Int): ItemStack? {
        val itemStack = stackList[slot]
        return if (itemStack.isEmpty) {
            ItemStack.EMPTY
        } else {
            stackList[slot] = ItemStack.EMPTY
            itemStack
        }
    }

    override fun setInvStack(slot: Int, stack: ItemStack) {
        stackList[slot] = stack
        if (!stack.isEmpty && stack.count > this.invMaxStackAmount) {
            stack.count = this.invMaxStackAmount
        }
        markDirty()
    }

    override fun getInvSize(): Int {
        return this.size
    }

    override fun isInvEmpty(): Boolean {
        val var1: Iterator<*> = stackList.iterator()
        var itemStack: ItemStack
        do {
            if (!var1.hasNext()) {
                return true
            }
            itemStack = var1.next() as ItemStack
        } while (itemStack.isEmpty)
        return false
    }

    override fun markDirty() {
        val var1: Iterator<*> = this.listeners.iterator()
        while (var1.hasNext()) {
            val inventoryListener = var1.next() as InventoryListener
            inventoryListener.onInvChange(this)
        }
    }

    override fun canPlayerUseInv(player: PlayerEntity?): Boolean {
        return true
    }

    override fun clear() {
        stackList.clear()
        markDirty()
    }

    fun provideRecipeInputs(recipeFinder: RecipeFinder) {
        val var2: Iterator<*> = stackList.iterator()
        while (var2.hasNext()) {
            val itemStack = var2.next() as ItemStack
            recipeFinder.addItem(itemStack)
        }
    }


    override fun getInvAvailableSlots(var1: Direction?): IntArray? = IntArray(invSize) { i -> i }

    override fun canExtractInvStack(slot: Int, stack: ItemStack?, direction: Direction?): Boolean = outputSlots.contains(slot)

    override fun canInsertInvStack(slot: Int, stack: ItemStack?, dir: Direction?): Boolean = inputSlots.contains(slot)

    override fun isValidInvStack(slot: Int, stack: ItemStack?): Boolean = slotPredicate(slot, stack)

    fun getInputInventory() = BasicInventory(*inputSlots.map { getInvStack(it) }.toTypedArray())

    fun getOutputInventory() = BasicInventory(*outputSlots.map { getInvStack(it) }.toTypedArray())

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