package me.steven.indrev.components.multiblock

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DefaultMultiblockMatcher<T : StructureDefinition>(private val definitions: Array<T>) : AbstractMultiblockMatcher() {

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        for (definition in definitions) {
            if (!definition.isBuilt(world, pos, state)) {
                isBuilt = definition.isOptional
                return
            }
        }
        isBuilt = true
    }


    //TODO reimplement this
    @Environment(EnvType.CLIENT)
    override fun getRenderingStructure(): Map<BlockPos, BlockState> = emptyMap()
}