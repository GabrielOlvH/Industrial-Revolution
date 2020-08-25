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
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class DrainBlockEntity(tier: Tier) : AOEMachineBlockEntity(tier, MachineRegistry.DRAIN_REGISTRY) {

    init {
        this.fluidComponent = FluidComponent(FluidAmount.BUCKET)
    }

    override fun machineTick() {
        if ((world?.time ?: return) % 20 == 0L || !fluidComponent!!.tanks[0].volume.isEmpty) return
        val fluidComponent = fluidComponent ?: return
        val hasFluid = Direction.values().mapNotNull { world?.getFluidState(pos.offset(it)) }.any { !it.isEmpty }
        val range = getWorkingArea()
        if (hasFluid && Energy.of(this).simulate().use(getConfig().energyCost)) {
            val mutablePos = pos.mutableCopy()
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
                                Energy.of(this).use(2.0)
                                break
                            }
                        }
                    }
                }
        }
    }

    override fun getWorkingArea(): Box = Box(pos.up()).expand(8.0, 0.0, 8.0).stretch(0.0, 4.0, 0.0)

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    fun getConfig() = IndustrialRevolution.CONFIG.machines.drain
}