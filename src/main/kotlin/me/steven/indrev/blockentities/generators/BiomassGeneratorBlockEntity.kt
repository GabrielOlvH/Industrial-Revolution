package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.item.Item
import net.minecraft.screen.ArrayPropertyDelegate

class BiomassGeneratorBlockEntity(tier: Tier) : SolidFuelGeneratorBlockEntity(tier, MachineRegistry.BIOMASS_GENERATOR_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(6)
        this.temperatureComponent = TemperatureComponent({ this }, 0.08, 900..2000, 2500.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
    }

    override fun getFuelMap(): Map<Item, Int> = BURN_TIME_MAP

    companion object {
        private val BURN_TIME_MAP = hashMapOf<Item, Int>()

        init {
            BURN_TIME_MAP[IRRegistry.BIOMASS] = 150
        }
    }
}