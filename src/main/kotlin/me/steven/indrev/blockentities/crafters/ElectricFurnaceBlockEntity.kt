package me.steven.indrev.blockentities.crafters

import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.mixin.MixinAbstractCookingRecipe
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.VanillaRecipeCachedGetter
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier

class ElectricFurnaceBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<MixinAbstractCookingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_REGISTRY) {

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 1300..1700, 2000.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
        }
    }

    override val type: IRecipeGetter<MixinAbstractCookingRecipe>
        get() {
            val upgrades = getUpgrades(inventoryComponent!!.inventory)
            return when (upgrades.keys.firstOrNull { it == Upgrade.BLAST_FURNACE || it == Upgrade.SMOKER }) {
                Upgrade.BLAST_FURNACE -> VanillaRecipeCachedGetter.BLASTING
                Upgrade.SMOKER -> VanillaRecipeCachedGetter.SMOKING
                else -> VanillaRecipeCachedGetter.SMELTING
            } as IRecipeGetter<MixinAbstractCookingRecipe>
        }

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.values()
}