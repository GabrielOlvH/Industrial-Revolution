package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

interface StructureDefinition {
    val identifier: String
    val isOptional: Boolean
    val structure: Map<BlockPos, BlockStateFilter>

    val appendices: Array<StructureDefinition>
        get() = emptyArray()

    fun toMatcher(): AbstractMultiblockMatcher = DefaultMultiblockMatcher(arrayOf(*appendices, this))

    fun isBuilt(world: World, pos: BlockPos, state: BlockState): Boolean {
        var currentChunk: Chunk = world.getChunk(pos)
        if (pos.y < 0)
            return false
        val rotation =
            AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])
        return structure.all { (offset, statePredicate) ->
            val statePos = pos.subtract(offset.rotate(rotation).rotate(BlockRotation.CLOCKWISE_180))
            if (currentChunk.pos.startX < statePos.x || currentChunk.pos.endX > statePos.x || currentChunk.pos.startZ < statePos.z || currentChunk.pos.endZ > statePos.z) {
                currentChunk = world.getChunk(statePos)
            }
            val blockState = currentChunk.getBlockState(statePos).rotate( AbstractMultiblockMatcher.rotateBlock0(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING]))
            statePredicate(blockState)
        }
    }
}