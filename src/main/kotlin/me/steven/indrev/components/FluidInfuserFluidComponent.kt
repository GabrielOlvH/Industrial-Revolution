package me.steven.indrev.components

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidExtractable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.GroupedFluidInvFixedWrapper
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import java.math.RoundingMode

class FluidInfuserFluidComponent : FluidComponent(FluidAmount(8) , 2) {
    override fun getExtractable(): FluidExtractable {
        return object : GroupedFluidInvFixedWrapper(this) {
            override fun attemptExtraction(filter: FluidFilter?, maxAmount: FluidAmount?, simulation: Simulation?): FluidVolume {
                require(!maxAmount!!.isNegative) { "maxAmount cannot be negative! (was $maxAmount)" }
                var fluid = FluidVolumeUtil.EMPTY
                if (maxAmount.isZero) {
                    return fluid
                }
                val t = 1
                val thisMax = maxAmount.roundedSub(fluid.amount_F, RoundingMode.DOWN)
                fluid = FluidVolumeUtil.extractSingle(inv(), t, filter, fluid, thisMax, simulation)
                if (fluid.amount_F >= maxAmount) {
                    return fluid
                }
                return fluid
            }

            override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation?): FluidVolume {
                var fluid = fluid
                if (fluid.isEmpty) {
                    return FluidVolumeUtil.EMPTY
                }
                fluid = fluid.copy()
                val t = 0
                fluid = FluidVolumeUtil.insertSingle(inv(), t, fluid, simulation)
                if (fluid.isEmpty) {
                    return FluidVolumeUtil.EMPTY
                }
                return fluid
            }
        }
    }
}