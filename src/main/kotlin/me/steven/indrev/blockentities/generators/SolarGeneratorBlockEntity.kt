package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.EnergySide

class SolarGeneratorBlockEntity(tier: Tier) :
    GeneratorBlockEntity(tier, MachineRegistry.SOLAR_GENERATOR_REGISTRY) {

    init {
        this.inventoryController = InventoryController {
            DefaultSidedInventory(2, intArrayOf(), intArrayOf()) { _, _ -> true }
        }
        this.temperatureController = TemperatureController({ this }, 0.1, 500..700, 1000.0)
    }

    override fun shouldGenerate(): Boolean = this.world?.isSkyVisible(pos.up()) == true && this.world?.isDay == true

    override fun getMaxOutput(side: EnergySide?): Double = 32.0

    override fun getGenerationRatio(): Double = when (tier) {
        Tier.MK1 -> 32.0
        Tier.MK3 -> 64.0
        else -> throw IllegalArgumentException("unsupported tier for solar generator")
    }
}