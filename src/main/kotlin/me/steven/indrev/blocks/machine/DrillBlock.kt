package me.steven.indrev.blocks.machine

import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

open class DrillBlock private constructor(settings: Settings, val part: DrillPart) : Block(settings) {

    init {
        this.defaultState = stateManager.defaultState.with(WORKING, false)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val middle = ctx.world.getBlockState(ctx.blockPos.up())
        val top = ctx.world.getBlockState(ctx.blockPos.up(2))
        return if (middle.canReplace(ctx) && top.canReplace(ctx)) defaultState else null
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        world.setBlockState(pos.up(2), DRILL_TOP.defaultState)
        world.setBlockState(pos.up(), DRILL_MIDDLE.defaultState)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient) {
            val blockEntity = world.getBlockEntity(part.getBlockEntityPos(pos)) as? DrillBlockEntity ?: return ActionResult.PASS
            player?.openHandledScreen(blockEntity)
        }
        return ActionResult.CONSUME
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction?,
        newState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        posFrom: BlockPos?
    ): BlockState {
        return if (newState.block is DrillBlock) state.with(WORKING, newState[WORKING])
        else if (!part.test(world, pos)) Blocks.AIR.defaultState
        else state
    }

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack = ItemStack(DRILL_BOTTOM)

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(WORKING)
    }

    enum class DrillPart : StringIdentifiable {
        TOP {
            override fun test(world: WorldView, pos: BlockPos): Boolean {
                return world.getBlockState(pos.down()).isOf(DRILL_MIDDLE)
                        && world.getBlockState(pos.down(2)).isOf(DRILL_BOTTOM)
            }

            override fun getBlockEntityPos(pos: BlockPos): BlockPos = pos.down(2)
        },
        MIDDLE {
            override fun test(world: WorldView, pos: BlockPos): Boolean {
                return world.getBlockState(pos.up()).isOf(DRILL_TOP)
                        && world.getBlockState(pos.down()).isOf(DRILL_BOTTOM)
            }
            override fun getBlockEntityPos(pos: BlockPos): BlockPos = pos.down()
        },
        BOTTOM {
            override fun test(world: WorldView, pos: BlockPos): Boolean {
                return world.getBlockState(pos.up()).isOf(DRILL_MIDDLE)
                        && world.getBlockState(pos.up(2)).isOf(DRILL_TOP)
            }

            override fun getBlockEntityPos(pos: BlockPos): BlockPos = pos
        };

        abstract fun test(world: WorldView, pos: BlockPos): Boolean

        abstract fun getBlockEntityPos(pos: BlockPos): BlockPos

        override fun asString(): String = toString().toLowerCase()
    }

    class TopDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.TOP)

    class MiddleDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.MIDDLE)

    class BottomDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.BOTTOM), BlockEntityProvider {
        override fun createBlockEntity(world: BlockView?): BlockEntity = DrillBlockEntity()

        override fun onStateReplaced(
            state: BlockState,
            world: World?,
            pos: BlockPos?,
            newState: BlockState,
            moved: Boolean
        ) {

            if (!newState.isOf(this)) {
                (world?.getBlockEntity(pos) as? DrillBlockEntity)?.let {
                    ItemScatterer.spawn(world, pos, it)
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    companion object {
        private val DRILL_TOP by lazy { IRBlockRegistry.DRILL_TOP }
        private val DRILL_MIDDLE by lazy { IRBlockRegistry.DRILL_MIDDLE }
        private val DRILL_BOTTOM by lazy { IRBlockRegistry.DRILL_BOTTOM }
        val WORKING = BooleanProperty.of("working")
    }
}