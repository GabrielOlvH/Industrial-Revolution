package me.steven.indrev.world

import net.minecraft.block.Block
import net.minecraft.block.Blocks

enum class ChunkVeinType(vararg val ores: Block) {
    COAL(Blocks.COAL_ORE),
    IRON(Blocks.IRON_ORE);

    companion object {
        fun valueOf(string: String?): ChunkVeinType? {
            return when (string) {
                "COAL" -> COAL
                "IRON" -> IRON
                else -> null
            }
        }
    }
}