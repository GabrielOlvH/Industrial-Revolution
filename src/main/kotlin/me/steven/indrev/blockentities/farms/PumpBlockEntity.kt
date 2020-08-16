package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.Box
import team.reborn.energy.EnergySide

class PumpBlockEntity(tier: Tier) : AOEMachineBlockEntity(tier, MachineRegistry.PUMP_REGISTRY) {

    init {
        this.fluidComponent = FluidComponent(FluidAmount.BUCKET)
    }

    override fun machineTick() {
        if ((world?.time ?: return) % 20 == 0L || fluidComponent?.volume?.isEmpty == false) return
        val fluidComponent = fluidComponent ?: return
        val down = pos.down()
        val hasFluid = world?.getFluidState(down)?.isEmpty == false
        val range = getWorkingArea()
        if (hasFluid) {
            val mutablePos = down.mutableCopy()
            for (x in range.minX.toInt()..range.maxX.toInt())
                for (y in range.minY.toInt()..range.maxY.toInt()) {
                    for (z in range.minZ.toInt()..range.maxZ.toInt()) {
                        mutablePos.set(x, y, z)
                        val blockState = world?.getBlockState(mutablePos)
                        val block = blockState?.block
                        if (block is FluidDrainable && block is FluidBlock) {
                            val drained = block.tryDrainFluid(world, mutablePos, blockState)
                            if (drained != Fluids.EMPTY) {
                                val toInsert = FluidKeys.get(drained).withAmount(FluidAmount.BUCKET)
                                world?.setBlockState(mutablePos, Blocks.AIR.defaultState)
                                fluidComponent.insertable.insert(toInsert)
                                break
                            }
                        }
                    }
                }
        }
    }

    override fun getWorkingArea(): Box = Box(pos).expand(8.0, 1.0, 8.0)

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun getConfig() = IndustrialRevolution.CONFIG.machines.chopper
}