package me.steven.indrev.components.multiblock

import net.minecraft.util.math.BlockPos

class StructureHolder(val variants: Map<StructureIdentifier, Map<BlockPos, BlockStateFilter>> = hashMapOf()) {

    init {
        variants.forEach { (id, structure) -> REGISTRY[id] = structure }
    }

    companion object {
        val REGISTRY: MutableMap<StructureIdentifier, Map<BlockPos, BlockStateFilter>> = hashMapOf()
    }
}