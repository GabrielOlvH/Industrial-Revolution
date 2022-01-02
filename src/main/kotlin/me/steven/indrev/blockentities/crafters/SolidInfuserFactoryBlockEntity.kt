package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.definitions.FactoryStructureDefinition
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class SolidInfuserFactoryBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.SOLID_INFUSER_FACTORY_REGISTRY, pos, state) {

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 700..1100, 1400)
        this.enhancerComponent = EnhancerComponent(intArrayOf(2, 3, 4, 5), Enhancer.DEFAULT, this::getBaseValue, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            input {
                slots = intArrayOf(6, 7, 9, 10, 12, 13, 15, 16, 18, 19)
                filter { _, dir, slot -> canInput(dir, slot) }
            }
            output { slots = intArrayOf(8, 11, 14, 17, 20) }
        }
        this.craftingComponents = Array(5) { index ->
            val component = CraftingComponent(index, this).apply {
                inputSlots = intArrayOf(6 + (index * 3), 6 + (index * 3) + 1)
                outputSlots = intArrayOf(6 + (index * 3) + 2)
            }
            trackObject(CRAFTING_COMPONENT_START_ID + index, component)
            component
        }
        this.multiblockComponent = MultiBlockComponent(FactoryStructureDefinition.SELECTOR)
    }

    override fun splitStacks() {
        splitStacks(TOP_SLOTS)
        splitStacks(BOTTOM_SLOTS)
    }

    private fun canInput(side: Direction?, slot: Int): Boolean {
        if (side == null) return true
        return when (inventoryComponent!!.itemConfig[side]) {
            TransferMode.INPUT_FIRST -> TOP_SLOTS.contains(slot)
            TransferMode.INPUT_SECOND -> BOTTOM_SLOTS.contains(slot)
            else -> true
        }
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> TransferMode.SOLID_INFUSER
            else -> super.getValidConfigurations(type)
        }
    }

    override val type: IRRecipeType<InfuserRecipe> = InfuserRecipe.TYPE

    override fun fromClientTag(tag: NbtCompound) {
        multiblockComponent?.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        multiblockComponent?.writeNbt(tag)
    }

    companion object {
        val TOP_SLOTS = intArrayOf(6, 9, 12, 15, 18)
        val BOTTOM_SLOTS = intArrayOf(7, 10, 13, 16, 19)
        const val CRAFTING_COMPONENT_START_ID = 4
    }
}