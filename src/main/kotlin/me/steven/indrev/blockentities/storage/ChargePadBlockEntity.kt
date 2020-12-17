package me.steven.indrev.blockentities.storage

import dev.technici4n.fasttransferlib.api.ContainerItemContext
import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import dev.technici4n.fasttransferlib.api.item.ItemKey
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier

class ChargePadBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHARGE_PAD_REGISTRY) {
    init {
        this.inventoryComponent = inventory(this) {}
    }

    override val maxOutput: Double = 16384.0
    override val maxInput: Double = 16384.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        val itemIo = EnergyApi.ITEM[ItemKey.of(stack), ContainerItemContext.ofStack(stack)]
        workingState = EnergyMovement.move(this, itemIo, maxOutput) == maxOutput
    }

    override fun getEnergyCapacity(): Double = 1000000.0

    class ChargePadEnergyIo(val blockEntity: ChargePadBlockEntity) : EnergyIo {
        override fun getEnergy(): Double = blockEntity.energy

        override fun getEnergyCapacity(): Double = blockEntity.energyCapacity

        override fun extract(maxAmount: Double, simulation: Simulation?): Double {
            if (!supportsExtraction()) return 0.0
            var extracted = maxAmount.coerceAtMost(blockEntity.maxOutput)
            val overflow = energy - extracted
            if (overflow < 0)
                extracted += overflow

            if (simulation == Simulation.ACT)
                blockEntity.energy -= extracted


            return extracted
        }

        override fun insert(amount: Double, simulation: Simulation?): Double {
            if (!supportsInsertion()) return amount
            var inserted = amount.coerceAtMost(blockEntity.maxInput)
            val overflow = energy + inserted
            if (overflow > energyCapacity)
                inserted -= overflow - energyCapacity

            if (simulation == Simulation.ACT)
                blockEntity.energy += inserted
            return amount - inserted
        }

        override fun supportsExtraction(): Boolean = false

        override fun supportsInsertion(): Boolean = true
    }
}