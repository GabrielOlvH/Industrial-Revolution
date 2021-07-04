package me.steven.indrev.blockentities.storage

import com.google.common.collect.Iterables
import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.asMutableList
import me.steven.indrev.utils.energyOf
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

class ChargePadBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHARGE_PAD_REGISTRY, pos, state), BlockEntityClientSerializable {

    init {
        this.inventoryComponent = inventory(this) {}
    }

    override val maxOutput: Double = 16384.0
    override val maxInput: Double = 16384.0

    val energyIo = ChargePadEnergyIo()

    var hasCollided = false

    private fun getItemEnergyIo() = energyOf(inventoryComponent!!.inventory.getStack(0))

    override fun getEnergyCapacity(): Double = 0.0

    override fun fromClientTag(tag: NbtCompound) {
        inventoryComponent!!.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        inventoryComponent!!.writeNbt(tag)
        return tag
    }

    inner class ChargePadEnergyIo : EnergyIo {
        override fun getEnergy(): Double = getItemEnergyIo()?.energy ?: 0.0

        override fun getEnergyCapacity(): Double = getItemEnergyIo()?.energyCapacity ?: 0.0

        override fun insert(amount: Double, simulation: Simulation): Double {
            val handlers = if (hasCollided) {
                val collidedEntityItems = collectItemIos()
                if (collidedEntityItems.isEmpty()) hasCollided = false
                getItemEnergyIo()?.also { collidedEntityItems.add(it) }
                collidedEntityItems
            } else
                getItemEnergyIo().let { if (it == null) emptyList() else listOf(it) }

            if (handlers.isEmpty()) return amount

            var remainder = amount.coerceAtMost(maxOutput)
            handlers.forEach { handler ->
                if (remainder > 0)
                    remainder -= remainder - handler.insert(remainder, simulation)
                else return 0.0
            }
            return remainder
        }

        override fun extract(maxAmount: Double, simulation: Simulation?): Double = 0.0

        override fun supportsExtraction(): Boolean = false

        override fun supportsInsertion(): Boolean = true

        private fun collectItemIos(): MutableList<EnergyIo> {
            return world!!.getEntitiesByClass(LivingEntity::class.java, Box(pos)) { true }.flatMap { entity ->
                when (entity) {
                    is PlayerEntity -> Iterables.concat(entity.inventory.armor, listOf(entity.mainHandStack, entity.offHandStack))
                    is ArmorStandEntity -> entity.itemsEquipped
                    else -> emptyList()
                }.mapNotNull { stack -> energyOf(stack) }
            }.asMutableList()
        }
    }
}