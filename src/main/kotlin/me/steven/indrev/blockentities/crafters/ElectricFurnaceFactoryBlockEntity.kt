package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.multiblock.FactoryStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.mixin.common.MixinAbstractCookingRecipe
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.recipes.machines.VanillaCookingRecipeCachedGetter
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.screen.ArrayPropertyDelegate

class ElectricFurnaceFactoryBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<MixinAbstractCookingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_FACTORY_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(2, 3, 4, 5)
    override val availableEnhancers: Array<Enhancer> = Enhancer.FURNACE

    init {
        this.propertyDelegate = ArrayPropertyDelegate(15)
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 1300..1700, 2000.0)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(6, 8, 10, 12, 14) }
            output { slots =intArrayOf(7, 9, 11, 13, 15) }
        }
        this.craftingComponents = Array(5) { index ->
            CraftingComponent(index, this).apply {
                inputSlots = intArrayOf(6 + (index * 2))
                outputSlots = intArrayOf(6 + (index * 2) + 1)
            }
        }
        this.multiblockComponent = MultiBlockComponent({ id -> id.variant == "factory" },FactoryStructureDefinition.SELECTOR)
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