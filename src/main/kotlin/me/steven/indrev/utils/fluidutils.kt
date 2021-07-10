package me.steven.indrev.utils

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidExtractable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.impl.GroupedFluidInvFixedWrapper
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
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

fun FixedFluidInv.createWrapper(outputTank: Int, inputTank: Int) = object : GroupedFluidInvFixedWrapper(this) {

    override fun attemptExtraction(
        filter: FluidFilter?,
        maxAmount: FluidAmount,
        simulation: Simulation?
    ): FluidVolume {
        if (maxAmount.isNegative)
            throw IllegalArgumentException("maxAmount cannot be negative! (was $maxAmount)")

        var fluid = FluidVolumeUtil.EMPTY
        if (maxAmount.isZero || outputTank < 0)
            return fluid

        val thisMax = maxAmount.roundedSub(fluid.amount(), RoundingMode.DOWN)
        fluid = extractFluid(outputTank, filter, fluid, thisMax, simulation)
        if (fluid.amount() >= maxAmount)
            return fluid
        return fluid
    }

    override fun attemptInsertion(immutableFluid: FluidVolume, simulation: Simulation?): FluidVolume {
        var fluid = immutableFluid
        if (inputTank < 0)
            return fluid
        else if (fluid.isEmpty)
            return FluidVolumeUtil.EMPTY
        fluid = insertFluid(inputTank, fluid.copy(), simulation)
        if (fluid.isEmpty)
            return FluidVolumeUtil.EMPTY
        return fluid
    }
}

fun fluidInsertableOf(world: World, pos: BlockPos, direction: Direction) = FluidAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))

fun fluidExtractableOf(world: World, pos: BlockPos, direction: Direction) = FluidAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))

fun groupedFluidInv(world: World, pos: BlockPos, direction: Direction) = FluidAttributes.GROUPED_INV.get(world, pos, SearchOptions.inDirection(direction))

fun FluidBlock.drainFluid(world: World, pos: BlockPos, state: BlockState): Fluid {
    return if (state.get(FluidBlock.LEVEL) as Int == 0) {
        world.setBlockState(pos, Blocks.AIR.defaultState, 11)
        fluid
    } else {
        Fluids.EMPTY
    }
}

fun FluidExtractable.use(vol: FluidVolume): Boolean {
    if (attemptExtraction({ key -> key == vol.fluidKey }, vol.amount(), Simulation.SIMULATE).amount() == vol.amount()) {
        assert(attemptExtraction({ key -> key == vol.fluidKey }, vol.amount(), Simulation.ACTION).amount() == vol.amount())
        return true
    }
    return false
}