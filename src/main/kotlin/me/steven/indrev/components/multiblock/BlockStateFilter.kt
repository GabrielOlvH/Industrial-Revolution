package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks

class BlockStateFilter(private val filter: (BlockState) -> Boolean, val display: BlockState = Blocks.AIR.defaultState) {

    constructor(blockState: BlockState) : this({ b -> b == blockState }, blockState)

    operator fun invoke(state: BlockState) = filter(state)
}