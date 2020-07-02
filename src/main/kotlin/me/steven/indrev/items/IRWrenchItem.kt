package me.steven.indrev.items

import me.steven.indrev.blocks.CableBlock
import me.steven.indrev.blocks.FacingMachineBlock
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.blocks.VerticalFacingMachineBlock
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction

class IRWrenchItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        val world = context?.world
        if (world?.isClient == true) return super.useOnBlock(context)
        val stack = context?.stack
        val pos = context?.blockPos
        var state = world?.getBlockState(pos)
        if (state?.block is CableBlock && context?.player?.isSneaking == false) {
            val side = context.side
            val property = CableBlock.getProperty(side)
            state = state.with(property, !state[property])
            world?.setBlockState(pos, state)
            stack?.damage(1, context.player) { p -> p?.sendToolBreakStatus(context.hand) }
            return ActionResult.SUCCESS
        } else if (state?.block is MachineBlock) {
            when {
                context?.player?.isSneaking == true -> {
                    world?.breakBlock(pos, true, context.player)
                }
                state.contains(VERTICAL_FACING) -> {
                    val facing = state[VERTICAL_FACING]
                    val rotated = if (facing.ordinal + 1 >= ALL.size) 0 else facing.ordinal + 1
                    state = state.with(VERTICAL_FACING, ALL[rotated])
                    world?.setBlockState(pos, state)
                }
                state.contains(HORIZONTAL_FACING) -> {
                    val facing = state[HORIZONTAL_FACING]
                    val rotated = if (facing.horizontal + 1 >= HORIZONTAL.size) 0 else facing.horizontal + 1
                    state = state.with(HORIZONTAL_FACING, HORIZONTAL[rotated])
                    world?.setBlockState(pos, state)
                }
                else -> return super.useOnBlock(context)
            }
            stack?.damage(1, context.player) { p -> p?.sendToolBreakStatus(context.hand) }
            return ActionResult.SUCCESS
        }
        return super.useOnBlock(context)
    }

    companion object {
        private val ALL = Direction.values()
        private val HORIZONTAL = ALL
            .filter { direction -> direction.axis.isHorizontal }
            .sortedBy { dir -> dir.horizontal }
            .toTypedArray()
        private val VERTICAL_FACING = VerticalFacingMachineBlock.FACING
        private val HORIZONTAL_FACING = FacingMachineBlock.HORIZONTAL_FACING
    }
}