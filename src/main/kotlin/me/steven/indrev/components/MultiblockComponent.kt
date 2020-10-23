package me.steven.indrev.components

import com.google.common.collect.ImmutableMap
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk

class MultiblockComponent constructor(val blockEntity: MachineBlockEntity<*>, val structure: Map<BlockPos, BlockState>)
    : AbstractMultiblockComponent() {

    override fun tick() {
        val world = blockEntity.world!!
        var currentChunk: Chunk = world.getChunk(blockEntity.pos)
        if (blockEntity.pos.y < 0) {
            isBuilt = false
            return
        }
        val rotation = rotateBlock(blockEntity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        isBuilt = structure.all { (offset, state) ->
            val statePos = blockEntity.pos.subtract(offset.rotate(rotation))
            if (currentChunk.pos.startX < statePos.x || currentChunk.pos.endX > statePos.x || currentChunk.pos.startZ < statePos.z || currentChunk.pos.endZ > statePos.z) {
                currentChunk = world.getChunk(statePos)
            }
            currentChunk.getBlockState(statePos) == state.rotate(rotation.rotate(BlockRotation.CLOCKWISE_180))
        }
    }

    @Environment(EnvType.CLIENT)
    override fun getRenderingStructure(): Map<BlockPos, BlockState> = structure

    fun test(): Boolean {
        val rotation = rotateBlock(blockEntity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        return structure.all { (offset, state) ->
            blockEntity.world!!.getBlockState(blockEntity.pos.subtract(offset.rotate(rotation))) == state
        }
    }

    class Builder(val test: MutableMap<BlockPos, BlockState> = HashMap()) {
        fun add(blockPos: BlockPos, blockState: BlockState): Builder {
            test[blockPos] = blockState
            return this
        }

        fun corners(center: BlockPos, radius: Int, state: BlockState, rotation: BlockRotation = BlockRotation.NONE): Builder {
            add(center.add(radius, radius, 0).rotate(rotation), state)
            add(center.add(-radius, radius, 0).rotate(rotation), state)
            add(center.add(radius, -radius, 0).rotate(rotation), state)
            add(center.add(-radius, -radius, 0).rotate(rotation), state)
            return this
        }

        fun horizontalCorners(center: BlockPos, radius: Int, state: BlockState, rotation: BlockRotation = BlockRotation.NONE): Builder {
            add(center.add(radius, 0, radius).rotate(rotation), state)
            add(center.add(-radius, 0, radius).rotate(rotation), state)
            add(center.add(radius, 0, -radius).rotate(rotation), state)
            add(center.add(-radius, 0, -radius).rotate(rotation), state)
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

        fun remove(pos: BlockPos) {
            test.remove(pos)
        }

        fun build(blockEntity: MachineBlockEntity<*>) = MultiblockComponent(blockEntity, ImmutableMap.copyOf(test))
    }
}