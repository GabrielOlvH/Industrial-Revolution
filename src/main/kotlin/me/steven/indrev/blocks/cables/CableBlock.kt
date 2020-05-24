package me.steven.indrev.blocks.cables

import me.steven.indrev.blocks.BasicMachineBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.AbstractProperty
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.DefaultedList
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World
import team.reborn.energy.Energy

class CableBlock(settings: Settings) : BasicMachineBlock(settings, { CableBlockEntity() }) {

    init {
        this.defaultState = stateManager.defaultState
                .with(NORTH, false).with(SOUTH, false)
                .with(EAST, false).with(WEST, false)
                .with(UP, false).with(DOWN, false)
                .with(COVERED, false)
    }

    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, context: EntityContext?): VoxelShape {
        return if (state != null && state[COVERED]) VoxelShapes.fullCube() else CENTER_SHAPE
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, COVERED)
    }

    override fun onBlockBreakStart(state: BlockState, world: World?, pos: BlockPos?, player: PlayerEntity?) {
        if (world?.isClient == false && state[COVERED]) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity !is CableBlockEntity) return
            world.setBlockState(pos, state.with(COVERED, false))
            val coverId = blockEntity.cover
            val block = Registry.BLOCK.get(coverId).asItem()
            ItemScatterer.spawn(world, pos, DefaultedList.ofSize(1, ItemStack(block)))
            blockEntity.cover = null
            blockEntity.markDirty()
        }
    }

    override fun onUse(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        val handStack = player?.getStackInHand(hand) ?: return ActionResult.FAIL
        if (state?.get(COVERED) == false && !handStack.isEmpty) {
            val blockEntity = world?.getBlockEntity(pos)
            if (blockEntity !is CableBlockEntity) return ActionResult.FAIL
            val id = Registry.ITEM.getId(handStack.item)
            if (!Registry.BLOCK.containsId(id)) return ActionResult.FAIL
            val block = Registry.BLOCK.get(id)
            if (block is BlockEntityProvider || !block.defaultState.isFullCube(world, pos)) return ActionResult.FAIL
            blockEntity.cover = id
            blockEntity.markDirty()
            world.setBlockState(pos, state.with(COVERED, true))
            handStack.count--
            return ActionResult.SUCCESS
        }
        return ActionResult.FAIL
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

        val COVERED: BooleanProperty = BooleanProperty.of("covered")

        fun getProperty(facing: Direction): AbstractProperty<Boolean> {
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