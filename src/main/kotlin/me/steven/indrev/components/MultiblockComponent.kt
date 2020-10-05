package me.steven.indrev.components

import com.google.common.collect.ImmutableMap
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkSection

class MultiblockComponent(val blockEntity: MachineBlockEntity<*>, val structure: Map<BlockPos, BlockState>) {

    var isBuilt = false

    fun tick() {
        val world = blockEntity.world!!
        var currentChunk: Chunk = world.getChunk(blockEntity.pos)
        val yOffset = blockEntity.pos.y shr 4
        if (blockEntity.pos.y < 0 || yOffset > currentChunk.sectionArray.size) {
            isBuilt = false
            return
        }
        var currentSection: ChunkSection = currentChunk.sectionArray[yOffset]
        isBuilt = structure.all { (offset, state) ->
            val rotation = rotateBlock(blockEntity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
            val statePos = blockEntity.pos.subtract(offset.rotate(rotation))
            if (currentChunk.pos.startX < statePos.x || currentChunk.pos.endX > statePos.x || currentChunk.pos.startZ < statePos.z || currentChunk.pos.endZ > statePos.z) {
                currentChunk = world.getChunk(statePos)
            }
            if (statePos.z shr 4 shl 4 != currentSection.yOffset && statePos.y >= 0 && statePos.y shr 4 < currentChunk.sectionArray.size) {
                currentSection = currentChunk.sectionArray[statePos.y shr 4]
            }
            !ChunkSection.isEmpty(currentSection) && currentSection.getBlockState(statePos.x and 15, statePos.y and 15, statePos.z and 15) == state
        }
    }

    fun test(): Boolean {
        val rotation = rotateBlock(blockEntity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        return structure.all { (offset, state) ->
            blockEntity.world!!.getBlockState(blockEntity.pos.subtract(offset.rotate(rotation))) == state
        }
    }

    fun rotateBlock(direction: Direction): BlockRotation {
        return when (direction) {
            Direction.NORTH -> BlockRotation.NONE
            Direction.SOUTH -> BlockRotation.CLOCKWISE_180
            Direction.WEST -> BlockRotation.COUNTERCLOCKWISE_90
            Direction.EAST -> BlockRotation.CLOCKWISE_90
            else -> return BlockRotation.NONE
        }
    }

    class Builder(val test: MutableMap<BlockPos, BlockState> = HashMap()) {
        fun add(blockPos: BlockPos, blockState: BlockState): Builder {
            test[blockPos] = blockState
            return this
        }

        fun corners(center: BlockPos, radius: Int, state: BlockState): Builder {
            add(center.add(radius, radius, 0), state)
            add(center.add(-radius, radius, 0), state)
            add(center.add(radius, -radius, 0), state)
            add(center.add(-radius, -radius, 0), state)
            return this
        }

        fun diamond(center: BlockPos, radius: Int, state: BlockState): Builder {
            add(center.add(0, radius, 0), state)
            add(center.add(0, -radius, 0), state)
            add(center.add(radius, 0, 0), state)
            add(center.add(-radius, 0, 0), state)
            return this
        }

        fun cube(start: BlockPos, width: Int, depth: Int, height: Int, state: BlockState): Builder {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    for (z in 0 until depth) {
                        add(start.add(BlockPos(x, y, z)), state)
                    }
                }
            }
            return this
        }

        fun build(blockEntity: MachineBlockEntity<*>) = MultiblockComponent(blockEntity, ImmutableMap.copyOf(test))
    }
}