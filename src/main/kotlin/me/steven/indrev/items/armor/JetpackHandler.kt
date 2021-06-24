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
import me.steven.indrev.utils.isJumping
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.plus
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack

interface JetpackHandler : AttributeProviderItem, ItemConvertible {

    val fluidFilter: FluidFilter
    val limit: FluidAmount

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

    override fun addAllAttributes(
        stack: Reference<ItemStack>,
        excess: LimitedConsumer<ItemStack>,
        to: ItemAttributeList<*>
    ) {
        if (to.attribute == FluidAttributes.INSERTABLE) {
            to.offer(JetpackFluidInsertable(this, stack, excess))
        }
    }

    fun getFuelStored(stack: ItemStack): FluidVolume {
        val tag = stack.tag ?: return FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)
        return FluidVolume.fromTag(tag.getCompound("Tank"))
    }

    //TODO oxyhydrogen
    private fun getConsumptionRatio(stack: ItemStack) = FluidAmount.BUCKET.div(500)

    private fun useFuel(stack: ItemStack): Boolean {
        val fuel = getFuelStored(stack)
        val consumption = getConsumptionRatio(stack)
        if (fuel.amount() < consumption) return false
        stack.tag?.put("Tank", fuel.fluidKey.withAmount(fuel.amount() - consumption).toTag())
        return true
    }

    private class JetpackFluidInsertable(val handler: JetpackHandler, val ref: Reference<ItemStack>, excess: LimitedConsumer<ItemStack>)
        : AbstractItemBasedAttribute(ref, excess), FluidInsertable {

        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation): FluidVolume {
            if (!handler.fluidFilter.matches(fluid.fluidKey)) return fluid
            var stack = ref.get()
            if (simulation.isSimulate) stack = stack.copy()
            if (stack.isEmpty || !stack.isOf(handler.asItem()))
                return fluid

            val tag = stack.getOrCreateSubTag("Tank") ?: return fluid
            val current = FluidVolume.fromTag(tag)
            val result = FluidVolumeUtil.computeInsertion(current, handler.limit, fluid)
            val actuallyInserted = fluid.amount() - result.result.amount()
            if (actuallyInserted.isZero) return fluid


            val oldStack = stack.copy()
            val newStack = stack
            newStack.orCreateTag.put("Tank", fluid.fluidKey.withAmount(current.amount() + actuallyInserted).toTag())
            return if (setStacks(simulation, oldStack, newStack)) result.result
            else fluid
        }
    }
}