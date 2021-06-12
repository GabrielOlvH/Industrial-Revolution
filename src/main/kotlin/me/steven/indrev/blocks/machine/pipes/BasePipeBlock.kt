package me.steven.indrev.blocks.machine.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.BasePipeBlockEntity
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
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

abstract class BasePipeBlock(settings: Settings, val tier: Tier, val type: Network.Type<*>) : Block(settings), BlockEntityProvider {

    init {
        this.defaultState = stateManager.defaultState
    }

    abstract fun getShape(blockEntity: BasePipeBlockEntity): VoxelShape

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = BasePipeBlockEntity(tier, pos, state)

    override fun getOutlineShape(
        state: BlockState,
        view: BlockView,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        val blockEntity = view.getBlockEntity(pos) as? BasePipeBlockEntity ?: return VoxelShapes.empty()
        return if (blockEntity.coverState != null) VoxelShapes.fullCube()
        else getShape(blockEntity)
    }

    override fun hasDynamicBounds(): Boolean = true

    override fun onBlockBreakStart(state: BlockState, world: World?, pos: BlockPos?, player: PlayerEntity?) {
        if (world?.isClient == false) {
            val blockEntity = world.getBlockEntity(pos) as? BasePipeBlockEntity ?: return
            if (blockEntity.coverState != null) {
                val cover = blockEntity.coverState ?: return
                //TODO replace ItemStack() with getDroppedStacks
                ItemScatterer.spawn(world, pos, DefaultedList.ofSize(1, ItemStack(cover.block)))
                blockEntity.coverState = null
                blockEntity.markDirty()
            }
        }
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult): ActionResult {
        val handStack = player?.getStackInHand(hand) ?: return ActionResult.FAIL
        val blockEntity = world.getBlockEntity(pos) as? BasePipeBlockEntity ?: return ActionResult.FAIL
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

            if (dir != null && blockEntity.connections[dir]!!.isConnected())
                blockEntity.connections[dir] = ConnectionType.WRENCHED
            else
                blockEntity.connections[hit.side] =
                    if (isConnectable(world, pos, hit.side)) ConnectionType.CONNECTED else ConnectionType.NONE
            blockEntity.markDirty()
            blockEntity.sync()
            world.updateNeighbors(pos, state.block)
            Network.handleUpdate(type, pos)
        }
        val item = handStack.item
        if (blockEntity.coverState != null && !handStack.isEmpty && !player.isSneaking) {
            if (item is BlockItem && item.block !is BlockEntityProvider && item.block.defaultState.isFullCube(world, pos)) {
                val result = item.block.getPlacementState(ItemPlacementContext(player, hand, handStack, hit))
                blockEntity.coverState = result
                blockEntity.markDirty()
                if (!player.abilities.creativeMode)
                    handStack.decrement(1)
                return ActionResult.SUCCESS
            }
        }
        return ActionResult.FAIL
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
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (!world.isClient) {
            DIRECTIONS.forEach { facing ->
                updateConnection(world as ServerWorld, pos, pos.offset(facing), facing)
            }

            Network.handleUpdate(type, pos)
        }
    }

    override fun neighborUpdate(
        state: BlockState,
        world: World?,
        pos: BlockPos,
        block: Block?,
        fromPos: BlockPos,
        notify: Boolean
    ) {
        val (x, y, z) = pos.subtract(fromPos)
        val facing = Direction.fromVector(x, y, z)!!.opposite
        if (world is ServerWorld) {
            updateConnection(world, pos, fromPos, facing)
        }
    }

    private fun updateConnection(world: ServerWorld, pos: BlockPos, neighborPos: BlockPos, facing: Direction) {
        val blockEntity = world.getBlockEntity(pos) as? BasePipeBlockEntity ?: return
        val before = blockEntity.connections[facing]
        val new = ConnectionType.getType(isConnectable(world, neighborPos, facing))
        val neighborBlockEntity = world.getBlockEntity(neighborPos) as? BasePipeBlockEntity
        if (before != new && (before != ConnectionType.WRENCHED || neighborBlockEntity != null
                    && neighborBlockEntity.connections[facing.opposite] == ConnectionType.CONNECTED)) {
            blockEntity.connections[facing] = new
            blockEntity.markDirty()
            blockEntity.sync()
            Network.handleUpdate(type, pos)
        }
    }

    enum class ConnectionType(val id: Int) {
        NONE(-1), CONNECTED(0), WRENCHED(1);

        fun isConnected() = this == CONNECTED

        fun isConnectable() = this != WRENCHED

        companion object {
            fun getType(connects: Boolean) = if (connects) CONNECTED else NONE

            fun byId(id: Int): ConnectionType {
                return when (id) {
                    0 -> CONNECTED
                    1 -> WRENCHED
                    else -> NONE
                }
            }
        }
    }

    companion object {

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