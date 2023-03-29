package me.steven.indrev.world.features

import com.mojang.serialization.Codec
import me.steven.indrev.blocks.misc.SulfurCrystalBlock
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.any
import me.steven.indrev.utils.forEach
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.util.FeatureContext
import kotlin.random.Random

class SulfurCrystalFeature(codec: Codec<DefaultFeatureConfig>) : Feature<DefaultFeatureConfig>(codec) {

    private val RANDOM = Random(1)

    override fun generate(
        context: FeatureContext<DefaultFeatureConfig>
    ): Boolean {
        val blockPos = context.origin
        val world = context.world
        val random = context.random
        val mutablePos = BlockPos.Mutable()
        val coveredArea = Box(blockPos).expand(8.0, 8.0, 8.0)
        val isNearLava = coveredArea.any { x, y, z ->
            if (context.world.isOutOfHeightLimit(y)) return@any false
            mutablePos.set(x, y, z)
            world?.getBlockState(mutablePos)?.isOf(Blocks.LAVA) == true
        }
        if (!isNearLava) return false
        coveredArea.forEach { x, y, z ->
            if (context.world.isOutOfHeightLimit(y)) return@forEach
            mutablePos.set(x, y, z)
            DIRECTIONS_LIST.shuffled(RANDOM).forEach { dir ->
                val blockState = world?.getBlockState(mutablePos)
                val pos = mutablePos.offset(dir)
                val airState = world?.getBlockState(pos)
                val state = IRBlockRegistry.SULFUR_CRYSTAL_CLUSTER.defaultState.with(SulfurCrystalBlock.FACING, dir)
                if (blockState?.material == Material.STONE && airState?.isAir == true && state.canPlaceAt(world, pos)) {
                    world.setBlockState(pos, state, 2)
                    return true
                }
            }
        }

        return false
    }

    companion object {
        private val DIRECTIONS_LIST = Direction.values().toMutableList()
    }
}