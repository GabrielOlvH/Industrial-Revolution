package me.steven.indrev.fluids

import me.steven.indrev.registry.IRRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class MoltenNetheriteFluid : FlowableFluid() {
    override fun toBlockState(state: FluidState?): BlockState? = IRRegistry.MOLTEN_NETHERITE.defaultState.with(FluidBlock.LEVEL, method_15741(state))

    override fun getBucketItem(): Item = IRRegistry.MOLTEN_NETHERITE_BUCKET

    override fun getLevelDecreasePerBlock(world: WorldView?): Int = 3

    override fun getTickRate(world: WorldView?): Int = 5

    override fun getFlowing(): Fluid = IRRegistry.MOLTEN_NETHERITE_FLOWING

    override fun getStill(): Fluid = IRRegistry.MOLTEN_NETHERITE_STILL

    override fun isInfinite(): Boolean = false

    override fun getFlowSpeed(world: WorldView?): Int = 5

    override fun canBeReplacedWith(state: FluidState?, world: BlockView?, pos: BlockPos?, fluid: Fluid, direction: Direction): Boolean = false

    override fun getBlastResistance(): Float = 100F

    override fun beforeBreakingBlock(world: WorldAccess, pos: BlockPos?, state: BlockState) {
        val blockEntity = if (state.block.hasBlockEntity()) world.getBlockEntity(pos) else null
        Block.dropStacks(state, world.world, pos, blockEntity)
    }

    override fun matchesType(fluid: Fluid?): Boolean = fluid == flowing || fluid == still


    class Flowing : MoltenNetheriteFluid() {
        override fun appendProperties(builder: StateManager.Builder<Fluid, FluidState>?) {
            super.appendProperties(builder)
            builder?.add(LEVEL)
        }

        override fun getLevel(state: FluidState): Int = state[LEVEL]

        override fun isStill(state: FluidState?): Boolean = false
    }

    class Still : MoltenNetheriteFluid() {
        override fun getLevel(state: FluidState?): Int = 8

        override fun isStill(state: FluidState?): Boolean = true

    }
}