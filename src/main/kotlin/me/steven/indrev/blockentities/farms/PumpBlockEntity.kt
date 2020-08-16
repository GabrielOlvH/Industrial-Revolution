package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.Box
import team.reborn.energy.EnergySide

class PumpBlockEntity(tier: Tier) : AOEMachineBlockEntity(tier, MachineRegistry.PUMP_REGISTRY) {

    init {
        this.fluidComponent = FluidComponent(FluidAmount(1))
    }

    override fun machineTick() {
        if ((world?.time ?: return) % 20 == 0L) return
        val fluidComponent = fluidComponent ?: return
        val down = pos.down()
        val hasFluid = world?.getFluidState(down)?.isEmpty == false
        val range = getWorkingArea()
        if (hasFluid) {
            val mutablePos = down.mutableCopy()
            for (x in range.minX.toInt()..range.maxX.toInt())
                for (z in range.minZ.toInt()..range.maxZ.toInt()) {
                    mutablePos.set(x, down.y, z)
                    val fluidState = world?.getFluidState(mutablePos)
                    val fluidBlock = fluidState?.blockState?.block
                    if (fluidState?.isEmpty == false) {
                        val toInsert = FluidKeys.get(fluidState.fluid).withAmount(FluidAmount.BUCKET)
                        if (fluidComponent.insertable.attemptInsertion(toInsert, Simulation.SIMULATE).isEmpty
                            && fluidBlock is FluidDrainable
                            && fluidBlock.tryDrainFluid(world, mutablePos, fluidState.blockState) != Fluids.EMPTY
                        ) {
                            fluidComponent.insertable.insert(toInsert)
                            break
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