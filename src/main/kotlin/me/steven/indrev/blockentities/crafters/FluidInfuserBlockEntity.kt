package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.FluidInfuserRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class FluidInfuserBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : CraftingMachineBlockEntity<FluidInfuserRecipe>(tier, MachineRegistry.FLUID_INFUSER_REGISTRY, pos, state) {

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.06, 700..1100, 1400)
        this.enhancerComponent = EnhancerComponent(intArrayOf(4, 5, 6, 7), Enhancer.DEFAULT, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
            output { slot = 3 }
        }
        this.fluidComponent = FluidInfuserFluidComponent()

        trackObject(CRAFTING_COMPONENT_ID, craftingComponents[0])

        trackObject(INPUT_TANK_ID, fluidComponent!![0])
        trackObject(OUTPUT_TANK_ID, fluidComponent!![1])
    }

    override val syncToWorld: Boolean = true

    override val type: IRRecipeType<FluidInfuserRecipe> = FluidInfuserRecipe.TYPE

    override fun fromClientTag(tag: NbtCompound) {
        fluidComponent!!.fromTag(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        fluidComponent!!.toTag(tag)
    }

    inner class FluidInfuserFluidComponent : FluidComponent({ this }, bucket * 8 , 2) {
        init {
            this.inputTanks = intArrayOf(0)
            this.outputTanks = intArrayOf(1)

        }
    }

    companion object {
        const val CRAFTING_COMPONENT_ID = 4
        const val INPUT_TANK_ID = 5
        const val OUTPUT_TANK_ID = 6
    }
}