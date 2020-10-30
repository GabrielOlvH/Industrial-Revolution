package me.steven.indrev.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

class WarningStrobeBlock(settings: Settings) : Block(settings) {
    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = SHAPE

    override fun canPlaceAt(state: BlockState?, world: WorldView, pos: BlockPos): Boolean {
        val blockBelow = world.getBlockState(pos.down())
        return isFaceFullSquare(blockBelow.getCollisionShape(world, pos.down()), Direction.UP)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction?,
        newState: BlockState?,
        world: WorldAccess?,
        pos: BlockPos?,
        posFrom: BlockPos?
    ): BlockState? {
        return if (!state.canPlaceAt(world, pos)) Blocks.AIR.defaultState else state
    }

    companion object {
        private val SHAPE = createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0)
    }
}