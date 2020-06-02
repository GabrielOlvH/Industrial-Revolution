package me.steven.indrev.blockentities.generators

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.EnergySide

class SolarGeneratorBlockEntity(tier: Tier) :
    GeneratorBlockEntity(tier, MachineRegistry.SOLAR_GENERATOR_REGISTRY) {

    override fun shouldGenerate(): Boolean = this.world?.isSkyVisible(pos.up()) == true && this.world?.isDay == true

    override fun createInventory(): DefaultSidedInventory = DefaultSidedInventory(0, intArrayOf(), intArrayOf())

    override fun getMaxOutput(side: EnergySide?): Double = 32.0

    override fun getGenerationRatio(): Double = 0.1
}