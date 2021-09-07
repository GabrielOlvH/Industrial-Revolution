package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class SolidInfuserBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.SOLID_INFUSER_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(5, 6, 7, 8)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 700..1100, 1400)
        this.inventoryComponent = inventory(this) {
            input {
                slots = intArrayOf(2, 3)
                filter { _, dir, slot -> canInput(dir, slot) }
            }
            output { slot = 4 }
        }

        trackObject(CRAFTING_COMPONENT_ID, craftingComponents[0])
    }

    private fun canInput(side: Direction?, slot: Int): Boolean {
        if (side == null) return true
        return when (inventoryComponent!!.itemConfig[side]) {
            TransferMode.INPUT_FIRST -> slot == 2
            TransferMode.INPUT_SECOND -> slot == 3
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

    companion object {
        const val CRAFTING_COMPONENT_ID = 4
    }
}