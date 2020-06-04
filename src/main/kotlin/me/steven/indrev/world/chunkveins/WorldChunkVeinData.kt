package me.steven.indrev.world.chunkveins

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.PersistentState

class WorldChunkVeinData : PersistentState(STATE_KEY) {

    val veins: MutableMap<ChunkPos, ChunkVeinData> = mutableMapOf()

    override fun toTag(tag: CompoundTag?): CompoundTag {
        val t = tag ?: CompoundTag()
        veins.forEach { (chunkPos, veinData) ->
            t.put("${chunkPos.x},${chunkPos.z}", veinData.toTag(CompoundTag()))
        }
        return t
    }

    override fun fromTag(tag: CompoundTag?) {
        tag?.keys?.forEach { key ->
            val chunkVeinData = ChunkVeinData(null, 0)
            chunkVeinData.fromTag(tag.getCompound(key))
            val index = key.indexOf(',')
            val x = key.substring(0, index).toInt()
            val z = key.substring(index + 1).toInt()
            val chunkPos = ChunkPos(x, z)
            veins[chunkPos] = chunkVeinData
        }
    }

    companion object {
        const val STATE_KEY = "indrev_world_veins"
    }
}