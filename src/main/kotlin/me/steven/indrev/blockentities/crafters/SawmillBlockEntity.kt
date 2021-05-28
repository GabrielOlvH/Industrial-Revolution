package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.SawmillRecipe
import me.steven.indrev.registry.MachineRegistry

class SawmillBlockEntity(tier: Tier) : CraftingMachineBlockEntity<SawmillRecipe>(tier, MachineRegistry.SAWMILL_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(7, 8, 9, 10)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slots = intArrayOf(3, 4, 5, 6) }
        }
    }

    override val type: IRRecipeType<SawmillRecipe> = SawmillRecipe.TYPE
}