package me.steven.indrev.components.multiblock

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class AbstractMultiblockMatcher {

    var isBuilt = false

    abstract fun tick(world: World, pos: BlockPos, state: BlockState)

    @Environment(EnvType.CLIENT)
    abstract fun getRenderingStructure(): Map<BlockPos, BlockState>

    companion object {
        fun rotateBlock(direction: Direction): BlockRotation {
            return when (direction) {
                Direction.NORTH -> BlockRotation.NONE
                Direction.SOUTH -> BlockRotation.CLOCKWISE_180
                Direction.WEST -> BlockRotation.COUNTERCLOCKWISE_90
                Direction.EAST -> BlockRotation.CLOCKWISE_90
                else -> return BlockRotation.NONE
            }
        }
    }
}