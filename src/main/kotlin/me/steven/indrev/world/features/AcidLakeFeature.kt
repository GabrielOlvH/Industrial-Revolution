package me.steven.indrev.world.features

import com.mojang.serialization.Codec
import me.steven.indrev.registry.IRRegistry
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.world.LightType
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.LakeFeature
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import net.minecraft.world.gen.feature.StructureFeature
import java.util.*

class AcidLakeFeature(codec: Codec<SingleStateFeatureConfig>) : LakeFeature(codec) {
    override fun generate(
        structureWorldAccess: StructureWorldAccess,
        chunkGenerator: ChunkGenerator,
        random: Random,
        origin: BlockPos,
        singleStateFeatureConfig: SingleStateFeatureConfig
    ): Boolean {
        var blockPos = origin
        while (blockPos.y > 5 && structureWorldAccess.isAir(blockPos)) {
            blockPos = blockPos.down()
        }

        if (blockPos.y <= 4) return false
        blockPos = blockPos.down(4)
        if (structureWorldAccess.getStructures(ChunkSectionPos.from(blockPos), StructureFeature.VILLAGE).findAny().isPresent)
            return false
        val bls = BooleanArray(2048)
        val i = random.nextInt(4) + 4
        var ab: Int
        ab = 0
        while (ab < i) {
            val d = random.nextDouble() * 6.0 + 3.0
            val e = random.nextDouble() * 4.0 + 2.0
            val f = random.nextDouble() * 6.0 + 3.0
            val g = random.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0
            val h = random.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0
            val k = random.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0
            for (l in 1..14) {
                for (m in 1..14) {
                    for (n in 1..6) {
                        val o = (l.toDouble() - g) / (d / 2.0)
                        val p = (n.toDouble() - h) / (e / 2.0)
                        val q = (m.toDouble() - k) / (f / 2.0)
                        val r = o * o + p * p + q * q
                        if (r < 1.0) {
                            bls[(l * 16 + m) * 8 + n] = true
                        }
                    }
                }
            }
            ++ab
        }
        var ad: Int
        var ac: Int
        var bl2: Boolean
        ab = 0
        while (ab < 16) {
            ac = 0
            while (ac < 16) {
                ad = 0
                while (ad < 8) {
                    bl2 =
                        !bls[(ab * 16 + ac) * 8 + ad] && (ab < 15 && bls[((ab + 1) * 16 + ac) * 8 + ad] || ab > 0 && bls[((ab - 1) * 16 + ac) * 8 + ad] || ac < 15 && bls[(ab * 16 + ac + 1) * 8 + ad] || ac > 0 && bls[(ab * 16 + (ac - 1)) * 8 + ad] || ad < 7 && bls[(ab * 16 + ac) * 8 + ad + 1] || ad > 0 && bls[(ab * 16 + ac) * 8 + (ad - 1)])
                    if (bl2) {
                        val material = structureWorldAccess.getBlockState(blockPos.add(ab, ad, ac)).material
                        if (ad >= 4 && material.isLiquid) {
                            return false
                        }
                        if (ad < 4 && !material.isSolid && structureWorldAccess.getBlockState(
                                blockPos.add(
                                    ab,
                                    ad,
                                    ac
                                )
                            ) !== singleStateFeatureConfig.state
                        ) {
                            return false
                        }
                    }
                    ++ad
                }
                ++ac
            }
            ++ab
        }
        ab = 0
        while (ab < 16) {
            ac = 0
            while (ac < 16) {
                ad = 0
                while (ad < 8) {
                    if (bls[(ab * 16 + ac) * 8 + ad]) {
                        structureWorldAccess.setBlockState(
                            blockPos.add(ab, ad, ac),
                            if (ad >= 4) Blocks.CAVE_AIR.defaultState else singleStateFeatureConfig.state,
                            2
                        )
                    }
                    ++ad
                }
                ++ac
            }
            ++ab
        }
        var blockPos3: BlockPos?
        ab = 0
        while (ab < 16) {
            ac = 0
            while (ac < 16) {
                ad = 4
                while (ad < 8) {
                    if (bls[(ab * 16 + ac) * 8 + ad]) {
                        blockPos3 = blockPos.add(ab, ad - 1, ac)
                        if (Feature.isSoil(structureWorldAccess.getBlockState(blockPos3).block) && structureWorldAccess.getLightLevel(
                                LightType.SKY,
                                blockPos.add(ab, ad, ac)
                            ) > 0
                        ) {
                            structureWorldAccess.setBlockState(blockPos3, Blocks.GRASS_BLOCK.defaultState, 2)
                        }
                    }
                    ++ad
                }
                ++ac
            }
            ++ab
        }
        if (singleStateFeatureConfig.state.material == IRRegistry.ACID_MATERIAL) {
            ab = 0
            while (ab < 16) {
                ac = 0
                while (ac < 16) {
                    ad = 0
                    while (ad < 8) {
                        bl2 =
                            !bls[(ab * 16 + ac) * 8 + ad] && (ab < 15 && bls[((ab + 1) * 16 + ac) * 8 + ad] || ab > 0 && bls[((ab - 1) * 16 + ac) * 8 + ad] || ac < 15 && bls[(ab * 16 + ac + 1) * 8 + ad] || ac > 0 && bls[(ab * 16 + (ac - 1)) * 8 + ad] || ad < 7 && bls[(ab * 16 + ac) * 8 + ad + 1] || ad > 0 && bls[(ab * 16 + ac) * 8 + (ad - 1)])
                        if (bl2 && (ad < 4 || random.nextInt(2) != 0) && structureWorldAccess.getBlockState(
                                blockPos.add(
                                    ab,
                                    ad,
                                    ac
                                )
                            ).material.isSolid
                        ) {
                            structureWorldAccess.setBlockState(
                                blockPos.add(ab, ad, ac),
                                Blocks.COARSE_DIRT.defaultState,
                                2
                            )
                        }
                        ++ad
                    }
                    ++ac
                }
                ++ab
            }
        }
        return true
    }
}