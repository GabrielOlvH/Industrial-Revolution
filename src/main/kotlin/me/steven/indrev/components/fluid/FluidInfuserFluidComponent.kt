package me.steven.indrev.components.fluid

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.GroupedFluidInvFixedWrapper
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.blockentities.MachineBlockEntity
import java.math.RoundingMode

class FluidInfuserFluidComponent(machine: () -> MachineBlockEntity<*>) : FluidComponent(machine, FluidAmount.ofWhole(8) , 2) {

    override fun getGroupedInv(): GroupedFluidInv {
        return object : GroupedFluidInvFixedWrapper(this) {

            override fun attemptExtraction(
                filter: FluidFilter?,
                maxAmount: FluidAmount,
                simulation: Simulation?
            ): FluidVolume {
                if (maxAmount.isNegative)
                    throw IllegalArgumentException("maxAmount cannot be negative! (was $maxAmount)")

                var fluid = FluidVolumeUtil.EMPTY
                if (maxAmount.isZero)
                    return fluid

                val thisMax = maxAmount.roundedSub(fluid.amount(), RoundingMode.DOWN)
                fluid = extractFluid(1, filter, fluid, thisMax, simulation)
                if (fluid.amount() >= maxAmount)
                    return fluid
                return fluid
            }

            override fun attemptInsertion(immutableFluid: FluidVolume, simulation: Simulation?): FluidVolume {
                var fluid = immutableFluid
                if (fluid.isEmpty)
                    return FluidVolumeUtil.EMPTY
                fluid = insertFluid(0, fluid.copy(), simulation)
                if (fluid.isEmpty)
                    return FluidVolumeUtil.EMPTY
                return fluid
            }
        }
    }
}