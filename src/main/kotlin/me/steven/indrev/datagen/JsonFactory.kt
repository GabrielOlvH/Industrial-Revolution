package me.steven.indrev.datagen

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File

interface JsonFactory<T> : DataFactory<T, JsonObject?> {

    override val extension: String get() = "json"

    override fun write(file: File, t: JsonObject?) {
        file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(t))
    }

    companion object {
        fun <T> nullFactory(): JsonFactory<T> = object : JsonFactory<T> {
            override fun generate(): JsonObject? = null
        }
    }
}