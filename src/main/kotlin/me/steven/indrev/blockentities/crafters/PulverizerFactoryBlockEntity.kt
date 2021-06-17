package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.multiblock.FactoryStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.BlockPos

class PulverizerFactoryBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<PulverizerRecipe>(tier, MachineRegistry.PULVERIZER_FACTORY_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(2, 3, 4, 5)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.propertyDelegate = ArrayPropertyDelegate(15)
        this.temperatureComponent = TemperatureComponent(this, 0.06, 700..1100, 1400)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(6, 8, 10, 12, 14) }
            output { slots = intArrayOf(7, 9, 11, 13, 15) }
        }
        this.craftingComponents = Array(5) { index ->
            CraftingComponent(index, this).apply {
                inputSlots = intArrayOf(6 + (index * 2))
                outputSlots = intArrayOf(6 + (index * 2) + 1)
            }
        }
        this.multiblockComponent = MultiBlockComponent({ id -> id.variant == "factory" },FactoryStructureDefinition.SELECTOR)
    }

    override val type: IRRecipeType<PulverizerRecipe> = PulverizerRecipe.TYPE
}