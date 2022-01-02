package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

class MultiblockMatcher(val definition: StructureDefinition) {

    var builtId: StructureIdentifier? = null
    val matches get() = builtId != null

    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (builtId != null && !isBuilt(world, pos, state, definition.holder.variants[builtId]!!)) {
            builtId = getBuiltStructureId(world, pos, state)
        }
    }

    fun getBuiltStructureId(world: World, pos: BlockPos, state: BlockState): StructureIdentifier? {
        return definition.holder.variants
            .filter { (_, structure) -> isBuilt(world, pos, state, structure) }
            .map { it.key }
            .firstOrNull()
    }

    fun isBuilt(world: World, pos: BlockPos, state: BlockState, map: Map<BlockPos, BlockStateFilter>): Boolean {
        var currentChunk: Chunk = world.getChunk(pos)
        if (pos.y < 0)
            return false
        val rotation =
            rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])
        return map.all { (offset, statePredicate) ->
            val statePos = pos.subtract(offset.rotate(rotation).rotate(BlockRotation.CLOCKWISE_180))
            if (currentChunk.pos.startX < statePos.x || currentChunk.pos.endX > statePos.x || currentChunk.pos.startZ < statePos.z || currentChunk.pos.endZ > statePos.z) {
                currentChunk = world.getChunk(statePos)
            }
            val blockState = currentChunk.getBlockState(statePos)
                .rotate(rotateBlock0(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING]))
            statePredicate(blockState)
        }
    }

    fun getBuiltStructure(): Map<BlockPos, BlockStateFilter> {
        return if (builtId == null) emptyMap() else definition.holder.variants[builtId] ?: emptyMap()
    }

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