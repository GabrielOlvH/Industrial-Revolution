package me.steven.indrev.components.multiblock

import com.google.common.collect.ImmutableMap
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

class StructureHelper(private val structure: MutableMap<BlockPos, (BlockState) -> Boolean> = HashMap()) {
    fun add(blockPos: BlockPos, blockState: (BlockState) -> Boolean): StructureHelper {
        structure[blockPos] = blockState
        return this
    }

    fun corners(center: BlockPos, radius: Int, state: (BlockState) -> Boolean, rotation: BlockRotation = BlockRotation.NONE): StructureHelper {
        add(center.add(radius, radius, 0).rotate(rotation), state)
        add(center.add(-radius, radius, 0).rotate(rotation), state)
        add(center.add(radius, -radius, 0).rotate(rotation), state)
        add(center.add(-radius, -radius, 0).rotate(rotation), state)
        return this
    }

    fun horizontalCorners(center: BlockPos, radius: Int, state: (BlockState) -> Boolean, rotation: BlockRotation = BlockRotation.NONE): StructureHelper {
        add(center.add(radius, 0, radius).rotate(rotation), state)
        add(center.add(-radius, 0, radius).rotate(rotation), state)
        add(center.add(radius, 0, -radius).rotate(rotation), state)
        add(center.add(-radius, 0, -radius).rotate(rotation), state)
        return this
    }

    fun diamond(center: BlockPos, radius: Int, state: (BlockState) -> Boolean): StructureHelper {
        add(center.add(0, radius, 0), state)
        add(center.add(0, -radius, 0), state)
        add(center.add(radius, 0, 0), state)
        add(center.add(-radius, 0, 0), state)
        return this
    }

    fun cube(start: BlockPos, width: Int, depth: Int, height: Int, state: (BlockState) -> Boolean): StructureHelper {
        for (x in 0 until width) {
            for (y in 0 until height) {
                for (z in 0 until depth) {
                    add(start.add(BlockPos(x, y, z)), state)
                }
            }
        }
        return this
    }

    fun add(blockPos: BlockPos, blockState: BlockState): StructureHelper {
        structure[blockPos] = { b -> b == blockState }
        return this
    }

    fun corners(center: BlockPos, radius: Int, state: BlockState, rotation: BlockRotation = BlockRotation.NONE): StructureHelper {
        add(center.add(radius, radius, 0).rotate(rotation), state)
        add(center.add(-radius, radius, 0).rotate(rotation), state)
        add(center.add(radius, -radius, 0).rotate(rotation), state)
        add(center.add(-radius, -radius, 0).rotate(rotation), state)
        return this
    }

    fun horizontalCorners(center: BlockPos, radius: Int, state: BlockState, rotation: BlockRotation = BlockRotation.NONE): StructureHelper {
        add(center.add(radius, 0, radius).rotate(rotation), state)
        add(center.add(-radius, 0, radius).rotate(rotation), state)
        add(center.add(radius, 0, -radius).rotate(rotation), state)
        add(center.add(-radius, 0, -radius).rotate(rotation), state)
        return this
    }

    fun diamond(center: BlockPos, radius: Int, state: BlockState): StructureHelper {
        add(center.add(0, radius, 0), state)
        add(center.add(0, -radius, 0), state)
        add(center.add(radius, 0, 0), state)
        add(center.add(-radius, 0, 0), state)
        return this
    }

    fun cube(start: BlockPos, width: Int, depth: Int, height: Int, state: BlockState): StructureHelper {
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
        structure.remove(pos)
    }

    fun create(): Map<BlockPos, (BlockState) -> Boolean> = ImmutableMap.copyOf(structure)
}