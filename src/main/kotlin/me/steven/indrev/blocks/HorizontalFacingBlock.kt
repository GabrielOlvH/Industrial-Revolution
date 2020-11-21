package me.steven.indrev.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.util.math.Direction

open class HorizontalFacingBlock(settings: Settings) : HorizontalFacingBlock(settings) {
    init {
        this.defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return defaultState.with(FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }
}