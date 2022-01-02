package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.BlockState
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

interface StructureDefinition {
    val identifier: String
    val isOptional: Boolean
    val holder: StructureHolder

    fun toMatcher(): MultiblockMatcher = MultiblockMatcher(this)
}