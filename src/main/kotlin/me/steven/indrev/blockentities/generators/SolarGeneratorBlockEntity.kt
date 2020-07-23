package me.steven.indrev.blockentities.generators

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier

class SolarGeneratorBlockEntity(tier: Tier) :
    GeneratorBlockEntity(tier, MachineRegistry.SOLAR_GENERATOR_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(2, EMPTY_INT_ARRAY, EMPTY_INT_ARRAY) { _, _ -> true }
        }
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 500..700, 1000.0)
    }

    override fun shouldGenerate(): Boolean = this.world?.isSkyVisible(pos.up()) == true && this.world?.isDay == true && energy < maxStoredPower

    override fun getConfig(): GeneratorConfig = when (tier) {
        Tier.MK1 -> IndustrialRevolution.CONFIG.generators.solarGeneratorMk1
        Tier.MK3 -> IndustrialRevolution.CONFIG.generators.solarGeneratorMk3
        else -> throw IllegalArgumentException("unsupported tier for $this $tier")
    }
}