package me.steven.indrev.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.stream.Stream

class SulfurCrystalBlock(settings: Settings) : Block(settings) {

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.UP)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = when (state[FACING]) {
        Direction.DOWN -> DOWN_SHAPE
        Direction.NORTH -> NORTH_SHAPE
        Direction.SOUTH -> SOUTH_SHAPE
        Direction.WEST -> WEST_SHAPE
        Direction.EAST -> EAST_SHAPE
        else -> UP_SHAPE
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return defaultState.with(FACING, ctx?.playerLookDirection?.opposite)
    }

    companion object {
        val FACING = Properties.FACING
        private val UP_SHAPE = Stream.of(
            createCuboidShape(2.0, 5.0, 4.0, 3.0, 6.0, 5.0),
            createCuboidShape(2.0, 4.0, 4.0, 4.0, 5.0, 6.0),
            createCuboidShape(1.0, 0.0, 3.0, 4.0, 4.0, 6.0),
            createCuboidShape(3.0, 0.0, 2.0, 5.0, 1.0, 4.0),
            createCuboidShape(0.0, 0.0, 5.0, 2.0, 2.0, 7.0),
            createCuboidShape(10.0, 0.0, 1.0, 12.0, 1.0, 3.0),
            createCuboidShape(11.0, 0.0, 2.0, 14.0, 4.0, 5.0),
            createCuboidShape(11.0, 4.0, 2.0, 13.0, 5.0, 4.0),
            createCuboidShape(12.0, 5.0, 3.0, 13.0, 6.0, 4.0),
            createCuboidShape(13.0, 0.0, 4.0, 15.0, 2.0, 6.0),
            createCuboidShape(6.0, 0.0, 7.0, 8.0, 2.0, 10.0),
            createCuboidShape(7.0, 0.0, 8.0, 12.0, 5.0, 13.0),
            createCuboidShape(8.0, 5.0, 9.0, 11.0, 8.0, 12.0),
            createCuboidShape(9.0, 8.0, 10.0, 10.0, 10.0, 11.0),
            createCuboidShape(11.0, 0.0, 12.0, 14.0, 3.0, 15.0)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)

        private val DOWN_SHAPE = Stream.of(
            createCuboidShape(13.0, 11.0, 4.0, 14.0, 12.0, 5.0),
            createCuboidShape(12.0, 12.0, 4.0, 14.0, 13.0, 6.0),
            createCuboidShape(12.0, 13.0, 3.0, 15.0, 17.0, 6.0),
            createCuboidShape(11.0, 16.0, 2.0, 13.0, 17.0, 4.0),
            createCuboidShape(14.0, 15.0, 5.0, 16.0, 17.0, 7.0),
            createCuboidShape(4.0, 16.0, 1.0, 6.0, 17.0, 3.0),
            createCuboidShape(2.0, 13.0, 2.0, 5.0, 17.0, 5.0),
            createCuboidShape(3.0, 12.0, 2.0, 5.0, 13.0, 4.0),
            createCuboidShape(3.0, 11.0, 3.0, 4.0, 12.0, 4.0),
            createCuboidShape(1.0, 15.0, 4.0, 3.0, 17.0, 6.0),
            createCuboidShape(8.0, 15.0, 7.0, 10.0, 17.0, 10.0),
            createCuboidShape(4.0, 12.0, 8.0, 9.0, 17.0, 13.0),
            createCuboidShape(5.0, 9.0, 9.0, 8.0, 12.0, 12.0),
            createCuboidShape(6.0, 7.0, 10.0, 7.0, 9.0, 11.0),
            createCuboidShape(2.0, 14.0, 12.0, 5.0, 17.0, 15.0)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)

        private val EAST_SHAPE = Stream.of(
            createCuboidShape(5.0, 11.0, 13.0, 6.0, 12.0, 14.0),
            createCuboidShape(4.0, 10.0, 12.0, 5.0, 12.0, 14.0),
            createCuboidShape(0.0, 10.0, 12.0, 4.0, 13.0, 15.0),
            createCuboidShape(0.0, 12.0, 11.0, 1.0, 14.0, 13.0),
            createCuboidShape(0.0, 9.0, 14.0, 2.0, 11.0, 16.0),
            createCuboidShape(0.0, 13.0, 4.0, 1.0, 15.0, 6.0),
            createCuboidShape(0.0, 11.0, 2.0, 4.0, 14.0, 5.0),
            createCuboidShape(4.0, 12.0, 3.0, 5.0, 14.0, 5.0),
            createCuboidShape(5.0, 12.0, 3.0, 6.0, 13.0, 4.0),
            createCuboidShape(0.0, 10.0, 1.0, 2.0, 12.0, 3.0),
            createCuboidShape(0.0, 6.0, 8.0, 2.0, 9.0, 10.0),
            createCuboidShape(0.0, 3.0, 4.0, 5.0, 8.0, 9.0),
            createCuboidShape(5.0, 4.0, 5.0, 8.0, 7.0, 8.0),
            createCuboidShape(8.0, 5.0, 6.0, 10.0, 6.0, 7.0),
            createCuboidShape(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)

        private val SOUTH_SHAPE = Stream.of(
            createCuboidShape(1.0, 4.0, 0.0, 3.0, 6.0, 2.0),
            createCuboidShape(3.0, 3.0, 5.0, 4.0, 4.0, 6.0),
            createCuboidShape(3.0, 2.0, 4.0, 5.0, 4.0, 5.0),
            createCuboidShape(2.0, 2.0, 0.0, 5.0, 5.0, 4.0),
            createCuboidShape(4.0, 1.0, 0.0, 6.0, 3.0, 1.0),
            createCuboidShape(11.0, 2.0, 0.0, 13.0, 4.0, 1.0),
            createCuboidShape(12.0, 3.0, 0.0, 15.0, 6.0, 4.0),
            createCuboidShape(12.0, 4.0, 4.0, 14.0, 6.0, 5.0),
            createCuboidShape(13.0, 4.0, 5.0, 14.0, 5.0, 6.0),
            createCuboidShape(14.0, 5.0, 0.0, 16.0, 7.0, 2.0),
            createCuboidShape(8.0, 7.0, 0.0, 10.0, 10.0, 2.0),
            createCuboidShape(4.0, 8.0, 0.0, 9.0, 13.0, 5.0),
            createCuboidShape(5.0, 9.0, 5.0, 8.0, 12.0, 8.0),
            createCuboidShape(6.0, 10.0, 8.0, 7.0, 11.0, 10.0),
            createCuboidShape(2.0, 12.0, 0.0, 5.0, 15.0, 3.0)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)

        private val NORTH_SHAPE = Stream.of(
            createCuboidShape(13.0, 4.0, 14.0, 15.0, 6.0, 16.0),
            createCuboidShape(12.0, 3.0, 10.0, 13.0, 4.0, 11.0),
            createCuboidShape(11.0, 2.0, 11.0, 13.0, 4.0, 12.0),
            createCuboidShape(11.0, 2.0, 12.0, 14.0, 5.0, 16.0),
            createCuboidShape(10.0, 1.0, 15.0, 12.0, 3.0, 16.0),
            createCuboidShape(3.0, 2.0, 15.0, 5.0, 4.0, 16.0),
            createCuboidShape(1.0, 3.0, 12.0, 4.0, 6.0, 16.0),
            createCuboidShape(2.0, 4.0, 11.0, 4.0, 6.0, 12.0),
            createCuboidShape(2.0, 4.0, 10.0, 3.0, 5.0, 11.0),
            createCuboidShape(0.0, 5.0, 14.0, 2.0, 7.0, 16.0),
            createCuboidShape(6.0, 7.0, 14.0, 8.0, 10.0, 16.0),
            createCuboidShape(7.0, 8.0, 11.0, 12.0, 13.0, 16.0),
            createCuboidShape(8.0, 9.0, 8.0, 11.0, 12.0, 11.0),
            createCuboidShape(9.0, 10.0, 6.0, 10.0, 11.0, 8.0),
            createCuboidShape(11.0, 12.0, 13.0, 14.0, 15.0, 16.0)
        ).reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)

        private val WEST_SHAPE = Stream.of(
            createCuboidShape(10.0, 11.0, 2.0, 11.0, 12.0, 3.0),
            createCuboidShape(11.0, 10.0, 2.0, 12.0, 12.0, 4.0),
            createCuboidShape(12.0, 10.0, 1.0, 16.0, 13.0, 4.0),
            createCuboidShape(15.0, 12.0, 3.0, 16.0, 14.0, 5.0),
            createCuboidShape(14.0, 9.0, 0.0, 16.0, 11.0, 2.0),
            createCuboidShape(15.0, 13.0, 10.0, 16.0, 15.0, 12.0),
            createCuboidShape(12.0, 11.0, 11.0, 16.0, 14.0, 14.0),
            createCuboidShape(11.0, 12.0, 11.0, 12.0, 14.0, 13.0),
            createCuboidShape(10.0, 12.0, 12.0, 11.0, 13.0, 13.0),
            createCuboidShape(14.0, 10.0, 13.0, 16.0, 12.0, 15.0),
            createCuboidShape(14.0, 6.0, 6.0, 16.0, 9.0, 8.0),
            createCuboidShape(11.0, 3.0, 7.0, 16.0, 8.0, 12.0),
            createCuboidShape(8.0, 4.0, 8.0, 11.0, 7.0, 11.0),
            createCuboidShape(6.0, 5.0, 9.0, 8.0, 6.0, 10.0),
            createCuboidShape(13.0, 1.0, 11.0, 16.0, 4.0, 14.0)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)
    }
}