package me.steven.indrev.datagen.generators

import com.google.gson.JsonObject
import me.steven.indrev.datagen.DataGenerator
import me.steven.indrev.datagen.JsonFactory
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import java.io.File

class BlockModelGenerator(val root: File, namespace: String, fallback: (Block) -> JsonFactory<Block>)
    : DataGenerator<Block, JsonObject?>(File(root, "models/block"), namespace, fallback) {

    override fun generate(): Int {
        var count = 0
        generators.forEach { (block, _) ->
            if (generate(Registries.BLOCK.getId(block), block))
                count++
        }
        return count
    }

    companion object {
        val CUBE_ALL: (Block) -> JsonFactory<Block> = { item ->
            object : JsonFactory<Block> {
                override fun generate(): JsonObject {
                    val id = Registries.BLOCK.getId(item)
                    val obj = JsonObject()
                    obj.addProperty("parent", "block/cube_all")
                    val texturesObj = JsonObject()
                    texturesObj.addProperty("all", "${id.namespace}:block/${id.path}")
                    obj.add("textures", texturesObj)
                    return obj
                }
            }
        }
    }
}