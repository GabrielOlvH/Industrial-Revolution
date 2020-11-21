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

            override fun getInsertionFilter(): FluidFilter = inv().getFilterForTank(0)

            override fun attemptExtraction(
                filter: FluidFilter?,
                maxAmount: FluidAmount,
                simulation: Simulation?
            ): FluidVolume {
                if (maxAmount.isNegative)
                    throw IllegalArgumentException("maxAmount cannot be negative! (was $maxAmount)")

                    var fluid = FluidVolumeUtil.EMPTY
                    return if (maxAmount.isZero)
                        fluid
                    else {
                        val tank = 1
                        val thisMax = maxAmount.roundedSub(fluid.amount_F, RoundingMode.DOWN)
                        fluid = FluidVolumeUtil.extractSingle(this.inv(), tank, filter, fluid, thisMax, simulation)
                        if (fluid.amount_F >= maxAmount)
                            return fluid
                        fluid
                    }
            }

            override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation?): FluidVolume {
                var fluid = fluid
                return if (fluid.isEmpty)
                    FluidVolumeUtil.EMPTY
                else {
                    fluid = fluid.copy()
                    val tank = 0
                    fluid = FluidVolumeUtil.insertSingle(this.inv(), tank, fluid, simulation)
                    if (fluid.isEmpty)
                        return FluidVolumeUtil.EMPTY
                    fluid
                }
            }
        }
    }
}