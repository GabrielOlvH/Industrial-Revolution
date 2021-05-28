package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.multiblock.FactoryStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.recipes.machines.CompressorRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.screen.ArrayPropertyDelegate

class CompressorFactoryBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<CompressorRecipe>(tier, MachineRegistry.COMPRESSOR_FACTORY_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(2, 3, 4, 5)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.propertyDelegate = ArrayPropertyDelegate(15)
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
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
        this.multiblockComponent = MultiBlockComponent({ id -> id.variant == "factory" }, FactoryStructureDefinition.SELECTOR)

    }

    override val type: IRRecipeType<CompressorRecipe> = CompressorRecipe.TYPE
}