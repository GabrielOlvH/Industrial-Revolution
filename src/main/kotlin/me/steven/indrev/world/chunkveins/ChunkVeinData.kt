package me.steven.indrev.world.chunkveins

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

data class ChunkVeinData(var veinIdentifier: Identifier, var size: Int, var explored: Int = 0) {

    val translationKey = "vein.${veinIdentifier.namespace}.${veinIdentifier.path}"

    fun writeNbt(tag: NbtCompound?): NbtCompound {
        tag?.putString("VeinIdentifier", veinIdentifier.toString())
        tag?.putInt("Size", size)
        tag?.putInt("Explored", explored)
        return tag ?: NbtCompound()
    }

    fun readNbt(tag: NbtCompound?) {
        if (tag?.contains("VeinIdentifier") == true && !tag.getString("VeinIdentifier").isNullOrEmpty())
            veinIdentifier = Identifier(tag.getString("VeinIdentifier"))
        size = tag?.getInt("Size") ?: 0
        explored = tag?.getInt("Explored") ?: 0
    }
}