package me.steven.indrev.blocks.machine

import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.setBlockState
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
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

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

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        part.onBreak(world, pos)
        world.syncWorldEvent(player, 2001, pos, getRawIdFromState(state))
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
        world: WorldAccess?,
        pos: BlockPos?,
        posFrom: BlockPos?
    ): BlockState {
        return if (newState.block is DrillBlock) state.with(WORKING, newState[WORKING])
        else state
    }

    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack
            = ItemStack(DRILL_BOTTOM)

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(WORKING)
    }

    enum class DrillPart : StringIdentifiable {
        TOP {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.down(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_MIDDLE) }
                world.setBlockState(pos.down(2), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_BOTTOM) }
            }
            override fun getBlockEntityPos(pos: BlockPos): BlockPos = pos.down(2)
        },
        MIDDLE {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.up(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_TOP) }
                world.setBlockState(pos.down(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_BOTTOM) }
            }
            override fun getBlockEntityPos(pos: BlockPos): BlockPos = pos.down()
        },
        BOTTOM {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.up(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_MIDDLE) }
                world.setBlockState(pos.up(2), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_TOP) }
            }

            override fun getBlockEntityPos(pos: BlockPos): BlockPos = pos
        };

        abstract fun onBreak(world: World, pos: BlockPos)

        abstract fun getBlockEntityPos(pos: BlockPos): BlockPos

        override fun asString(): String = toString().toLowerCase()
    }

    class TopDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.TOP)

    class MiddleDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.MIDDLE)

    class BottomDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.BOTTOM), BlockEntityProvider {
        override fun createBlockEntity(world: BlockView?): BlockEntity = DrillBlockEntity()
    }

    companion object {
        private val DRILL_TOP by lazy { IRBlockRegistry.DRILL_TOP }
        private val DRILL_MIDDLE by lazy { IRBlockRegistry.DRILL_MIDDLE }
        private val DRILL_BOTTOM by lazy { IRBlockRegistry.DRILL_BOTTOM }
        val WORKING = BooleanProperty.of("working")
    }
}