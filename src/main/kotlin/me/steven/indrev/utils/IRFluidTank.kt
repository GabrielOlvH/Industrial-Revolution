package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.components.DefaultSyncableObject
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.SyncableObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry

class IRFluidTank(val index: Int, val component: () -> FluidComponent) : SingleVariantStorage<FluidVariant>(), SyncableObject by DefaultSyncableObject() {

    val isEmpty: Boolean get() = variant.isBlank || amount == 0L
    val fluidRawId: Int get() = Registry.FLUID.getRawId(variant.fluid)

    override fun getCapacity(variant: FluidVariant): Long = component().limit

    override fun getBlankVariant(): FluidVariant = FluidVariant.blank()

    fun toTag(): NbtCompound {
        val nbt = NbtCompound()
        nbt.put("variant", variant.toNbt())
        nbt.putLong("amt", amount)
        return nbt
    }

    fun render(faces: List<FluidRenderFace?>?, vcp: VertexConsumerProvider?, matrices: MatrixStack?) {
        if (!variant.isBlank)
            FluidKeys.get(variant.fluid).withAmount(FluidAmount.BUCKET).render(faces, vcp, matrices)
    }

    fun renderGuiRect(x0: Double, y0: Double, x1: Double, y1: Double) {
        if (!variant.isBlank)
            FluidKeys.get(variant.fluid).withAmount(FluidAmount.BUCKET).renderGuiRect(x0, y0, x1, y1)
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
            val extracted = extract(variant, amount, it)
            if (act) it.commit() else it.abort()
            return extracted
        }
    }

    fun tryInsert(variant: FluidVariant, amount: Long): Boolean {
        if (!this.variant.isBlank && variant != this.variant) return false
        Transaction.openOuter().use {
            val extracted = extract(variant, amount, it)
            it.abort()
            return extracted == amount
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
        val variant = FluidVariant.fromNbt(nbt.getCompound("variant"))
        val amt = nbt.getLong("amt")

        this.variant = variant
        amount = amt
    }
}