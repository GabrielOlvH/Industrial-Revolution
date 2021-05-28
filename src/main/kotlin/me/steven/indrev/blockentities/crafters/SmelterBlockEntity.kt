package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.SmelterRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

class SmelterBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<SmelterRecipe>(tier, MachineRegistry.SMELTER_REGISTRY) {

    override val enhancementsSlots: IntArray = intArrayOf(3, 4, 5, 6)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.2, 1700..2500, 2700.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = FluidComponent({ this }, FluidAmount.ofWhole(8))
    }

    override val type: IRRecipeType<SmelterRecipe> = SmelterRecipe.TYPE

    override fun getMaxEnhancer(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 4 else super.getMaxEnhancer(enhancer)
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
}