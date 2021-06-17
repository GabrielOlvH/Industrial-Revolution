package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.BlockPos

class BiomassGeneratorBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : SolidFuelGeneratorBlockEntity(tier, MachineRegistry.BIOMASS_GENERATOR_REGISTRY, pos, state) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(6)
        this.temperatureComponent = TemperatureComponent(this, 0.08, 900..2000, 2500)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
    }

    override fun getFuelMap(): Map<Item, Int> = BURN_TIME_MAP

    companion object {
        private val BURN_TIME_MAP = hashMapOf<Item, Int>()

        init {
            BURN_TIME_MAP[IRItemRegistry.BIOMASS] = 150
        }
    }
}