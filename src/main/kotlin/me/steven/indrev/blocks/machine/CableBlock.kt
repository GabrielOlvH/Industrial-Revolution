package me.steven.indrev.blocks.machine

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.energy.EnergyNetwork
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Property
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import team.reborn.energy.Energy

class CableBlock(settings: Settings, private val tier: Tier) : Block(settings), BlockEntityProvider {

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

    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxInput").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.lftick", getConfig().maxInput).formatted(Formatting.GRAY))
        )
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxOutput").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.lftick", getConfig().maxOutput).formatted(Formatting.GRAY))
        )
    }

    override fun getOutlineShape(
        state: BlockState,
        view: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return if (state[COVERED]) VoxelShapes.fullCube()
        else getShape(state)
    }

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

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        if (player?.isSneaking == true) return super.onUse(state, world, pos, player, hand, hit)!!
        val handStack = player?.getStackInHand(hand) ?: return ActionResult.FAIL
        if (state?.get(COVERED) == false && !handStack.isEmpty) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity !is CableBlockEntity) return ActionResult.FAIL
            val id = Registry.ITEM.getId(handStack.item)
            if (!Registry.BLOCK.getOrEmpty(id).isPresent) return ActionResult.FAIL
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
            state = state.with(getProperty(direction), isConnectable(neighbor))
        }
        return state
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        super.onStateReplaced(state, world, pos, newState, moved)
        if (!world.isClient) {
            if (state.isOf(newState.block))
                EnergyNetwork.handleUpdate(world as ServerWorld, pos)
            else
                EnergyNetwork.handleBreak(world as ServerWorld, pos)
        }
    }

    private fun isConnectable(blockEntity: BlockEntity?) =
        blockEntity != null && Energy.valid(blockEntity) || (blockEntity is CableBlockEntity && tier == blockEntity.tier)

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (!world.isClient) {
            EnergyNetwork.handleUpdate(world as ServerWorld, pos)
        }
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        facing: Direction,
        neighborState: BlockState?,
        world: WorldAccess?,
        pos: BlockPos?,
        neighborPos: BlockPos?
    ): BlockState {
        val neighborBlockEntity = world?.getBlockEntity(neighborPos)
        return state.with(getProperty(facing), isConnectable(neighborBlockEntity))
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity? = CableBlockEntity(tier)

    fun getConfig() = when(tier) {
        Tier.MK1 -> IndustrialRevolution.CONFIG.cables.cableMk1
        Tier.MK2 -> IndustrialRevolution.CONFIG.cables.cableMk2
        Tier.MK3 -> IndustrialRevolution.CONFIG.cables.cableMk3
        else -> IndustrialRevolution.CONFIG.cables.cableMk4
    }

    data class CableShape(val directions: Array<Direction>, val shape: VoxelShape) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CableShape

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

        val CENTER_SHAPE: VoxelShape = createCuboidShape(5.5, 5.5, 5.5, 10.5, 10.5, 10.5)
        val DOWN_SHAPE: VoxelShape = createCuboidShape(6.0, 0.0, 6.0, 10.0, 6.0, 10.0)
        val UP_SHAPE: VoxelShape = createCuboidShape(6.0, 10.5, 6.0, 10.0, 16.0, 10.0)
        val SOUTH_SHAPE: VoxelShape = createCuboidShape(6.0, 6.0, 10.5, 10.0, 10.0, 16.0)
        val NORTH_SHAPE: VoxelShape = createCuboidShape(6.0, 6.0, 5.5, 10.0, 10.0, 0.0)
        val EAST_SHAPE: VoxelShape = createCuboidShape(10.5, 6.0, 6.0, 16.0, 10.0, 10.0)
        val WEST_SHAPE: VoxelShape = createCuboidShape(0.0, 6.0, 6.0, 5.5, 10.0, 10.0)

        val NORTH: BooleanProperty = BooleanProperty.of("north")
        val SOUTH: BooleanProperty = BooleanProperty.of("south")
        val EAST: BooleanProperty = BooleanProperty.of("east")
        val WEST: BooleanProperty = BooleanProperty.of("west")
        val UP: BooleanProperty = BooleanProperty.of("up")
        val DOWN: BooleanProperty = BooleanProperty.of("down")

        val COVERED: BooleanProperty = BooleanProperty.of("covered")

        fun getShape(direction: Direction): VoxelShape {
            var shape = VoxelShapes.empty()
            if (direction == Direction.NORTH) shape = NORTH_SHAPE
            if (direction == Direction.SOUTH) shape = SOUTH_SHAPE
            if (direction == Direction.EAST) shape = EAST_SHAPE
            if (direction == Direction.WEST) shape = WEST_SHAPE
            if (direction == Direction.UP) shape = UP_SHAPE
            if (direction == Direction.DOWN) shape = DOWN_SHAPE
            return shape
        }

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

        private val SHAPE_CACHE = hashSetOf<CableShape>()
        private fun getShape(state: BlockState): VoxelShape {
            val directions = Direction.values().filter { dir -> state[getProperty(dir)] }.toTypedArray()
            var cableShapeCache = SHAPE_CACHE.firstOrNull { shape -> shape.directions.contentEquals(directions) }
            if (cableShapeCache == null) {
                var shape = CENTER_SHAPE
                Direction.values().forEach { direction ->
                    if (state[getProperty(direction)]) shape = VoxelShapes.union(shape, getShape(direction))
                }
                cableShapeCache = CableShape(directions, shape)
                SHAPE_CACHE.add(cableShapeCache)
            }
            return cableShapeCache.shape
        }
    }
}