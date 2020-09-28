package me.steven.indrev.blockentities.crafters

import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.CompressorRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.RecipeType

class CompressorBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<CompressorRecipe>(tier, MachineRegistry.COMPRESSOR_REGISTRY) {

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1500.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
        }
    }

    override val type: RecipeType<CompressorRecipe> = CompressorRecipe.TYPE

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT
}