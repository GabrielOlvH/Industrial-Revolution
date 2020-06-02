package me.steven.indrev.world

import me.steven.indrev.registry.ModRegistry
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.WeightedList

enum class ChunkVeinType(val sizeRange: IntRange, val ores: WeightedList<Block>) {
    PEAT(300..1000, WeightedList<Block>().add(Blocks.COAL_ORE, 6).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    LIGNITE(600..1500, WeightedList<Block>().add(Blocks.COAL_ORE, 8).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    BITUMINOUS(900..1900, WeightedList<Block>().add(Blocks.COAL_ORE, 10).add(Blocks.DIAMOND_ORE, 1).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    ANTHRACITE(2000..6000, WeightedList<Block>().add(Blocks.COAL_ORE, 12).add(Blocks.DIAMOND_ORE, 2).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    SIDERITE(500..1000, WeightedList<Block>().add(Blocks.IRON_ORE, 6).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    LIMONITE(700..1500, WeightedList<Block>().add(Blocks.IRON_ORE, 8).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    HEMATITE(900..2000, WeightedList<Block>().add(Blocks.IRON_ORE, 10).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    MAGNETITE(1000..2500, WeightedList<Block>().add(Blocks.IRON_ORE, 12).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    CHALCOPRYTE(600..1200, WeightedList<Block>().add(ModRegistry.COPPER_ORE, 6).add(Blocks.IRON_ORE, 8).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    CUPRITE(800..1900, WeightedList<Block>().add(ModRegistry.COPPER_ORE, 6).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1)),
    CASSITERITE(800..1900, WeightedList<Block>().add(ModRegistry.TIN_ORE, 6).add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1));

    companion object {

        fun valueOf(string: String?): ChunkVeinType? {
            return when (string) {
                "ANTHRACITE" -> ANTHRACITE
                "HEMATITE" -> HEMATITE
                else -> null
            }
        }
    }
}