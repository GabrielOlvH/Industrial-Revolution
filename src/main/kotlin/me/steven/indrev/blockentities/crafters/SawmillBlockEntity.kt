package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.SawmillRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class SawmillBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : CraftingMachineBlockEntity<SawmillRecipe>(tier, MachineRegistry.SAWMILL_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(7, 8, 9, 10)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 700..1100, 1400)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slots = intArrayOf(3, 4, 5, 6) }
        }
    }

    override val type: IRRecipeType<SawmillRecipe> = SawmillRecipe.TYPE
}