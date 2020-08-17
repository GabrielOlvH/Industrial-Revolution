package me.steven.indrev.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.fluid.FlowableFluid
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*

class AcidFluidBlock(fluid: FlowableFluid, settings: Settings) : FluidBlock(fluid, settings) {
    override fun randomTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        Direction.values().forEach { dir ->
            val neighbor = pos?.offset(dir)
            val blockState = world?.getBlockState(neighbor)
            val block = blockState?.block
            if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.FARMLAND || block == Blocks.GRASS_PATH)
                world?.setBlockState(pos, Blocks.COARSE_DIRT.defaultState)
        }
    }
}