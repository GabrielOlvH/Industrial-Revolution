package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.mixin.common.MixinAbstractCookingRecipe
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.VanillaCookingRecipeCachedGetter
import me.steven.indrev.registry.MachineRegistry

class ElectricFurnaceBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<MixinAbstractCookingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(4, 5, 6, 7)
    override val availableEnhancers: Array<Enhancer> = Enhancer.FURNACE

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 1300..1700, 2000.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override val type: IRecipeGetter<MixinAbstractCookingRecipe>
        get() {
            val enhancements = getEnhancers(inventoryComponent!!.inventory)
            return when (enhancements.keys.firstOrNull { it == Enhancer.BLAST_FURNACE || it == Enhancer.SMOKER }) {
                Enhancer.BLAST_FURNACE -> VanillaCookingRecipeCachedGetter.BLASTING
                Enhancer.SMOKER -> VanillaCookingRecipeCachedGetter.SMOKING
                else -> VanillaCookingRecipeCachedGetter.SMELTING
            } as IRecipeGetter<MixinAbstractCookingRecipe>
        }
}