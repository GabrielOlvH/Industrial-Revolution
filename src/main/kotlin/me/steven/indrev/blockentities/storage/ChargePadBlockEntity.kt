package me.steven.indrev.blockentities.storage

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class ChargePadBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHARGE_PAD_REGISTRY, pos, state) {
    init {
        this.inventoryComponent = inventory(this) {}
    }

    override val maxOutput: Double = 16384.0
    override val maxInput: Double = 16384.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        val itemIo = energyOf(stack)
        EnergyMovement.move(this, itemIo ?: return, maxOutput)
    }

    override fun getEnergyCapacity(): Double = 1000000.0

    class ChargePadEnergyIo(val blockEntity: ChargePadBlockEntity) : EnergyIo {
        override fun getEnergy(): Double = blockEntity.energy

        override fun getEnergyCapacity(): Double = blockEntity.energyCapacity

        override fun insert(amount: Double, simulation: Simulation?): Double {
            val inserted = amount.coerceAtMost(blockEntity.maxInput).coerceAtMost(this.energyCapacity - energy)
            if (simulation?.isActing == true) blockEntity.energy += inserted
            return amount - inserted
        }

        override fun extract(maxAmount: Double, simulation: Simulation?): Double {
            val extracted = maxAmount.coerceAtMost(blockEntity.maxOutput).coerceAtMost(energy)
            if (simulation?.isActing == true) blockEntity.energy -= extracted
            return extracted
        }
        override fun supportsExtraction(): Boolean = false

        override fun supportsInsertion(): Boolean = true
    }
}