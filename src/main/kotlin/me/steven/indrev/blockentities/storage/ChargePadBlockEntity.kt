package me.steven.indrev.blockentities.storage

import com.google.common.collect.Iterables
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.asMutableList
import me.steven.indrev.utils.energyOf
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import team.reborn.energy.api.EnergyStorage

class ChargePadBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHARGE_PAD_REGISTRY, pos, state), BlockEntityClientSerializable {

    init {
        this.inventoryComponent = inventory(this) {
            input { slot = 0 }
            output { slot = 0 }
        }

    }

    override val maxOutput: Long = 16384
    override val maxInput: Long = 16384

    val energyIo = ChargePadEnergyIo()

    var hasCollided = false

    private fun getItemEnergyIo() = energyOf(inventoryComponent!!.inventory, 0)

    override fun getCapacity(): Long = 0

    override fun fromClientTag(tag: NbtCompound) {
        inventoryComponent!!.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        inventoryComponent!!.writeNbt(tag)
        return tag
    }

    inner class ChargePadEnergyIo : EnergyStorage {
        override fun getAmount(): Long = getItemEnergyIo()?.amount ?: 0

        override fun getCapacity(): Long = getItemEnergyIo()?.capacity ?: 0

        override fun insert(amount: Long, transaction: TransactionContext?): Long {
            val handlers = if (hasCollided) {
                val collidedEntityItems = collectItemIos()
                if (collidedEntityItems.isEmpty()) hasCollided = false
                getItemEnergyIo()?.also { collidedEntityItems.add(it) }
                collidedEntityItems
            } else
                getItemEnergyIo().let { if (it == null) emptyList() else listOf(it) }

            if (handlers.isEmpty()) return 0

            val max = amount.coerceAtMost(maxOutput)
            var remainder = max
            handlers.forEach { handler ->
                if (remainder > 0)
                    remainder -= handler.insert(remainder, transaction)
                else return 0
            }
            return max - remainder
        }

        override fun extract(maxAmount: Long, transaction: TransactionContext?): Long = 0

        override fun supportsExtraction(): Boolean = false

        override fun supportsInsertion(): Boolean = true

        private fun collectItemIos(): MutableList<EnergyStorage> {
            return world!!.getEntitiesByClass(LivingEntity::class.java, Box(pos)) { true }.flatMap { entity ->
                when (entity) {
                    is PlayerEntity -> Iterables.concat(entity.inventory.armor.mapIndexed { index, _ ->  energyOf(entity.inventory, 36 + index) },
                        listOf(energyOf(entity.inventory, entity.inventory.selectedSlot), energyOf(entity.inventory, 40)))
                    else -> emptyList()
                }
            }.filterNotNull().asMutableList()
        }
    }
}