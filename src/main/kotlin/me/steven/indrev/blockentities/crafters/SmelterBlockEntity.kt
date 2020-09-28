package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.SmelterRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.RecipeType

class SmelterBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<SmelterRecipe>(tier, MachineRegistry.SMELTER_REGISTRY) {

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.2, 1700..2500, 2700.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = FluidComponent({ this }, FluidAmount(8))
    }

    override val type: RecipeType<SmelterRecipe> = SmelterRecipe.TYPE

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT
}