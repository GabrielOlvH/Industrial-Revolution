package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.ElectrolysisRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class ElectrolyticSeparatorBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : CraftingMachineBlockEntity<ElectrolysisRecipe>(tier, MachineRegistry.ELECTROLYTIC_SEPARATOR_REGISTRY, pos, state) {

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 500..700, 900)
        this.enhancerComponent = EnhancerComponent(intArrayOf(1, 2, 3, 4), Enhancer.DEFAULT, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            coolerSlot = 0
        }
        this.fluidComponent = ElectrolyticSeparatorFluidComponent()

        trackObject(CRAFTING_COMPONENT_ID, craftingComponents[0])

        trackObject(INPUT_TANK_ID, fluidComponent!![0])
        trackObject(FIRST_OUTPUT_TANK_ID, fluidComponent!![1])
        trackObject(SECOND_OUTPUT_TANK_ID, fluidComponent!![2])
    }

    override val type: IRRecipeType<ElectrolysisRecipe> = ElectrolysisRecipe.TYPE

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        if (type != ConfigurationType.ITEM)
            super.applyDefault(state, type, configuration)
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.FLUID -> TransferMode.ELECTROLYTIC_SEPARATOR
            else -> return super.getValidConfigurations(type)
        }
    }

    inner class ElectrolyticSeparatorFluidComponent : FluidComponent({ this }, bucket * 4, 3) {

        init {
            this.inputTanks = intArrayOf(0)
            this.outputTanks = intArrayOf(1, 2)
        }

        override fun getValidTanks(dir: Direction?): IntArray {
            return when (transferConfig[dir]!!) {
                TransferMode.OUTPUT_FIRST -> intArrayOf(1)
                TransferMode.OUTPUT_SECOND -> intArrayOf(2)
                else -> super.getValidTanks(dir)
            }
        }
    }

    companion object {
        const val CRAFTING_COMPONENT_ID = 4
        const val INPUT_TANK_ID = 5
        const val FIRST_OUTPUT_TANK_ID = 6
        const val SECOND_OUTPUT_TANK_ID = 7
    }
}