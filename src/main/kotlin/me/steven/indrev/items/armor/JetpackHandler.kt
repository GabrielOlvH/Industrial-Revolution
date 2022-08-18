package me.steven.indrev.items.armor

import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.transaction
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleTypes

interface JetpackHandler : ItemConvertible {

    val fluidFilter: (FluidVariant) -> Boolean
    val capacity: Long

    fun isUsable(stack: ItemStack): Boolean = true

    fun tickJetpack(stack: ItemStack, playerEntity: PlayerEntity) {
        if (!playerEntity.jumping || !isActive(stack)) return
        val max = 0.5
        val vel = playerEntity.velocity
        if (playerEntity.isFallFlying && useFuel(stack, playerEntity.inventory, 38)) {
            val facing = playerEntity.rotationVector
            playerEntity.velocity = vel.multiply(0.3).add(facing.multiply(max))
        } else if (vel.y < max && useFuel(stack, playerEntity.inventory, 38)) {
            playerEntity.setVelocity(vel.x, max.coerceAtMost(vel.y + 0.15), vel.z)
        } else return
        repeat(6) {
            playerEntity.world.addParticle(
                ParticleTypes.FLAME,
                playerEntity.x + (2 * playerEntity.random.nextGaussian() - 1) * 0.1,
                playerEntity.y,
                playerEntity.z + (2 * playerEntity.random.nextGaussian() - 1) * 0.1,
                0.0,
                -.5,
                0.0
            )
        }
    }

    fun getFuelStored(stack: ItemStack): ResourceAmount<FluidVariant> {
        val nbt = stack.getSubNbt("Fluid") ?: return ResourceAmount(FluidVariant.blank(), 0)
        val variant = FluidVariant.fromNbt(nbt)
        val amt = nbt.getLong("Amount")
        return ResourceAmount(variant, amt)
    }

    private fun getConsumptionRatio(stack: ItemStack) = bucket / 81 / 3

    private fun useFuel(stack: ItemStack, inv: Inventory, slot: Int): Boolean {
        val consumption = getConsumptionRatio(stack)
        val storage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(InventoryStorage.of(inv, null).getSlot(slot)))
        transaction { tx ->
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
    fun isActive(stack: ItemStack) = stack.item is JetpackHandler && stack.orCreateNbt.getBoolean("JetpackActive")

    fun toggle(stack: ItemStack) {
        if (stack.item is JetpackHandler) {
            val tag = stack.orCreateNbt
            tag.putBoolean("JetpackActive", !tag.getBoolean("JetpackActive"))
        }
    }


    class JetpackFluidStorage(val handler: JetpackHandler, ctx: ContainerItemContext) : SingleVariantItemStorage<FluidVariant>(ctx) {

        override fun getCapacity(variant: FluidVariant?): Long = handler.capacity

        override fun getBlankResource(): FluidVariant = FluidVariant.blank()

        override fun getResource(currentVariant: ItemVariant): FluidVariant {
            val compound = currentVariant.nbt?.getCompound("Fluid") ?: return blankResource
            return FluidVariant.fromNbt(compound)
        }

        override fun getAmount(currentVariant: ItemVariant): Long {
            val compound = currentVariant.nbt?.getCompound("Fluid") ?: return 0
            return compound.getLong("Amount")
        }

        override fun getUpdatedVariant(
            currentVariant: ItemVariant,
            newResource: FluidVariant,
            newAmount: Long
        ): ItemVariant {
            var nbt = currentVariant.nbt
            if (newAmount > 0 && !newResource.isBlank) {
                if (nbt == null) nbt = NbtCompound()
                val fluidNbt = newResource.toNbt()
                fluidNbt.putLong("Amount", newAmount)
                nbt.put("Fluid", fluidNbt)
            } else if (nbt != null) {
                nbt.remove("Fluid")
            }
            return ItemVariant.of(handler, nbt)
        }

    }
}