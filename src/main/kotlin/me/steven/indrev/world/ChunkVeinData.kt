package me.steven.indrev.world

import me.steven.indrev.LOGGER
import net.minecraft.nbt.CompoundTag

class ChunkVeinData(var chunkVeinType: ChunkVeinType?, var size: Int, var explored: Int = 0) {
    fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("ChunkVeinType", chunkVeinType.toString())
        tag?.putInt("Size", size)
        tag?.putInt("Explored", explored)
        return tag ?: CompoundTag()
    }

    fun fromTag(tag: CompoundTag?) {
        if (tag?.contains("ChunkVeinType") == true && !tag.getString("ChunkVeinType").isNullOrEmpty())
            try {
                chunkVeinType = ChunkVeinType.valueOf(tag.getString("ChunkVeinType"))
            } catch (e: Throwable) {
                tag.remove("ChunkVeinType")
                LOGGER.warn("Miner had invalid chunk vein type \"${tag.getString("ChunkVeinType")}\"")
            }
        size = tag?.getInt("Size") ?: 0
        explored = tag?.getInt("Explored") ?: 0
    }
}