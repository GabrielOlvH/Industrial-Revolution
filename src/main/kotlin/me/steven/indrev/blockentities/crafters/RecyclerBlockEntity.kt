package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.RecyclerRecipe
import me.steven.indrev.registry.MachineRegistry

class RecyclerBlockEntity(tier: Tier) : CraftingMachineBlockEntity<RecyclerRecipe>(tier, MachineRegistry.RECYCLER_REGISTRY), EnhancerProvider {

    override val enhancementsSlots: IntArray = intArrayOf(4, 5, 6, 7)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
            coolerSlot = 1
        }
    }

    override val type: IRRecipeType<RecyclerRecipe> = RecyclerRecipe.TYPE

    override fun isLocked(slot: Int, tier: Tier): Boolean = false
}