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
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.ElectrolysisRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.createWrapper
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.plus
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.math.RoundingMode

class ElectrolyticSeparatorBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : CraftingMachineBlockEntity<ElectrolysisRecipe>(tier, MachineRegistry.ELECTROLYTIC_SEPARATOR_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(0, 1, 2, 3)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 500..700, 900)
        this.inventoryComponent = inventory(this) {}
        this.fluidComponent = ElectrolyticSeparatorFluidComponent()
        this.craftingComponents = arrayOf(ElectrolyticSeparatorCraftingComponent())
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
            ConfigurationType.FLUID -> arrayOf(TransferMode.OUTPUT_FIRST, TransferMode.OUTPUT_SECOND, TransferMode.NONE)
            else -> return super.getValidConfigurations(type)
        }
    }

    inner class ElectrolyticSeparatorFluidComponent : FluidComponent({ this }, FluidAmount.ofWhole(4), 3) {

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

    inner class ElectrolyticSeparatorCraftingComponent : CraftingComponent<ElectrolysisRecipe>(0, this) {
        override fun handleInventories(inventory: IRInventory, inputInventory: List<ItemStack>, recipe: IRRecipe) {
            if (recipe is ElectrolysisRecipe) {
                val fluidInput = recipe.fluidInput
                val inputTank = fluidComponent!!.tanks[0]
                val amount = inputTank.amount() - fluidInput.amount()
                fluidComponent!![0] = inputTank.fluidKey.withAmount(amount)

                val firstOut = recipe.fluidOutput
                val firstOutTank = fluidComponent!!.tanks[1]
                val firstOutAmount = firstOutTank.amount() + firstOut.amount()
                fluidComponent!![1] = firstOut.fluidKey.withAmount(firstOutAmount)

                val secondOut = recipe.secondFluidOutput
                val secondOutTank = fluidComponent!!.tanks[2]
                val secoundOutAmount = secondOutTank.amount() + secondOut.amount()
                fluidComponent!![2] = secondOut.fluidKey.withAmount(secoundOutAmount)
            }
        }
    }
}