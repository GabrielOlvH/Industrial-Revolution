package me.steven.indrev.world.chunkveins

import me.steven.indrev.registry.IRRegistry
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

enum class ChunkVeinType(val sizeRange: IntRange, val ores: WeightedList<Block>, val dimension: RegistryKey<World> = World.OVERWORLD) {
    PEAT(
        300..1000,
        WeightedList<Block>().add(Blocks.COAL_ORE, 6)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    LIGNITE(
        600..1500,
        WeightedList<Block>().add(Blocks.COAL_ORE, 8)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    BITUMINOUS(
        900..1900,
        WeightedList<Block>().add(Blocks.COAL_ORE, 20).add(Blocks.DIAMOND_ORE, 1)
            .add(Blocks.STONE, 20).add(Blocks.DIRT, 10).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    ANTHRACITE(
        2000..6000,
        WeightedList<Block>().add(Blocks.COAL_ORE, 24).add(Blocks.DIAMOND_ORE, 1)
            .add(Blocks.STONE, 20).add(Blocks.DIRT, 10).add(Blocks.CLAY, 2).add(Blocks.GRAVEL, 2)
    ),
    SIDERITE(
        500..1000,
        WeightedList<Block>().add(Blocks.IRON_ORE, 6)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    LIMONITE(
        700..1500,
        WeightedList<Block>().add(Blocks.IRON_ORE, 8)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    HEMATITE(
        900..2000,
        WeightedList<Block>().add(Blocks.IRON_ORE, 10)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    MAGNETITE(
        1600..3500,
        WeightedList<Block>().add(Blocks.IRON_ORE, 12)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    CHALCOPRYTE(
        600..1200,
        WeightedList<Block>().add(IRRegistry.COPPER_ORE, 6).add(Blocks.IRON_ORE, 8)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    CUPRITE(
        1200..2100,
        WeightedList<Block>().add(IRRegistry.COPPER_ORE, 6)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    CASSITERITE(
        1200..2100,
        WeightedList<Block>().add(IRRegistry.TIN_ORE, 6)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    STANNITE(
        1600..2700,
        WeightedList<Block>().add(IRRegistry.TIN_ORE, 6).add(IRRegistry.COPPER_ORE, 4).add(Blocks.IRON_ORE, 4)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    CALAVERITE(
        400..600,
        WeightedList<Block>().add(Blocks.GOLD_ORE, 8)
            .add(Blocks.STONE, 10).add(Blocks.DIRT, 5).add(Blocks.CLAY, 1).add(Blocks.GRAVEL, 1)
    ),
    QUARTZ(
        200..3000,
        WeightedList<Block>().add(Blocks.NETHER_QUARTZ_ORE, 8)
            .add(Blocks.NETHERRACK, 10).add(Blocks.SOUL_SAND, 3).add(Blocks.GRAVEL, 1),
        World.NETHER
    ),
    CALAVERITE_NETHER(
        1200..3000,
        WeightedList<Block>().add(Blocks.NETHER_GOLD_ORE, 64)
            .add(Blocks.NETHERRACK, 80).add(Blocks.SOUL_SAND, 24).add(Blocks.GRAVEL, 8).add(Blocks.ANCIENT_DEBRIS, 1),
        World.NETHER
    )
}