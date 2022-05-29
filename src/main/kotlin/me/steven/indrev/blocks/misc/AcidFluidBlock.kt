package me.steven.indrev.blocks.misc

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.fluid.FlowableFluid
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class AcidFluidBlock(fluid: FlowableFluid, settings: Settings) : FluidBlock(fluid, settings) {
    override fun randomTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) {
        Direction.values().forEach { dir ->
            val neighbor = pos?.offset(dir)
            val blockState = world?.getBlockState(neighbor)
            val block = blockState?.block
            if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.FARMLAND || block == Blocks.DIRT_PATH)
                world?.setBlockState(pos, Blocks.COARSE_DIRT.defaultState)
        }
    }

    override fun randomDisplayTick(state: BlockState?, world: World, pos: BlockPos, random: Random) {
        if (!world.isAir(pos.up()) || random.nextInt(10) < 5) return
        (0..1).forEach { a ->
            (0..1).forEach { b ->
                world.addParticle(
                    ParticleTypes.SNEEZE, pos.x + a / 2.0 + (random.nextFloat() / 5), pos.y + 1.0, pos.z + b / 2.0 + (random.nextFloat() / 5), 0.0, 0.005, 0.0)
            }
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