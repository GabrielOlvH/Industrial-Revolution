package me.steven.indrev.blocks

import me.steven.indrev.blockentities.BasicMachineBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.world.BlockView

open class BasicMachineBlock(settings: Settings, val blockEntityProvider: () -> BasicMachineBlockEntity) : Block(settings), BlockEntityProvider {

    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}