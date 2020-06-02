package me.steven.indrev.world

import net.minecraft.nbt.CompoundTag

class ChunkVeinData(var chunkVeinType: ChunkVeinType?, var size: Int) {
    fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("ChunkVeinType", chunkVeinType.toString())
        tag?.putInt("Size", size)
        return tag ?: CompoundTag()
    }

    fun fromTag(tag: CompoundTag?) {
        chunkVeinType = ChunkVeinType.valueOf(tag?.getString("ChunkVeinType"))
        size = tag?.getInt("Size") ?: 0
    }
}