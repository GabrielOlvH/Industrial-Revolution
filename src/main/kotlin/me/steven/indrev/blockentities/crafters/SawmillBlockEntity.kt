package me.steven.indrev.blockentities.crafters

import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.SawmillRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.RecipeType

class SawmillBlockEntity(tier: Tier) : CraftingMachineBlockEntity<SawmillRecipe>(tier, MachineRegistry.SAWMILL_REGISTRY) {

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slots = intArrayOf(3, 4, 5, 6) }
        }
    }

    override val type: RecipeType<SawmillRecipe> = SawmillRecipe.TYPE

    override fun getUpgradeSlots(): IntArray = intArrayOf(7, 8, 9, 10)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT
}