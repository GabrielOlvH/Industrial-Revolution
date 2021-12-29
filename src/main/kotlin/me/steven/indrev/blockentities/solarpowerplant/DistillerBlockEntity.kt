package me.steven.indrev.blockentities.solarpowerplant

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.DistillerRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class DistillerBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity<DistillerRecipe>(Tier.MK4, MachineRegistry.DISTILLER_REGISTRY, pos, state) {

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.01, 70..120, 200)
        this.fluidComponent = FluidComponent({ this }, bucket).also {
            it.inputTanks = intArrayOf(0)
        }

        this.enhancerComponent = EnhancerComponent(intArrayOf(3, 4, 5, 6), Enhancer.DEFAULT, this::getBaseValue, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            output { slot = 2 }
        }

        trackObject(CRAFTING_COMPONENT_ID, craftingComponents[0])
        trackObject(TANK_ID, fluidComponent!![0])
    }

    override val type: IRRecipeType<DistillerRecipe> = DistillerRecipe.TYPE

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 2 else super.getMaxCount(enhancer)
    }

    companion object {
        const val CRAFTING_COMPONENT_ID = 4
        const val TANK_ID = 5
    }
}