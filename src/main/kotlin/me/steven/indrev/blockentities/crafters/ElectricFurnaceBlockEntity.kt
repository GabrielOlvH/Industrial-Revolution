package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.mixin.common.MixinAbstractCookingRecipe
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.VanillaCookingRecipeCachedGetter
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class ElectricFurnaceBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<MixinAbstractCookingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(4, 5, 6, 7)
    override val availableEnhancers: Array<Enhancer> = Enhancer.FURNACE

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.1, 1300..1700, 2000)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override val type: IRecipeGetter<MixinAbstractCookingRecipe>
        get() {
            val upgrades = getEnhancers()
            return when (upgrades.keys.firstOrNull { it == Enhancer.BLAST_FURNACE || it == Enhancer.SMOKER }) {
                Enhancer.BLAST_FURNACE -> VanillaCookingRecipeCachedGetter.BLASTING
                Enhancer.SMOKER -> VanillaCookingRecipeCachedGetter.SMOKING
                else -> VanillaCookingRecipeCachedGetter.SMELTING
            } as IRecipeGetter<MixinAbstractCookingRecipe>
        }
}