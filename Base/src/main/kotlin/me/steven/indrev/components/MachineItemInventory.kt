package me.steven.indrev.components

import me.steven.indrev.utils.SidedConfiguration
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

open class MachineItemInventory(
    size: Int,
    canInsert: (slot: Int, mode: SidedConfiguration.Mode, variant: ItemVariant, count: Long) -> Boolean = noSlots(),
    canExtract: (slot: Int, mode: SidedConfiguration.Mode, variant: ItemVariant, count: Long) -> Boolean = noSlots(),
    onChange: () -> Unit,
) : MachineInventory<ItemVariant, MachineItemInventory.ItemSlot>(size, canInsert, canExtract, onChange, createSlots(size, onChange)) {

    override fun exists(): Boolean = this != NullItemInventory

    class ItemSlot(onChange: () -> Unit) : Slot<ItemVariant>(onChange) {

        override fun getBlankVariant(): ItemVariant = ItemVariant.blank()

        fun isOf(item: Item) = variant.isOf(item)

        override fun getCapacity(variant: ItemVariant): Long = variant.item.maxCount.toLong()

        fun set(stack: ItemStack) {
            this.variant = ItemVariant.of(stack)
            this.amount = stack.count.toLong()
        }

        override fun readNbt(nbt: NbtCompound) {
            this.variant = ItemVariant.fromNbt(nbt.getCompound("variant"))
            this.amount = nbt.getLong("amount")
        }
    }
}

object NullItemInventory : MachineItemInventory(0, { _, _, _,_ -> false }, { _, _, _, _ -> false }, {})

private fun createSlots(size: Int, onChange: () -> Unit) = (0 until size).map { MachineItemInventory.ItemSlot(onChange) }