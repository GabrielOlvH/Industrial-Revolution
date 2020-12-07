package me.steven.indrev.blockentities.storage

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class ChargePadBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHARGE_PAD_REGISTRY) {
    init {
        this.inventoryComponent = inventory(this) {}
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        workingState = Energy.valid(stack) && Energy.of(this).into(Energy.of(stack)).move() > 0
    }

    override fun getBaseBuffer(): Double = 1000000.0

    override fun getMaxOutput(side: EnergySide?): Double {
        return 16384.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        return 16384.0
    }
}