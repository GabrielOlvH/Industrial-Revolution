package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidInfuserFluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.recipes.machines.FluidInfuserRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry

class FluidInfuserBlockEntity(tier: Tier) : CraftingMachineBlockEntity<FluidInfuserRecipe>(tier, MachineRegistry.FLUID_INFUSER_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(4, 5, 6, 7)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
        }
        this.fluidComponent = FluidInfuserFluidComponent { this }
    }

    override val type: IRRecipeType<FluidInfuserRecipe> = FluidInfuserRecipe.TYPE
}