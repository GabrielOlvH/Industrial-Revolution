package me.steven.indrev.blocks.misc

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.math.Direction

open class VerticalFacingBlock(settings: Settings) : Block(settings) {

    init {
        this.defaultState = stateManager.defaultState.with(FACING, Direction.UP)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return defaultState.with(FACING, Direction.getEntityFacingOrder(ctx?.player).firstOrNull { it.axis.isVertical }?.opposite ?: Direction.UP)
    }

    companion object {
        val FACING = EnumProperty.of("facing", Direction::class.java, Direction.UP, Direction.DOWN)
    }
}