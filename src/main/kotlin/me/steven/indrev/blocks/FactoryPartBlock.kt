package me.steven.indrev.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldAccess

class FactoryPartBlock(settings: Settings) : Block(settings) {

    init {
        this.defaultState = stateManager.defaultState
            .with(CONNECTED_X, false)
            .with(CONNECTED_Y, false)
            .with(CONNECTED_Z, false)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return getConnectedState(ctx.blockPos, ctx.world)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState?,
        direction: Direction?,
        newState: BlockState?,
        world: WorldAccess,
        pos: BlockPos,
        posFrom: BlockPos?
    ): BlockState {
        return getConnectedState(pos, world)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(CONNECTED_X, CONNECTED_Y, CONNECTED_Z)
    }

    fun getConnectedState(blockPos: BlockPos, world: WorldAccess): BlockState {
        var state = defaultState
        if (isFactoryPartBlock(blockPos.offset(Direction.UP), world) && isFactoryPartBlock(blockPos.offset(Direction.DOWN), world))
            state = state.with(CONNECTED_Y, true)
        else if (isFactoryPartBlock(blockPos.offset(Direction.NORTH), world) && isFactoryPartBlock(blockPos.offset(Direction.SOUTH), world))
            state = state.with(CONNECTED_X, true)
        else if (isFactoryPartBlock(blockPos.offset(Direction.EAST), world) && isFactoryPartBlock(blockPos.offset(Direction.WEST), world))
            state = state.with(CONNECTED_Z, true)
        return state
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return when (rotation) {
            BlockRotation.NONE, BlockRotation.CLOCKWISE_180 -> state
            BlockRotation.CLOCKWISE_90, BlockRotation.COUNTERCLOCKWISE_90 -> state.with(CONNECTED_X, state[CONNECTED_Z]).with(CONNECTED_Z, state[CONNECTED_X])
        }
    }

    fun isFactoryPartBlock(pos: BlockPos, world: WorldAccess) = world.getBlockState(pos).block == this

    companion object {
        val CONNECTED_Y = BooleanProperty.of("y")
        val CONNECTED_X = BooleanProperty.of("x")
        val CONNECTED_Z = BooleanProperty.of("z")
    }
}