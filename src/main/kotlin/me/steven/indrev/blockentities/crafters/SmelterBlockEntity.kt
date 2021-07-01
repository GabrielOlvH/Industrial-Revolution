package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.SmelterRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.rawId
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class SmelterBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<SmelterRecipe>(tier, MachineRegistry.SMELTER_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(3, 4, 5, 6)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.2, 1700..2500, 2700)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = object : FluidComponent({ this }, FluidAmount.ofWhole(8)) {
            init {
                this.outputTanks = intArrayOf(0)
            }
        }
        this.propertiesSize = 6
    }

    override fun get(index: Int): Int {
        return when (index) {
            TANK_SIZE -> fluidComponent!!.limit.asInt(1000)
            TANK_AMOUNT_ID -> fluidComponent!![0].amount().asInt(1000)
            TANK_FLUID_ID -> fluidComponent!![0].rawFluid.rawId
            else -> super.get(index)
        }
    }

    override val type: IRRecipeType<SmelterRecipe> = SmelterRecipe.TYPE

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 4 else super.getMaxCount(enhancer)
    }

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        val direction = (state.block as MachineBlock).getFacing(state)
        when (type) {
            ConfigurationType.ITEM -> {
                configuration[direction.rotateYClockwise()] = TransferMode.INPUT
            }
            else -> super.applyDefault(state, type, configuration)
        }
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> arrayOf(TransferMode.INPUT, TransferMode.NONE)
            else -> return super.getValidConfigurations(type)
        }
    }

    companion object {
        const val TANK_SIZE = 4
        const val TANK_AMOUNT_ID = 5
        const val TANK_FLUID_ID = 6
    }
}