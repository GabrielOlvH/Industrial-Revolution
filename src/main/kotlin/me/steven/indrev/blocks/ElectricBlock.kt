package me.steven.indrev.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import team.reborn.energy.Energy

open class ElectricBlock(settings: Settings, open val maxBuffer: Double, open val blockEntityProvider: () -> ElectricBlockEntity) : Block(settings), BlockEntityProvider {
    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    fun tryProvideEnergyTo(world: World?, sourcePos: BlockPos, targetPos: BlockPos): Double {
        val sourceBlockEntity = world?.getBlockEntity(sourcePos)
        if (sourceBlockEntity == null || !Energy.valid(sourceBlockEntity)) return 0.0
        val targetBlockEntity = world.getBlockEntity(targetPos)
        if (targetBlockEntity == null || !Energy.valid(targetBlockEntity)) return 0.0
        val sourceHandler = Energy.of(sourceBlockEntity)
        val targetHandler = Energy.of(targetBlockEntity)
        return sourceHandler.into(targetHandler).move()
    }

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}