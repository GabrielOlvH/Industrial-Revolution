package me.steven.indrev.components

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

abstract class AbstractMultiblockComponent {

    var isBuilt = false

    abstract fun tick()

    @Environment(EnvType.CLIENT)
    abstract fun getRenderingStructure(): Map<BlockPos, BlockState>

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