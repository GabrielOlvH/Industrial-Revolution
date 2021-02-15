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

    private val grouped = object : GroupedFluidInvFixedWrapper(this) {

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

    override fun getGroupedInv(): GroupedFluidInv = grouped

    override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation?): FluidVolume? {
        return grouped.attemptInsertion(fluid, simulation)
    }

    override fun getMinimumAcceptedAmount(): FluidAmount? {
        return grouped.minimumAcceptedAmount
    }

    override fun getInsertionFilter(): FluidFilter? {
        return grouped.insertionFilter
    }

    override fun attemptExtraction(
        filter: FluidFilter?,
        maxAmount: FluidAmount,
        simulation: Simulation?
    ): FluidVolume {
        return grouped.attemptExtraction(filter, maxAmount, simulation)
    }

    override fun attemptAnyExtraction(maxAmount: FluidAmount?, simulation: Simulation?): FluidVolume? {
        return grouped.attemptAnyExtraction(maxAmount, simulation)
    }
}