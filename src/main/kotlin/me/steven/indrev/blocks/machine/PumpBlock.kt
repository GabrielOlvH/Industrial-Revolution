package me.steven.indrev.blocks.machine

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.farms.PumpBlockEntity
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class PumpBlock(settings: Settings) : HorizontalFacingMachineBlock(
    settings,
    Tier.MK1,
    IndustrialRevolution.CONFIG.machines.drain,
    null,
    { PumpBlockEntity(Tier.MK1) }) {

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = when (state?.get(HORIZONTAL_FACING)) {
        Direction.NORTH -> SHAPE_NORTH
        Direction.SOUTH -> SHAPE_SOUTH
        Direction.WEST -> SHAPE_WEST
        Direction.EAST -> SHAPE_EAST
        else -> VoxelShapes.fullCube()
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(HORIZONTAL_FACING, ctx?.playerFacing)
    }

    companion object {
        private val SHAPE_NORTH = createCuboidShape(2.5, 0.0, 0.0, 14.5, 16.0, 14.5)
        private val SHAPE_SOUTH = createCuboidShape(2.5, 0.0, 2.5, 14.5, 16.0, 16.0)
        private val SHAPE_WEST = createCuboidShape(0.0, 0.0, 2.5, 14.5, 16.0, 14.5)
        private val SHAPE_EAST = createCuboidShape(2.5, 0.0, 2.5, 16.0, 16.0, 14.5)
    }
}