package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier

class SolarGeneratorBlockEntity(tier: Tier) :
    GeneratorBlockEntity(tier, MachineRegistry.SOLAR_GENERATOR_REGISTRY) {

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 500..700, 1000.0)
        this.inventoryComponent = inventory(this) {}

    }

    override fun shouldGenerate(): Boolean = this.world?.isSkyVisible(pos.up()) == true && this.world?.isDay == true && energy < energyCapacity
}