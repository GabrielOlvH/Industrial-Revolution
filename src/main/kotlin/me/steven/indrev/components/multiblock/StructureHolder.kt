package me.steven.indrev.components.multiblock

import com.google.common.collect.ImmutableMap
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

class StructureHolder(val variants: Map<StructureIdentifier, Map<BlockPos, BlockStateFilter>> = hashMapOf()) {

    class Builder(private val definition: StructureDefinition, private val structure: MutableMap<BlockPos, BlockStateFilter> = HashMap()) {

        private val createdStructures: MutableMap<StructureIdentifier, Map<BlockPos, BlockStateFilter>> = hashMapOf()

        fun add(blockPos: BlockPos, blockState: (BlockState) -> Boolean): Builder {
            structure[blockPos] = BlockStateFilter(blockState)
            return this
        }

        fun from(structure: Map<BlockPos, BlockStateFilter>): Builder {
            this.structure.clear()
            this.structure.putAll(structure)
            return this
        }

        fun corners(
            center: BlockPos,
            radius: Int,
            state: (BlockState) -> Boolean,
            rotation: BlockRotation = BlockRotation.NONE
        ): Builder {
            add(center.add(radius, radius, 0).rotate(rotation), state)
            add(center.add(-radius, radius, 0).rotate(rotation), state)
            add(center.add(radius, -radius, 0).rotate(rotation), state)
            add(center.add(-radius, -radius, 0).rotate(rotation), state)
            return this
        }

        fun horizontalCorners(
            center: BlockPos,
            radius: Int,
            state: (BlockState) -> Boolean,
            rotation: BlockRotation = BlockRotation.NONE
        ): Builder {
            add(center.add(radius, 0, radius).rotate(rotation), state)
            add(center.add(-radius, 0, radius).rotate(rotation), state)
            add(center.add(radius, 0, -radius).rotate(rotation), state)
            add(center.add(-radius, 0, -radius).rotate(rotation), state)
            return this
        }

        fun diamond(center: BlockPos, radius: Int, state: (BlockState) -> Boolean): Builder {
            add(center.add(0, radius, 0), state)
            add(center.add(0, -radius, 0), state)
            add(center.add(radius, 0, 0), state)
            add(center.add(-radius, 0, 0), state)
            return this
        }

        fun cube(start: BlockPos, width: Int, depth: Int, height: Int, state: (BlockState) -> Boolean): Builder {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    for (z in 0 until depth) {
                        add(start.add(BlockPos(x, y, z)), state)
                    }
                }
            }
            return this
        }

        fun add(blockPos: BlockPos, blockState: BlockState): Builder {
            structure[blockPos] = BlockStateFilter(blockState)
            return this
        }

        fun corners(
            center: BlockPos,
            radius: Int,
            state: BlockState,
            rotation: BlockRotation = BlockRotation.NONE
        ): Builder {
            add(center.add(radius, radius, 0).rotate(rotation), state)
            add(center.add(-radius, radius, 0).rotate(rotation), state)
            add(center.add(radius, -radius, 0).rotate(rotation), state)
            add(center.add(-radius, -radius, 0).rotate(rotation), state)
            return this
        }

        fun horizontalCorners(
            center: BlockPos,
            radius: Int,
            state: BlockState,
            rotation: BlockRotation = BlockRotation.NONE
        ): Builder {
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


        fun remove(pos: BlockPos): Builder {
            structure.remove(pos)
            return this
        }

        fun create(variant: String): Builder {
            createdStructures[StructureIdentifier("indrev", definition.identifier, variant)] = ImmutableMap.copyOf(structure)
            structure.clear()
            return this
        }

        fun build(): StructureHolder = StructureHolder(ImmutableMap.copyOf(createdStructures))
    }
}