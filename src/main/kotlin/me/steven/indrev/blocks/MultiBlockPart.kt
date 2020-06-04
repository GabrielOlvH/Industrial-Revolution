package me.steven.indrev.blocks

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

interface MultiBlockPart {
    fun getBlockEntityPos(state: BlockState, blockPos: BlockPos): BlockPos
}