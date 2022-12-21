package me.steven.indrev.screens.machine

import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.utils.SidedConfiguration
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class MachineSlot(val inv: MachineItemInventory, index: Int, x: Int, y: Int, val filter: ((ItemStack) -> Boolean)?) : Slot(SimpleInventory(0), index, x, y) {

    private var cachedItemStack: ItemStack? = null

    override fun getStack(): ItemStack {
        cachedItemStack = this.inv[index].variant.toStack(this.inv[index].amount.toInt())
        return cachedItemStack!!
    }

    override fun setStack(stack: ItemStack) {
        this.inv[index].variant = ItemVariant.of(stack)
        this.inv[index].amount = stack.count.toLong()
        inv.onChange()
        cachedItemStack = stack
    }

    override fun setStackNoCallbacks(stack: ItemStack) {
        this.inv[index].variant = ItemVariant.of(stack)
        this.inv[index].amount = stack.count.toLong()
    }

    override fun markDirty() {
        if (cachedItemStack != null) {
            stack = cachedItemStack!!
        }
        inv.onChange()
    }

    override fun canInsert(stack: ItemStack): Boolean {
        return filter?.invoke(stack) == true || inv.canInsert(index, SidedConfiguration.Mode.INPUT, ItemVariant.of(stack), stack.count.toLong())
    }

    override fun getMaxItemCount(): Int {
        return inv[index].capacity.toInt()
    }

    override fun takeStack(amount: Int): ItemStack {
        val s = stack.copy()
        val r = s.split(amount)
        stack = s
        cachedItemStack = null
        return r
    }
}