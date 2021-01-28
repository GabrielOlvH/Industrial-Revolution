package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry

class SolidInfuserBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.INFUSER_REGISTRY) {

    override val upgradeSlots: IntArray = intArrayOf(5, 6, 7, 8)
    override val availableUpgrades: Array<Upgrade> = Upgrade.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(2, 3) }
            output { slot = 4 }
        }
    }

    override val type: IRRecipeType<InfuserRecipe> = InfuserRecipe.TYPE
}