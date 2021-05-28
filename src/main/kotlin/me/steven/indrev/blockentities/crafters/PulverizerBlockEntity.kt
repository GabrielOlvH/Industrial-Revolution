package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry

class PulverizerBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<PulverizerRecipe>(tier, MachineRegistry.PULVERIZER_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(5, 6, 7, 8)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slots = intArrayOf(3, 4) }
        }
    }

    override val type: IRRecipeType<PulverizerRecipe> = PulverizerRecipe.TYPE
}