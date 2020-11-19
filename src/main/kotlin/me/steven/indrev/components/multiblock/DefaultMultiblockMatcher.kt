package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DefaultMultiblockMatcher(override val definitions: Array<StructureDefinition>) : AbstractMultiblockMatcher() {

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        for (definition in definitions) {
            if (!definition.isBuilt(world, pos, state)) {
                isBuilt = definition.isOptional
                return
            }
        }
        isBuilt = true
    }
}