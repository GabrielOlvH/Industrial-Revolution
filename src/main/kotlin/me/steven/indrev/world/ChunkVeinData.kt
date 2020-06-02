package me.steven.indrev.world

import net.minecraft.nbt.CompoundTag

class ChunkVeinData(var chunkVeinType: ChunkVeinType?, var richness: Int) {
    fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("ChunkVeinType", chunkVeinType.toString())
        tag?.putInt("Richness", richness)
        return tag ?: CompoundTag()
    }

    fun fromTag(tag: CompoundTag?) {
        chunkVeinType = ChunkVeinType.valueOf(tag?.getString("ChunkVeinType"))
        richness = tag?.getInt("Richness") ?: 0
    }
}