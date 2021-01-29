package me.steven.indrev.blocks.misc

import net.minecraft.block.BlockState
import net.minecraft.block.SnowBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldView

class PlankBlock(settings: Settings) : SnowBlock(settings) {
    override fun canPlaceAt(state: BlockState, world: WorldView?, pos: BlockPos): Boolean = true
}