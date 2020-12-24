package me.steven.indrev.datagen.generators

import com.google.gson.JsonObject
import me.steven.indrev.datagen.DataGenerator
import me.steven.indrev.datagen.JsonFactory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry
import java.io.File

class ItemModelGenerator(val root: File, namespace: String, fallback: (Item) -> JsonFactory<Item>)
    : DataGenerator<Item, JsonObject?>(File(root, "models/item"), namespace, fallback) {

    override fun generate(): Int {
        var count = 0
        Registry.ITEM.ids.filter { id -> id.namespace == namespace }.forEach {
            val item = Registry.ITEM.get(it)
            if (item.asItem() != null && generate(it, item)) {
                count++
            }
        }
        return count
    }

    companion object {
        val DEFAULT_ITEM: (Item) -> JsonFactory<Item> = { item ->
            object : JsonFactory<Item> {
                override fun generate(): JsonObject {
                    val id = Registry.ITEM.getId(item)
                    val obj = JsonObject()
                    if (item is BlockItem) {
                        obj.addProperty("parent", "${id.namespace}:block/${id.path}")
                    } else {
                        obj.addProperty("parent", "item/generated")
                        val texturesObj = JsonObject()
                        texturesObj.addProperty("layer0", "${id.namespace}:item/${id.path}")
                        obj.add("textures", texturesObj)
                    }
                    return obj
                }

            }
        }
        val HANDHELD: (Item) -> JsonFactory<Item> = { item ->
            object : JsonFactory<Item> {
                override fun generate(): JsonObject {
                    val id = Registry.ITEM.getId(item)
                    val obj = JsonObject()
                    obj.addProperty("parent", "item/handheld")
                    val texturesObj = JsonObject()
                    texturesObj.addProperty("layer0", "${id.namespace}:item/${id.path}")
                    obj.add("textures", texturesObj)
                    return obj
                }
            }
        }
    }
}