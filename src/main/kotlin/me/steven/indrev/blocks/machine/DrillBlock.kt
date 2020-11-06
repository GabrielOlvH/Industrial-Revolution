package me.steven.indrev.blocks.machine

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DrillBlock(settings: Settings) : Block(settings) {

    init {
        this.defaultState = stateManager.defaultState.with(PART, DrillPart.BOTTOM)
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
        val defaultState = state.block.defaultState
        world.setBlockState(pos.up().up(), defaultState.with(PART, DrillPart.TOP))
        world.setBlockState(pos.up(), defaultState.with(PART, DrillPart.MIDDLE))
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity?) {
        state[PART].onBreak(world, pos)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(PART)
    }

    enum class DrillPart : StringIdentifiable {
        TOP {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.down(), Blocks.AIR.defaultState)
                world.setBlockState(pos.down().down(), Blocks.AIR.defaultState)
            }
        },
        MIDDLE {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.up(),  Blocks.AIR.defaultState)
                world.setBlockState(pos.down(), Blocks.AIR.defaultState)
            }
        },
        BOTTOM {
            override fun onBreak(world: World, pos: BlockPos) {
                world.setBlockState(pos.up(), Blocks.AIR.defaultState)
                world.setBlockState(pos.up().up(), Blocks.AIR.defaultState)
            }
        };

        abstract fun onBreak(world: World, pos: BlockPos)

        override fun asString(): String = toString().toLowerCase()
    }

    companion object {
        val PART = EnumProperty.of("part", DrillPart::class.java)
    }
}