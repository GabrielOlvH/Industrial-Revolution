package me.steven.indrev.blockentities.generators

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.EnergySide

class SolarGeneratorBlockEntity(tier: Tier) :
    GeneratorBlockEntity(tier, MachineRegistry.SOLAR_GENERATOR_REGISTRY) {

    override fun shouldGenerate(): Boolean = this.world?.isSkyVisible(pos.up()) == true && this.world?.isDay == true

    override fun createInventory(): DefaultSidedInventory =
        DefaultSidedInventory(2, intArrayOf(), intArrayOf()) { _, _ -> true }

    override fun getMaxOutput(side: EnergySide?): Double = 32.0

    override fun getGenerationRatio(): Double = 0.1

    override fun getOptimalRange(): IntRange = 500..700

    override fun getBaseHeatingEfficiency(): Double = if (temperature > 600) 0.001 else 0.1

    override fun getLimitTemperature(): Double = 1000.0
}