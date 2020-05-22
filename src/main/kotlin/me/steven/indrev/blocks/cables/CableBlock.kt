package me.steven.indrev.blocks.cables

import me.steven.indrev.blocks.BasicMachineBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.AbstractProperty
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import team.reborn.energy.Energy

class CableBlock(settings: Settings) : BasicMachineBlock(settings, { CableBlockEntity() }) {

    init {
        this.defaultState = stateManager.defaultState
                .with(NORTH, false).with(SOUTH, false)
                .with(EAST, false).with(WEST, false)
                .with(UP, false).with(DOWN, false)
    }

    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, context: EntityContext?): VoxelShape = CENTER_SHAPE

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(NORTH, SOUTH, EAST, WEST, UP, DOWN)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        var state = defaultState
        val blockPos = ctx?.blockPos ?: return state
        for (direction in Direction.values()) {
            val neighbor = ctx.world.getBlockEntity(blockPos.offset(direction)) ?: continue
            if (Energy.valid(neighbor)) state = state.with(getProperty(direction), true)
        }
        return state
    }

    override fun getStateForNeighborUpdate(state: BlockState, facing: Direction, neighborState: BlockState?, world: IWorld?, pos: BlockPos?, neighborPos: BlockPos?): BlockState {
        val neighborBlockEntity = world?.getBlockEntity(neighborPos)
        return if (neighborBlockEntity == null || !Energy.valid(neighborBlockEntity)) state.with(getProperty(facing), false)
        else state.with(getProperty(facing), true)
    }

    companion object {

        val CENTER_SHAPE: VoxelShape = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)

        val NORTH: BooleanProperty = BooleanProperty.of("north")
        val SOUTH: BooleanProperty = BooleanProperty.of("south")
        val EAST: BooleanProperty = BooleanProperty.of("east")
        val WEST: BooleanProperty = BooleanProperty.of("west")
        val UP: BooleanProperty = BooleanProperty.of("up")
        val DOWN: BooleanProperty = BooleanProperty.of("down")


        fun getProperty(facing: Direction?): AbstractProperty<Boolean> {
            return when (facing) {
                Direction.EAST -> EAST
                Direction.WEST -> WEST
                Direction.NORTH -> NORTH
                Direction.SOUTH -> SOUTH
                Direction.UP -> UP
                Direction.DOWN -> DOWN
                else -> EAST
            }
        }
    }
}