package me.steven.indrev.blocks.misc

import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.SnowBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.WorldView

class PlankBlock(settings: Settings) : SnowBlock(settings) {
    override fun canPlaceAt(state: BlockState, world: WorldView?, pos: BlockPos): Boolean = true

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = LAYERS_TO_SHAPE[state.get(LAYERS)]
}