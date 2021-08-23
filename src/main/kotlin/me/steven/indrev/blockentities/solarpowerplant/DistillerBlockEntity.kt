package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.blockentities.crafters.SmelterBlockEntity
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.recipes.machines.DistillerRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.rawId
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class DistillerBlockEntity(pos: BlockPos, state: BlockState) : CraftingMachineBlockEntity<DistillerRecipe>(Tier.MK4, MachineRegistry.DISTILLER_REGISTRY, pos, state) {

    override val enhancerSlots: IntArray = intArrayOf(3, 4, 5, 6)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.01, 70..120, 200)
        this.fluidComponent = FluidComponent({this}, FluidAmount.BUCKET)
        this.inventoryComponent = inventory(this) {
            output { slot = 2 }
        }
        this.propertiesSize = 9
    }

    override val type: IRRecipeType<DistillerRecipe> = DistillerRecipe.TYPE

    override fun getMaxCount(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 2 else super.getMaxCount(enhancer)
    }

    override fun get(index: Int): Int {
        return when (index) {
            SmelterBlockEntity.TANK_SIZE -> fluidComponent!!.limit.asInt(1000)
            SmelterBlockEntity.TANK_AMOUNT_ID -> fluidComponent!![0].amount().asInt(1000)
            SmelterBlockEntity.TANK_FLUID_ID -> fluidComponent!![0].rawFluid.rawId
            else -> super.get(index)
        }
    }

    companion object {
        const val TANK_SIZE = 6
        const val TANK_AMOUNT_ID = 7
        const val TANK_FLUID_ID = 8
    }
}