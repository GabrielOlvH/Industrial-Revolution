package me.steven.indrev.blocks

import me.steven.indrev.blockentities.solarpowerplant.HeliostatBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockView
import net.minecraft.world.World

class HeliostatBlock(settings: Settings) : Block(settings), BlockEntityProvider {

    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.ENTITYBLOCK_ANIMATED

    override fun createBlockEntity(world: BlockView?): BlockEntity = HeliostatBlockEntity()

    companion object {
        private const val RAD2DEG = 57.2957763671875

        fun getYaw(origin: BlockPos, target: BlockPos): Float {
            val xOffset = target.x + 0.5 - origin.x.toDouble() + 0.5
            val zOffset = target.z + 0.5 - origin.z.toDouble() + 0.5
            return MathHelper.wrapDegrees((MathHelper.atan2(zOffset, xOffset) * RAD2DEG).toFloat() - 90.0f)
        }

        fun getPitch(origin: BlockPos, target: BlockPos): Float {
            val xOffset = target.x + 0.5 - origin.x.toDouble() + 0.5
            val yOffset = target.y + 0.5 - origin.y.toDouble() + 0.5
            val zOffset = target.z + 0.5 - origin.z.toDouble() + 0.5
            val g = MathHelper.sqrt(xOffset * xOffset + zOffset * zOffset).toDouble()
            return MathHelper.wrapDegrees((-(MathHelper.atan2(yOffset, g) * RAD2DEG)).toFloat())
        }


        fun findConnectingHeliostats(origin: BlockPos, world: World, scanned: MutableSet<Long>, positions: MutableSet<Long>) {
            Direction.values().forEach { dir ->
                val off = origin.offset(dir)
                if (scanned.add(off.asLong()) && world.testBlockState(off) { state -> state.block is HeliostatBlock }) {
                    positions.add(off.asLong())
                    findConnectingHeliostats(off, world, scanned, positions)
                }
            }
        }
    }
}