package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class AbstractMultiblockMatcher {

    abstract val definitions: Array<StructureDefinition>

    var isBuilt = false

    abstract fun tick(world: World, pos: BlockPos, state: BlockState)

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
        // this is because there is something fundamentally wrong in the rotation/position of the multiblock structures
        // I don't want to fix it now so this is a workaround and yes I am aware of the problems.
        fun rotateBlock0(direction: Direction): BlockRotation {
            return when (direction) {
                Direction.NORTH -> BlockRotation.NONE
                Direction.SOUTH -> BlockRotation.CLOCKWISE_180
                Direction.WEST -> BlockRotation.CLOCKWISE_90
                Direction.EAST -> BlockRotation.COUNTERCLOCKWISE_90
                else -> return BlockRotation.NONE
            }
        }
    }
}