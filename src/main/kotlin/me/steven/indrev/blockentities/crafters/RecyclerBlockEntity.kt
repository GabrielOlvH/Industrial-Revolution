package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.RecyclerRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class RecyclerBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : CraftingMachineBlockEntity<RecyclerRecipe>(tier, MachineRegistry.RECYCLER_REGISTRY, pos, state), EnhancerProvider {

    override val enhancerSlots: IntArray = intArrayOf(4, 5, 6, 7)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
            coolerSlot = 1
        }

        trackObject(CRAFTING_COMPONENT_ID, craftingComponents[0])
    }

    override val type: IRRecipeType<RecyclerRecipe> = RecyclerRecipe.TYPE

    override fun isLocked(slot: Int, tier: Tier): Boolean = false

    companion object {
        const val CRAFTING_COMPONENT_ID = 4
    }
}