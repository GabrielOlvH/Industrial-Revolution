package me.steven.indrev.datagen.generators

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.steven.indrev.datagen.DataGenerator
import me.steven.indrev.datagen.JsonFactory
import net.minecraft.util.Identifier
import java.io.File

class MaterialTagGenerator(val root: File, namespace: String, fallback: (String) -> JsonFactory<String>)
    : DataGenerator<String>(File(root, "tags/items"), namespace, fallback) {

    override fun generate(): Int {
        var count = 0
        generators.forEach { (id, _) ->
            if (generate(Identifier(id), id))
                count++
        }
        return count
    }

    companion object {
        val DEFAULT_ITEM: (String) -> JsonFactory<String> = { tag ->
            object : JsonFactory<String> {
                override fun generate(): JsonObject? {
                    val obj = JsonObject()
                    obj.addProperty("replace", false)
                    val values = JsonArray()
                    values.add(tag)
                    obj.add("values", values)
                    return obj
                }

                override fun getFileName(t: String, id: Identifier): String = "${super.getFileName(t, id)}s"
            }
        }
    }
}