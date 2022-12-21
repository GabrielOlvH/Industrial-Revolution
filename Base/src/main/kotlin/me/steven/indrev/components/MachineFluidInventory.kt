package me.steven.indrev.components

import me.steven.indrev.utils.SidedConfiguration
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

open class MachineFluidInventory(
    override val syncId: Int,
    size: Int,
    capacity: (Int) -> Long,
    canInsert: (slot: Int, mode: SidedConfiguration.Mode, variant: FluidVariant, count: Long) -> Boolean = noSlots(),
    canExtract: (slot: Int, mode: SidedConfiguration.Mode, variant: FluidVariant, count: Long) -> Boolean = noSlots(),
    onChange: () -> Unit,
) : MachineInventory<FluidVariant, MachineFluidInventory.FluidSlot>(size, canInsert, canExtract, onChange, createSlots(size, onChange, capacity)), SyncableObject {

    override var isDirty: Boolean = false

    override fun exists(): Boolean = this != NullFluidInventory

    override fun toPacket(buf: PacketByteBuf) {
        parts.forEach { slot -> slot.toPacket(buf) }
    }

    override fun fromPacket(buf: PacketByteBuf) {
        parts.forEach { slot -> slot.fromPacket(buf) }
    }

    class FluidSlot(private val c: Long, onChange: () -> Unit) : Slot<FluidVariant>(onChange) {

        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()

        override fun getCapacity(variant: FluidVariant): Long = c

        override fun readNbt(nbt: NbtCompound) {
            this.variant = FluidVariant.fromNbt(nbt.getCompound("variant"))
            this.amount = nbt.getLong("amount")
        }

        fun toPacket(buf: PacketByteBuf) {
            variant.toPacket(buf)
            buf.writeLong(amount)
        }

        fun fromPacket(buf: PacketByteBuf) {
            variant = FluidVariant.fromPacket(buf)
            amount = buf.readLong()
        }
    }
}

object NullFluidInventory : MachineFluidInventory(-1, 0, { 0 }, { _, _, _,_ -> false }, { _, _, _, _ -> false }, {})

private fun createSlots(size: Int, onChange: () -> Unit, capacity: (Int) -> Long) = (0 until size).map { MachineFluidInventory.FluidSlot(capacity(it), onChange) }