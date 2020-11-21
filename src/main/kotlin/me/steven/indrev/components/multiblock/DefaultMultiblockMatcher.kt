package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DefaultMultiblockMatcher(override val definitions: Array<StructureDefinition>) : AbstractMultiblockMatcher() {

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        structureIds.clear()
        for (definition in definitions) {
            structureIds.addAll(definition.getBuiltStructureId(world, pos, state))
        }
    }
}