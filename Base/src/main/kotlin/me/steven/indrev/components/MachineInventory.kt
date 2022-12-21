package me.steven.indrev.components

import me.steven.indrev.utils.SidedConfiguration
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.Direction

abstract class MachineInventory<T : TransferVariant<*>, V: MachineInventory.Slot<T>>(
    val size: Int,
    val canInsert: (slot: Int, mode: SidedConfiguration.Mode, variant: T, count: Long) -> Boolean = noSlots(),
    val canExtract: (slot: Int, mode: SidedConfiguration.Mode, variant: T, count: Long) -> Boolean = noSlots(),
    val onChange: () -> Unit,
    slots: List<V>
) : CombinedStorage<T, V>(slots) {

    val sidedInventories = Array(6) { Sided(Direction.byId(it)) }
    val sidedConfiguration = SidedConfiguration()

    operator fun get(index: Int): V = if (index < size) parts[index] else error("Out of bounds")

    fun getSide(dir: Direction) = sidedInventories[dir.id]

    fun writeNbt(): NbtCompound {
        val inv = NbtList()
        (0 until size).forEach { index ->
            val slotNbt = NbtCompound()
            val slot = get(index)
            if (!slot.isEmpty()) {
                slotNbt.putInt("i", index)
                slotNbt.put("s", slot.writeNbt())
                inv.add(slotNbt)
            }
        }
        val nbt = NbtCompound()
        nbt.put("sideconfig", sidedConfiguration.writeNbt())
        if (!inv.isEmpty())
            nbt.put("stacks", inv)
        return nbt
    }

    fun readNbt(nbt: NbtCompound) {
        sidedConfiguration.readNbt(nbt.getCompound("sideconfig"))
        if (!nbt.contains("stacks", 9)) return
        val listNbt = nbt.getList("stacks", 10)
        listNbt.forEach { element ->
            val slotNbt = element as NbtCompound
            val index = slotNbt.getInt("i")
            parts[index].readNbt(slotNbt.getCompound("s"))
        }

    }

    fun insert(slots: IntArray, resource: T, maxAmount: Long, transaction: TransactionContext): Long {
        StoragePreconditions.notNegative(maxAmount)
        var amount: Long = 0

        for ((index, part) in parts.withIndex()) {
            if (slots.contains(index)) {
                amount += part.insert(resource, maxAmount - amount, transaction)
                if (amount == maxAmount) break
            }
        }

        return amount
    }

    abstract fun exists(): Boolean

    abstract class Slot<T : TransferVariant<*>>(val onChange: () -> Unit) : SingleVariantStorage<T>() {

        fun isEmpty() = isResourceBlank || amount <= 0

        fun decrement(count: Long = 1L) {
            if (amount >= count) {
                amount -= count
                if (amount == 0L) {
                    variant = blankVariant
                }
            }
        }

        override fun onFinalCommit() {
            super.onFinalCommit()
            onChange()
        }

        abstract fun readNbt(nbt: NbtCompound)

        fun writeNbt(): NbtCompound {
            val nbt = NbtCompound()
            nbt.put("variant", variant.toNbt())
            nbt.putLong("amount", amount)
            return nbt
        }
    }

    inner class Sided(private val dir: Direction) : CombinedStorage<T, Storage<T>>(this@MachineInventory.parts.mapIndexed { index, slot ->
        object : SingleSlotStorage<T> {
            override fun extract(resource: T, maxAmount: Long, transaction: TransactionContext?): Long {
                if (canExtract(index, sidedConfiguration.getMode(dir), resource, maxAmount)) {
                    return slot.extract(resource, maxAmount, transaction)
                }
                return 0
            }

            override fun insert(resource: T, maxAmount: Long, transaction: TransactionContext?): Long {
                if (canInsert(index, sidedConfiguration.getMode(dir), resource, maxAmount)) {
                    return slot.insert(resource, maxAmount, transaction)
                }
                return 0
            }

            override fun isResourceBlank(): Boolean = slot.isResourceBlank

            override fun getResource(): T = slot.resource

            override fun getAmount(): Long = slot.getAmount()

            override fun getCapacity(): Long = slot.capacity
        }
    })
}

fun <T : TransferVariant<*>> inSlots(vararg slots: Int, test: (T) -> Boolean): (slot: Int, mode: SidedConfiguration.Mode, variant: T, count: Long) -> Boolean = { slot, mode, variant, _ -> slots.contains(slot) && mode == SidedConfiguration.Mode.INPUT && test(variant) }

fun <T : TransferVariant<*>> inSlots(vararg slots: Int): (slot: Int, mode: SidedConfiguration.Mode, variant: T, count: Long) -> Boolean = { slot, mode, _, _ -> slots.contains(slot) && mode == SidedConfiguration.Mode.INPUT }

fun <T : TransferVariant<*>> outSlots(vararg slots: Int): (slot: Int, mode: SidedConfiguration.Mode, variant: T, count: Long) -> Boolean = { slot, mode, _, _ -> slots.contains(slot) && mode == SidedConfiguration.Mode.OUTPUT }

fun <T : TransferVariant<*>> noSlots(): (slot: Int, mode: SidedConfiguration.Mode, variant: T, count: Long) -> Boolean = { _, _, _, _ -> false }