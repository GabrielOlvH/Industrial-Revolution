package me.steven.indrev.utils

import me.steven.indrev.components.DefaultSyncableObject
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.SyncableObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

class IRFluidTank(val index: Int, val component: () -> FluidComponent) : SingleVariantStorage<FluidVariant>(), SyncableObject by DefaultSyncableObject() {

    val isEmpty: Boolean get() = variant.isBlank || amount == 0L
    val exposed = ExposedIRFluidTank()

    override fun getCapacity(variant: FluidVariant): Long = component().getTankCapacity(index)

    override fun getBlankVariant(): FluidVariant = FluidVariant.blank()

    fun toTag(): NbtCompound {
        val nbt = NbtCompound()
        nbt.put("variant", variant.toNbt())
        nbt.putLong("amt", amount)
        return nbt
    }

    /*fun render(faces: List<FluidRenderFace?>?, vcp: VertexConsumerProvider?, matrices: MatrixStack?) {
        if (!variant.isBlank)
            FluidKeys.get(variant.fluid).withAmount(FluidAmount.BUCKET).render(faces, vcp, matrices)
    }*/

    fun renderGuiRect(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        if (!variant.isBlank) {
            //FluidKeys.get(variant.fluid).withAmount(FluidAmount.BUCKET).renderGuiRect(x0, y0, x1, y1)
            renderInGui(matrices, resource, amount, capacity, x, y, width, height)
        }
    }

    override fun onFinalCommit() {
        super.onFinalCommit()
        component().syncable().markForUpdate()
        markDirty()
    }

    fun extract(amount: Long, act: Boolean = false): Long {
        Transaction.openOuter().use {
            val extracted = extract(variant, amount, it)
            if (act) it.commit() else it.abort()
            return extracted
        }
    }

    fun tryExtract(amount: Long): Boolean {
        Transaction.openOuter().use {
            val extracted = extract(variant, amount, it)
            it.abort()
            return extracted == amount
        }
    }

    fun insert(variant: FluidVariant, amount: Long, act: Boolean = false): Long {
        if (!this.variant.isBlank && variant != this.variant) return 0
        Transaction.openOuter().use {
            val inserted = insert(variant, amount, it)
            if (act) it.commit() else it.abort()
            return inserted
        }
    }

    fun tryInsert(variant: FluidVariant, amount: Long): Boolean {
        if (!this.variant.isBlank && variant != this.variant) return false
        Transaction.openOuter().use {
            val inserted = insert(variant, amount, it)
            it.abort()
            return inserted == amount
        }
    }

    override fun toPacket(buf: PacketByteBuf) {
        resource.toPacket(buf)
        buf.writeLong(amount)
    }

    @Environment(EnvType.CLIENT)
    override fun fromPacket(buf: PacketByteBuf) {
        this.variant = FluidVariant.fromPacket(buf)
        this.amount = buf.readLong()
    }

    fun fromTag(nbt: NbtCompound) {
        this.variant = FluidVariant.fromNbt(nbt.getCompound("variant"))
        this.amount = nbt.getLong("amt")
    }

    inner class ExposedIRFluidTank : SnapshotParticipant<ResourceAmount<FluidVariant>>(), SingleSlotStorage<FluidVariant> by this {
        override fun createSnapshot(): ResourceAmount<FluidVariant> = this@IRFluidTank.createSnapshot()

        override fun readSnapshot(snapshot: ResourceAmount<FluidVariant>) = this@IRFluidTank.readSnapshot(snapshot)

        override fun insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
            return if (component().inputTanks.contains(index) && component().isFluidValidForTank(index, resource))
                this@IRFluidTank.insert(resource, maxAmount, transaction)
            else 0
        }

        override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
            return if (component().outputTanks.contains(index))
                this@IRFluidTank.extract(resource, maxAmount, transaction)
            else 0
        }

        override fun onFinalCommit() = this@IRFluidTank.onFinalCommit()
    }
}