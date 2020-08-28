package me.steven.indrev.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.fluid.FlowableFluid
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
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

    override fun onEntityCollision(state: BlockState?, world: World, pos: BlockPos?, entity: Entity?) {
        if (world.time % 15 == 0L)
            entity?.damage(ACID_DAMAGE_SOURCE, 4f)
    }

    companion object {
        val ACID_DAMAGE_SOURCE = object : DamageSource("acid") {
            init {
                setBypassesArmor()
                setUnblockable()
            }
        }
    }
}