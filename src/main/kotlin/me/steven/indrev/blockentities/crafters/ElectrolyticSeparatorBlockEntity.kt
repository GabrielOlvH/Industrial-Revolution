package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.ElectrolysisRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.createWrapper
import me.steven.indrev.utils.rawId
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.math.RoundingMode

class ElectrolyticSeparatorBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : CraftingMachineBlockEntity<ElectrolysisRecipe>(tier, MachineRegistry.ELECTROLYTIC_SEPARATOR_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(1, 2, 3, 4)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 500..700, 900)
        this.inventoryComponent = inventory(this) {
            coolerSlot = 0
        }
        this.fluidComponent = ElectrolyticSeparatorFluidComponent()
        this.propertiesSize = 13
    }

    override val type: IRRecipeType<ElectrolysisRecipe> = ElectrolysisRecipe.TYPE

    override fun get(index: Int): Int {
        return when (index) {
            TANK_SIZE_ID -> fluidComponent!!.limit.asInt(1000)
            FIRST_INPUT_TANK_ID -> fluidComponent!![0].amount().asInt(1000)
            FIRST_INPUT_TANK_FLUID_ID -> fluidComponent!![0].rawFluid.rawId
            SECOND_INPUT_TANK_ID -> fluidComponent!![1].amount().asInt(1000)
            SECOND_INPUT_TANK_FLUID_ID -> fluidComponent!![1].rawFluid.rawId
            OUTPUT_TANK_ID -> fluidComponent!![2].amount().asInt(1000)
            OUTPUT_TANK_FLUID_ID -> fluidComponent!![2].rawFluid.rawId
            else -> super.get(index)
        }
    }

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

    inner class ElectrolyticSeparatorFluidComponent : FluidComponent({ this }, FluidAmount.ofWhole(4), 3) {

        init {
            this.inputTanks = intArrayOf(0)
            this.outputTanks = intArrayOf(1, 2)
        }

        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation?): FluidVolume? {
            var fluid = fluid
            if (fluid.isEmpty) {
                return FluidVolumeUtil.EMPTY
            }
            fluid = insertFluid(0, fluid.copy(), simulation)
            if (fluid.isEmpty) {
                return FluidVolumeUtil.EMPTY
            }
            return fluid
        }

        override fun attemptExtraction(
            filter: FluidFilter?,
            maxAmount: FluidAmount,
            simulation: Simulation?
        ): FluidVolume? {
            require(!maxAmount.isNegative) { "maxAmount cannot be negative! (was $maxAmount)" }
            var fluid = FluidVolumeUtil.EMPTY
            if (maxAmount.isZero) {
                return fluid
            }
            for (t in 1 until tankCount) {
                val thisMax = maxAmount.roundedSub(fluid.amount_F, RoundingMode.DOWN)
                fluid = extractFluid(t, filter, fluid, thisMax, simulation)
                if (!fluid.amount_F.isLessThan(maxAmount)) {
                    return fluid
                }
            }
            return fluid
        }

        override fun getInteractInventory(tank: Int): FluidTransferable {
            return createWrapper(tank, tank)
        }
    }

    companion object {
        const val TANK_SIZE_ID = 6
        const val FIRST_INPUT_TANK_ID = 7
        const val FIRST_INPUT_TANK_FLUID_ID = 8
        const val SECOND_INPUT_TANK_ID = 9
        const val SECOND_INPUT_TANK_FLUID_ID = 10
        const val OUTPUT_TANK_ID = 11
        const val OUTPUT_TANK_FLUID_ID = 12
    }
}