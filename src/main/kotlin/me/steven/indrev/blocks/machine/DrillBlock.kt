package me.steven.indrev.blocks.machine

import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.utils.setBlockState
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class DrillBlock private constructor(settings: Settings, private val part: DrillPart) : Block(settings) {

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

    enum class DrillPart : StringIdentifiable {
        TOP {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.down(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_MIDDLE) }
                world.setBlockState(pos.down(2), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_BOTTOM) }
            }
        },
        MIDDLE {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.up(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_TOP) }
                world.setBlockState(pos.down(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_BOTTOM) }
            }
        },
        BOTTOM {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.up(), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_MIDDLE) }
                world.setBlockState(pos.up(2), Blocks.AIR.defaultState) { oldState -> oldState.isOf(DRILL_TOP) }
            }
        };

        abstract fun onBreak(world: World, pos: BlockPos)

        override fun asString(): String = toString().toLowerCase()
    }

    class TopDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.TOP)

    class MiddleDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.MIDDLE)

    class BottomDrillBlock(settings: Settings) : DrillBlock(settings, DrillPart.BOTTOM)

    companion object {
        private val DRILL_TOP by lazy { IRRegistry.DRILL_TOP }
        private val DRILL_MIDDLE by lazy { IRRegistry.DRILL_MIDDLE }
        private val DRILL_BOTTOM by lazy { IRRegistry.DRILL_BOTTOM }
    }
}