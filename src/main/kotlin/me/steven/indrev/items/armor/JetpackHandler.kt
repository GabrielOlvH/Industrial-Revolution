package me.steven.indrev.items.armor

import alexiil.mc.lib.attributes.AttributeProviderItem
import alexiil.mc.lib.attributes.ItemAttributeList
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidInsertable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import alexiil.mc.lib.attributes.misc.AbstractItemBasedAttribute
import alexiil.mc.lib.attributes.misc.LimitedConsumer
import alexiil.mc.lib.attributes.misc.Reference
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.isJumping
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.plus
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack

interface JetpackHandler :  ItemConvertible {

    val fluidFilter: (FluidVariant) -> Boolean
    val limit: Long

    fun isUsable(stack: ItemStack): Boolean = true

    fun tickJetpack(stack: ItemStack, playerEntity: PlayerEntity) {
        if (!playerEntity.isJumping) return
        val max = 0.5 // TODO oxyhydrogen
        val vel = playerEntity.velocity
        if (playerEntity.isFallFlying && useFuel(stack)) {
            val facing = playerEntity.rotationVector
            playerEntity.velocity = vel.multiply(0.3).add(facing.multiply(max))
        } else if (vel.y < max && useFuel(stack)) {
            playerEntity.setVelocity(vel.x, max.coerceAtMost(vel.y + 0.15), vel.z)
        }
    }

    fun getFuelStored(stack: ItemStack): ResourceAmount<FluidVariant> {
        Transaction.openOuter().use { tx ->
            val view = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack))
                ?.exactView(tx, FluidVariant.blank())
                ?: return ResourceAmount(FluidVariant.blank(), 0)
            tx.abort()
            return ResourceAmount(view.resource, view.amount)
        }
    }

    //TODO oxyhydrogen
    private fun getConsumptionRatio(stack: ItemStack) = bucket / (81 / 3)

    private fun useFuel(stack: ItemStack): Boolean {
        val consumption = getConsumptionRatio(stack)
        val storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack))
        Transaction.openOuter().use { tx ->
            val extracted = storage?.extract(FluidVariant.of(IRFluidRegistry.HYDROGEN_STILL), consumption, tx)
            return if (extracted == consumption) {
                tx.commit()
                true
            } else {
                tx.abort()
                false
            }
        }
    }

    class JetpackFluidStorage(val handler: JetpackHandler) : SingleVariantStorage<FluidVariant>() {

        override fun extract(extractedVariant: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long = 0

        override fun getCapacity(variant: FluidVariant?): Long = handler.limit

        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()

    }
}