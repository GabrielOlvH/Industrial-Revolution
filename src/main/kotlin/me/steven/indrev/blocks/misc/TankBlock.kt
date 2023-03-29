package me.steven.indrev.blocks.misc

import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.blockentities.storage.TankBlockEntity
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.chunk.Chunk
import java.util.stream.Stream

class TankBlock(settings: Settings) : Block(settings), BlockEntityProvider {

    init {
        this.defaultState = stateManager.defaultState.with(UP, false).with(DOWN, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(UP, DOWN)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = TankBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (world.isClient) null
        else BlockEntityTicker { world, pos, state, blockEntity -> TankBlockEntity.tick(world, pos, state, blockEntity as TankBlockEntity) }
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        val isDown = state[DOWN]
        val isUp = state[UP]
        return when {
            !isUp && !isDown -> SINGLE_TANK_SHAPE
            isUp && !isDown -> TANK_UP_SHAPE
            !isUp && isDown -> TANK_DOWN_SHAPE
            else -> TANK_BOTH_SHAPE
        }
    }

    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world is ServerWorld) {
            val storage = fluidStorageOf(world, pos, hit.side)
            val inHand = ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM)
            var res = StorageUtil.move(storage, inHand, { true }, Long.MAX_VALUE, null)
            if (res == 0L)
                res = StorageUtil.move(inHand, storage, { true }, Long.MAX_VALUE, null)
            return if (res > 0) ActionResult.SUCCESS else ActionResult.PASS
        }
        return ActionResult.CONSUME
    }

    override fun afterBreak(
        world: World?,
        player: PlayerEntity?,
        pos: BlockPos?,
        state: BlockState?,
        blockEntity: BlockEntity?,
        toolStack: ItemStack?
    ) {
        if (world?.isClient == true) return
        player?.incrementStat(Stats.MINED.getOrCreateStat(this))
        player?.addExhaustion(0.005f)
        writeNbtComponents(world, player, pos, state, blockEntity, toolStack)
    }

    override fun onStateReplaced(
        state: BlockState?,
        world: World,
        pos: BlockPos,
        newState: BlockState?,
        moved: Boolean
    ) {
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    fun writeNbtComponents(
        world: World?,
        player: PlayerEntity?,
        pos: BlockPos?,
        state: BlockState?,
        blockEntity: BlockEntity?,
        toolStack: ItemStack?
    ) {
        if (world is ServerWorld) {
            getDroppedStacks(state, world, pos, blockEntity, player, toolStack).forEach { stack ->
                if (blockEntity is TankBlockEntity) {
                    val tag = stack.orCreateNbt
                    blockEntity.fluidComponent.toTag(tag)
                }
                dropStack(world, pos, stack)
            }
            state!!.onStacksDropped(world, pos, toolStack, true)
        }
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction?,
        newState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        posFrom: BlockPos
    ): BlockState {
        if (world.isClient) return state
        val connects = isConnectable(world as World, pos, posFrom)
        return when (direction) {
            Direction.UP -> state.with(UP, newState.isOf(this) && newState[DOWN] && connects)
            Direction.DOWN -> state.with(DOWN, newState.isOf(this) && newState[UP] && connects)
            else -> state
        }
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        val tag = itemStack?.nbt
        if (world.isClient) return
        val tankEntity = world.getBlockEntity(pos) as? TankBlockEntity ?: return
        if (tag?.isEmpty == false) {
            tankEntity.fluidComponent.fromTag(tag)
        }
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        val tag = stack?.nbt
        val tanksTag = tag?.getCompound("tanks") ?: return
        val volume = tanksTag.keys?.map { key ->
            val tankTag = tanksTag.getCompound(key).getCompound("fluids")
            IRFluidAmount(FluidVariant.fromNbt(tankTag.getCompound("variant")), tankTag.getLong("amt"))
        }?.firstOrNull() ?: return
        tooltip?.addAll(getTooltip(volume.resource, volume.amount, bucket*8))
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val blockPos = ctx.blockPos
        val world = ctx.world
        val fluidComponent = FluidComponent({ object : Syncable{
            override fun markForUpdate(condition: () -> Boolean) {
                TODO("Not yet implemented")
            }
        } }, bucket, 1)
        if (ctx.stack.nbt != null && !ctx.stack.nbt!!.isEmpty)
            fluidComponent.fromTag(ctx.stack.nbt)
        val connectsUp = isConnectable(world, fluidComponent, blockPos.up())
        val connectsDown = isConnectable(world, fluidComponent, blockPos.down())
        return defaultState
            .with(UP, connectsUp && (!connectsDown || isConnectable(world, blockPos.down(), blockPos.up())))
            .with(DOWN, connectsDown && (!connectsUp || isConnectable(world, blockPos.up(), blockPos.down())))
    }

    private fun isConnectable(world: World, pos: BlockPos, other: BlockPos): Boolean {
        if (world.isClient) return false
        val firstInv = fluidStorageOf(world, pos, Direction.UP) as? TankBlockEntity.CombinedTankStorage ?: return false
        val secondInv = fluidStorageOf(world, pos, Direction.DOWN)  as? TankBlockEntity.CombinedTankStorage ?: return false
        return if (firstInv.initialFluid.isBlank || secondInv.initialFluid.isBlank) true
        else firstInv.initialFluid == secondInv.initialFluid
    }

    private fun isConnectable(world: World, firstInv: FluidComponent, other: BlockPos): Boolean {
        if (world.isClient) return false
        val secondInv = fluidStorageOf(world, other, Direction.UP)  as? TankBlockEntity.CombinedTankStorage ?: return false
        return if (firstInv[0].isEmpty || secondInv.initialFluid.isBlank) true
        else firstInv[0].variant == secondInv.initialFluid
    }

    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack {
        val stack = super.getPickStack(world, pos, state)
        val blockEntity = world?.getBlockEntity(pos) as? TankBlockEntity ?: return stack
        blockEntity.fluidComponent.toTag(stack.orCreateNbt)
        return stack
    }

    companion object {

        val UP: BooleanProperty = BooleanProperty.of("up")
        val DOWN: BooleanProperty = BooleanProperty.of("down")
        
        val SINGLE_TANK_SHAPE = Stream.of(
            createCuboidShape(1.0, 14.0, 1.0, 15.0, 15.0, 15.0),
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            createCuboidShape(14.0, 1.0, 14.0, 15.0, 14.0, 15.0),
            createCuboidShape(1.0, 1.0, 14.0, 2.0, 14.0, 15.0),
            createCuboidShape(1.0, 1.0, 1.0, 2.0, 14.0, 2.0),
            createCuboidShape(14.0, 1.0, 1.0, 15.0, 14.0, 2.0),
            createCuboidShape(14.5, 1.0, 1.5, 14.7, 14.0, 14.5),
            createCuboidShape(1.5, 1.0, 1.5, 1.7, 14.0, 14.5),
            createCuboidShape(1.5, 1.0, 1.5, 14.5, 14.0, 1.7),
            createCuboidShape(1.5, 1.0, 14.5, 14.5, 14.0, 14.7)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        val TANK_DOWN_SHAPE = Stream.of(
            createCuboidShape(1.0, 14.0, 1.0, 15.0, 15.0, 15.0),
            createCuboidShape(14.0, 0.0, 14.0, 15.0, 15.0, 15.0),
            createCuboidShape(1.0, 0.0, 14.0, 2.0, 15.0, 15.0),
            createCuboidShape(1.0, 0.0, 1.0, 2.0, 15.0, 2.0),
            createCuboidShape(14.0, 0.0, 1.0, 15.0, 15.0, 2.0),
            createCuboidShape(14.5, 0.0, 1.5, 14.7, 15.0, 14.5),
            createCuboidShape(1.5, 0.0, 1.5, 1.7, 15.0, 14.5),
            createCuboidShape(1.5, 0.0, 1.5, 14.5, 15.0, 1.7),
            createCuboidShape(1.5, 0.0, 14.5, 14.5, 15.0, 14.7)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        val TANK_UP_SHAPE = Stream.of(
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            createCuboidShape(14.0, 1.0, 14.0, 15.0, 16.0, 15.0),
            createCuboidShape(1.0, 1.0, 14.0, 2.0, 16.0, 15.0),
            createCuboidShape(1.0, 1.0, 1.0, 2.0, 16.0, 2.0),
            createCuboidShape(1.04, 1.0, 1.0, 15.0, 16.0, 2.0),
            createCuboidShape(14.5, 1.0, 1.5, 14.7, 16.0, 14.5),
            createCuboidShape(1.5, 1.0, 1.5, 1.7, 16.0, 14.5),
            createCuboidShape(1.5, 1.0, 1.5, 14.5, 16.0, 1.7),
            createCuboidShape(1.5, 1.0, 14.5, 14.5, 16.0, 14.7)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        val TANK_BOTH_SHAPE = Stream.of(
            createCuboidShape(14.0, 0.0, 14.0, 15.0, 16.0, 15.0),
            createCuboidShape(1.0, 0.0, 14.0, 2.0, 16.0, 15.0),
            createCuboidShape(1.0, 0.0, 1.0, 2.0, 16.0, 2.0),
            createCuboidShape(14.0, 0.0, 1.0, 15.0, 16.0, 2.0),
            createCuboidShape(14.5, 0.0, 1.5, 14.7, 16.0, 14.5),
            createCuboidShape(1.5, 0.0, 1.5, 1.7, 16.0, 14.5),
            createCuboidShape(1.5, 0.0, 1.5, 14.5, 16.0, 1.7),
            createCuboidShape(1.5, 0.0, 14.5, 14.5, 16.0, 14.7)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        fun findAllTanks(chunk: Chunk, blockState: BlockState, pos: BlockPos, scanned: MutableSet<BlockPos>, to: TankBlockEntity.CombinedTankStorage) {
            if (blockState.isOf(IRBlockRegistry.TANK_BLOCK) && scanned.add(pos)) {
                to.add((chunk.getBlockEntity(pos) as TankBlockEntity))

                if (blockState[UP])
                    findAllTanks(chunk, chunk.getBlockState(pos.up()), pos.up(), scanned, to)
                if (blockState[DOWN])
                    findAllTanks(chunk, chunk.getBlockState(pos.down()), pos.down(), scanned, to)
            }
        }
    }
}