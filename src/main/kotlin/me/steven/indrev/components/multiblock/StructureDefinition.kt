package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

abstract class StructureDefinition {
    abstract val identifier: String
    abstract val isOptional: Boolean
    abstract val holder: StructureHolder

    open val appendices: Array<StructureDefinition> = emptyArray()

    open fun toMatcher(): AbstractMultiblockMatcher = DefaultMultiblockMatcher(arrayOf(*appendices, this))

    fun getBuiltStructureId(world: World, pos: BlockPos, state: BlockState): Set<StructureIdentifier> {
        var currentChunk: Chunk = world.getChunk(pos)
        if (pos.y < 0)
            return emptySet()
        val rotation =
            AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])
        return arrayOf(*appendices, this).map { it.holder.variants }.flatMap { it.entries }.filter { (id, structure) ->
            structure.all { (offset, statePredicate) ->
                val statePos = pos.subtract(offset.rotate(rotation).rotate(BlockRotation.CLOCKWISE_180))
                if (currentChunk.pos.startX < statePos.x || currentChunk.pos.endX > statePos.x || currentChunk.pos.startZ < statePos.z || currentChunk.pos.endZ > statePos.z) {
                    currentChunk = world.getChunk(statePos)
                }
                val blockState = currentChunk.getBlockState(statePos)
                    .rotate(AbstractMultiblockMatcher.rotateBlock0(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING]))
                statePredicate(blockState)
            }
        }.map { it.key }.toSet()
    }
}