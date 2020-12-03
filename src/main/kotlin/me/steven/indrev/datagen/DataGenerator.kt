package me.steven.indrev.datagen

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.util.Identifier
import java.io.File

abstract class DataGenerator<T>(val dir: File, val namespace: String, val fallback: (T) -> JsonFactory<T>) {

    protected val generators = HashMap<T, JsonFactory<T>>()

    init {
        dir.mkdirs()
    }

    operator fun get(obj: T): JsonFactory<T> = generators.getOrDefault(obj, fallback(obj))

    fun register(obj: T, factory: JsonFactory<T>) {
        generators[obj] = factory
    }

    fun register(obj: T, factory: (T) -> JsonObject?) {
        generators[obj] = object : JsonFactory<T> {
            override fun generate(): JsonObject? {
                return factory(obj)
            }
        }
    }

    fun generate(identifier: Identifier, obj: T): Boolean {
        val jsonFactory = this[obj]
        return generate(identifier, obj, jsonFactory)
    }

    fun generate(identifier: Identifier, obj: T, jsonFactory: JsonFactory<T>): Boolean {
        val file = File(dir, "${jsonFactory.getFileName(obj, identifier)}.json")
        file.parentFile.mkdirs()
        val output = jsonFactory.generate()
        if (output != null) {
            file.createNewFile()
            file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(output))
            return true
        }
        return false
    }

    abstract fun generate(): Int
}