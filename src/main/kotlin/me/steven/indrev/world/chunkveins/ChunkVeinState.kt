package me.steven.indrev.world.chunkveins

import me.steven.indrev.utils.identifier
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.PersistentState

class ChunkVeinState : PersistentState() {

    val veins: MutableMap<ChunkPos, ChunkVeinData> = mutableMapOf()

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        val t = tag ?: NbtCompound()
        veins.filter { (_, data) -> data.veinIdentifier.toString() != "indrev:empty" }.forEach { (chunkPos, veinData) ->
            t.put("${chunkPos.x},${chunkPos.z}", veinData.writeNbt(NbtCompound()))
        }
        return t
    }

    companion object {
        const val STATE_OVERWORLD_KEY = "indrev_veins"
        fun readNbt(tag: NbtCompound?): ChunkVeinState {
            val state = ChunkVeinState()
            tag?.keys?.forEach { key ->
                val chunkVeinData = ChunkVeinData(identifier("empty"), 0)
                chunkVeinData.readNbt(tag.getCompound(key))
                val index = key.indexOf(',')
                val x = key.substring(0, index).toInt()
                val z = key.substring(index + 1).toInt()
                val chunkPos = ChunkPos(x, z)
                state.veins[chunkPos] = chunkVeinData
            }
            return state
        }

        fun getState(world: ServerWorld): ChunkVeinState {
            return world.persistentStateManager.getOrCreate(
                { tag -> readNbt(tag) },
                { ChunkVeinState() },
                STATE_OVERWORLD_KEY
            )
        }
    }
}