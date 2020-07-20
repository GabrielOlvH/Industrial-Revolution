package me.steven.indrev.world.chunkveins

import me.steven.indrev.world.chunkveins.ChunkVeinType.*
import net.minecraft.util.collection.WeightedList
import net.minecraft.world.biome.Biome

object VeinPicker {
    private val TAIGA_VEINS = picker {
        when (it) {
            PEAT, LIGNITE -> 5
            CALAVERITE, HEMATITE -> 4
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val HILLS_VEINS = picker {
        when (it) {
            BITUMINOUS, ANTHRACITE -> 5
            HEMATITE, MAGNETITE, STANNITE, CALAVERITE -> 4
            LIMONITE, CUPRITE, PEAT, LIGNITE -> 3
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val JUNGLE_VEINS = picker {
        when (it) {
            PEAT, LIGNITE -> 5
            CALAVERITE, HEMATITE -> 3
            else -> -1
        }
    }

    private val MESA_VEINS = picker {
        when (it) {
            BITUMINOUS, ANTHRACITE, HEMATITE, MAGNETITE, STANNITE, CALAVERITE -> 4
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val PLAINS_VEINS = picker {
        when (it) {
            PEAT, LIGNITE, SIDERITE, CHALCOPRYTE, CASSITERITE -> 5
            BITUMINOUS, LIMONITE, CUPRITE -> 3
            ANTHRACITE, HEMATITE, MAGNETITE, STANNITE, CALAVERITE -> 1
            else -> -1
        }
    }

    private val SAVANNA_VEINS = picker {
        when (it) {
            LIGNITE, SIDERITE, CHALCOPRYTE, CASSITERITE -> 4
            BITUMINOUS, LIMONITE, CUPRITE -> 6
            else -> -1
        }
    }

    private val ICY_VEINS = picker {
        when (it) {
            PEAT, LIGNITE -> 5
            CALAVERITE, CASSITERITE, CHALCOPRYTE, SIDERITE -> 1
            else -> -1
        }
    }

    private val BEACH_VEINS = picker {
        when (it) {
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val FOREST_VEINS = picker {
        when (it) {
            PEAT, LIGNITE, SIDERITE, CHALCOPRYTE, CASSITERITE -> 4
            BITUMINOUS, LIMONITE, CUPRITE -> 4
            ANTHRACITE, HEMATITE, MAGNETITE, STANNITE, CALAVERITE -> 3
            else -> -1
        }
    }

    private val OCEAN_VEINS = picker {
        when (it) {
            PEAT, LIGNITE -> 1
            else -> -1
        }
    }

    private val DESERT_VEINS = picker {
        when (it) {
            BITUMINOUS, ANTHRACITE -> 6
            HEMATITE, MAGNETITE, STANNITE, CALAVERITE, LIMONITE, CUPRITE -> 4
            PEAT, LIGNITE -> 1
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val RIVER_VEINS = picker {
        when (it) {
            PEAT, LIGNITE -> 1
            else -> -1
        }
    }

    private val SWAMP_VEINS = picker {
        when (it) {
            PEAT, LIGNITE -> 4
            CALAVERITE, CASSITERITE, CHALCOPRYTE, SIDERITE -> 2
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val MUSHROOM_VEINS = picker {
        when (it) {
            QUARTZ, CALAVERITE_NETHER -> -1
            else -> 1
        }
    }

    private val NETHER_VEINS = picker {
        when (it) {
            CALAVERITE_NETHER, QUARTZ -> 1
            else -> -1
        }
    }

    fun getList(biome: Biome): WeightedList<ChunkVeinType> =
        when (biome.category) {
            Biome.Category.TAIGA -> TAIGA_VEINS
            Biome.Category.EXTREME_HILLS -> HILLS_VEINS
            Biome.Category.JUNGLE -> JUNGLE_VEINS
            Biome.Category.MESA -> MESA_VEINS
            Biome.Category.PLAINS -> PLAINS_VEINS
            Biome.Category.SAVANNA -> SAVANNA_VEINS
            Biome.Category.ICY -> ICY_VEINS
            Biome.Category.BEACH -> BEACH_VEINS
            Biome.Category.FOREST -> FOREST_VEINS
            Biome.Category.OCEAN -> OCEAN_VEINS
            Biome.Category.DESERT -> DESERT_VEINS
            Biome.Category.RIVER -> RIVER_VEINS
            Biome.Category.SWAMP -> SWAMP_VEINS
            Biome.Category.MUSHROOM -> MUSHROOM_VEINS
            Biome.Category.NETHER -> NETHER_VEINS
            else -> OCEAN_VEINS
        }


    private fun picker(block: (ChunkVeinType) -> Int): WeightedList<ChunkVeinType> = WeightedList<ChunkVeinType>().apply {
        values().forEach {
            val i = block(it)
            if (i > 0)
                this.add(it, i)
        }
    }
}