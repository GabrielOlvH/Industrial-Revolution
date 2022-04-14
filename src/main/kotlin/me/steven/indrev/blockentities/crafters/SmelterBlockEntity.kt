package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.SmelterRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class SmelterBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<SmelterRecipe>(tier, MachineRegistry.SMELTER_REGISTRY, pos, state) {

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.2, 1700..2500, 2700)
        this.enhancerComponent = EnhancerComponent(intArrayOf(3, 4, 5, 6), Enhancer.DEFAULT, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = object : FluidComponent({ this }, bucket * 8) {
            init {
                this.outputTanks = intArrayOf(0)
            }
        }
        trackObject(CRAFTING_COMPONENT_ID, craftingComponents[0])
        trackObject(TANK_ID, fluidComponent!![0])
    }

    override val type: IRRecipeType<SmelterRecipe> = SmelterRecipe.TYPE

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) 4 else super.getMaxCount(enhancer)
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
        const val CRAFTING_COMPONENT_ID = 4
        const val TANK_ID = 5
    }
}