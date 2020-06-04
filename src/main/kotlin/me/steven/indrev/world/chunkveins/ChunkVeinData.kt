package me.steven.indrev.world.chunkveins

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
            chunkVeinType = ChunkVeinType.valueOf(tag.getString("ChunkVeinType"))
        size = tag?.getInt("Size") ?: 0
        explored = tag?.getInt("Explored") ?: 0
    }
}