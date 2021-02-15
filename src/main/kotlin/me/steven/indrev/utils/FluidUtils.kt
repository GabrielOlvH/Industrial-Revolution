package me.steven.indrev.utils

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.GroupedFluidInvFixedWrapper
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import java.math.RoundingMode

val NUGGET_AMOUNT: FluidAmount = FluidAmount.of(10, 625)
val INGOT_AMOUNT: FluidAmount = NUGGET_AMOUNT.mul(9)
val BLOCK_AMOUNT: FluidAmount = INGOT_AMOUNT.mul(9)
val SCRAP_AMOUNT: FluidAmount = INGOT_AMOUNT.div(4)

val MB: FluidAmount = FluidAmount.BUCKET.div(1000)

operator fun FluidAmount.plus(volume: FluidAmount): FluidAmount {
    return add(volume)
}

operator fun FluidAmount.minus(volume: FluidAmount): FluidAmount {
    return sub(volume)
}

operator fun FluidAmount.times(volume: FluidAmount): FluidAmount {
    return mul(volume)
}

operator fun FluidAmount.times(whole: Int): FluidAmount {
    return mul(whole.toLong())
}

fun  FixedFluidInv.createWrapper(outputTank: Int, inputTank: Int) = object : GroupedFluidInvFixedWrapper(this) {

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
        fluid = extractFluid(outputTank, filter, fluid, thisMax, simulation)
        if (fluid.amount() >= maxAmount)
            return fluid
        return fluid
    }

    override fun attemptInsertion(immutableFluid: FluidVolume, simulation: Simulation?): FluidVolume {
        var fluid = immutableFluid
        if (fluid.isEmpty)
            return FluidVolumeUtil.EMPTY
        fluid = insertFluid(inputTank, fluid.copy(), simulation)
        if (fluid.isEmpty)
            return FluidVolumeUtil.EMPTY
        return fluid
    }
}