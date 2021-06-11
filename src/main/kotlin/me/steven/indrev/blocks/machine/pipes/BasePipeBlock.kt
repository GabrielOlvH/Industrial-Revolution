package me.steven.indrev.blocks.machine.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.CoverableBlockEntity
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import me.steven.indrev.utils.toVec3d
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Property
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

abstract class BasePipeBlock(settings: Settings, val tier: Tier, val type: Network.Type<*>) : Block(settings), BlockEntityProvider {

    init {
        this.defaultState = stateManager.defaultState
            .with(NORTH, false)
            .with(SOUTH, false)
            .with(EAST, false)
            .with(WEST, false)
            .with(UP, false)
            .with(DOWN, false)
            .with(COVERED, false)
    }

    abstract fun getShape(blockState: BlockState): VoxelShape

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(
            NORTH,
            SOUTH,
            EAST,
            WEST,
            UP,
            DOWN,
            COVERED
        )
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = CoverableBlockEntity(tier, pos, state)

    override fun onBlockBreakStart(state: BlockState, world: World?, pos: BlockPos?, player: PlayerEntity?) {
        if (world?.isClient == false && state[COVERED]) {
            val blockEntity = world.getBlockEntity(pos) as? CoverableBlockEntity ?: return
            world.setBlockState(pos, state.with(COVERED, false))
            val cover = blockEntity.coverState ?: return
            ItemScatterer.spawn(world, pos, DefaultedList.ofSize(1, ItemStack(cover.block)))
            blockEntity.coverState = null
            blockEntity.markDirty()
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult): ActionResult {
        val handStack = player?.getStackInHand(hand) ?: return ActionResult.FAIL
        if (handStack.item == IRItemRegistry.WRENCH && world is ServerWorld) {
            val dir = getSideFromHit(hit.pos, pos)
            val (x, y, z) = hit.pos
            (type.getNetworkState(world) as? ServoNetworkState<*>?)?.let { networkState ->
                if (dir != null) {
                    val data = networkState.removeEndpointData(pos, dir)
                    when (data?.type) {
                        EndpointData.Type.OUTPUT ->
                            ItemScatterer.spawn(world, x, y, z, ItemStack(IRItemRegistry.SERVO_OUTPUT))
                        EndpointData.Type.RETRIEVER ->
                            ItemScatterer.spawn(world, x, y, z, ItemStack(IRItemRegistry.SERVO_RETRIEVER))
                        else -> return@let
                    }
                    return ActionResult.CONSUME
                }
            }
        }
        val item = handStack.item
        if (!state[COVERED] && !handStack.isEmpty && !player.isSneaking) {
            val blockEntity = world.getBlockEntity(pos) as? CoverableBlockEntity ?: return ActionResult.FAIL
            if (item is BlockItem && item.block !is BlockEntityProvider && item.block.defaultState.isFullCube(world, pos)) {
                val result = item.block.getPlacementState(ItemPlacementContext(player, hand, handStack, hit))
                blockEntity.coverState = result
                blockEntity.markDirty()
                world.setBlockState(pos, state.with(COVERED, true))
                if (!player.abilities.creativeMode)
                    handStack.decrement(1)
                return ActionResult.SUCCESS
            }
        }
        return ActionResult.FAIL
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        val world = ctx?.world
        if (world !is ServerWorld) return defaultState
        var state = defaultState
        val blockPos = ctx.blockPos
        for (direction in Direction.values()) {
            state = state.with(getProperty(direction), isConnectable(world, blockPos.offset(direction), direction.opposite))
        }
        return state
    }

    @Suppress("DEPRECATION")
    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        super.onStateReplaced(state, world, pos, newState, moved)
        if (!world.isClient) {
            if (state.isOf(newState.block)) {
                Network.handleUpdate(type, pos)
            } else {
                (type.getNetworkState(world as ServerWorld) as? ServoNetworkState<*>?)?.let { networkState ->
                    Direction.values().forEach { dir ->
                        val data = networkState.removeEndpointData(pos, dir)
                        val (x, y, z) = pos.toVec3d()
                        when (data?.type) {
                            EndpointData.Type.OUTPUT ->
                                ItemScatterer.spawn(world, x, y, z, ItemStack(IRItemRegistry.SERVO_OUTPUT))
                            EndpointData.Type.RETRIEVER ->
                                ItemScatterer.spawn(world, x, y, z, ItemStack(IRItemRegistry.SERVO_RETRIEVER))
                            else -> {}
                        }
                    }
                }

                Network.handleBreak(type, pos)
            }
        }
    }

    abstract fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction): Boolean

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (!world.isClient) {
            Network.handleUpdate(type, pos)
        }
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        facing: Direction,
        neighborState: BlockState?,
        world: WorldAccess?,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        val (x, y, z) = pos.subtract(neighborPos)
        return if (world is ServerWorld)
            state.with(getProperty(facing), isConnectable(world, neighborPos, Direction.fromVector(x, y, z)!!))
        else state
    }

    data class PipeShape(val directions: Array<Direction>, val shape: VoxelShape) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PipeShape

            if (!directions.contentEquals(other.directions)) return false
            if (shape != other.shape) return false

            return true
        }

        override fun hashCode(): Int {
            var result = directions.contentHashCode()
            result = 31 * result + shape.hashCode()
            return result
        }
    }

    companion object {

        val NORTH: BooleanProperty = BooleanProperty.of("north")
        val SOUTH: BooleanProperty = BooleanProperty.of("south")
        val EAST: BooleanProperty = BooleanProperty.of("east")
        val WEST: BooleanProperty = BooleanProperty.of("west")
        val UP: BooleanProperty = BooleanProperty.of("up")
        val DOWN: BooleanProperty = BooleanProperty.of("down")

        val COVERED: BooleanProperty = BooleanProperty.of("covered")

        fun getProperty(facing: Direction): Property<Boolean> {
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

        fun getSideFromHit(hit: Vec3d, pos: BlockPos): Direction? {
            val x = hit.x - pos.x
            val y = hit.y - pos.y
            val z = hit.z - pos.z
            return when {
                y > 0.6625 -> Direction.UP
                y < 0.3375 -> Direction.DOWN
                x > 0.6793 -> Direction.EAST
                x < 0.3169 -> Direction.WEST
                z < 0.3169 -> Direction.NORTH
                z > 0.6625 -> Direction.SOUTH
                else -> null
            }
        }
    }
}