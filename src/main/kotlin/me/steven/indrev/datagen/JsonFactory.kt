package me.steven.indrev.datagen

import com.google.gson.JsonObject
import net.minecraft.util.Identifier

interface JsonFactory<T> {
    fun generate(): JsonObject?

    fun getFileName(t: T, id: Identifier): String = id.path

    companion object {
        fun <T> nullFactory(): JsonFactory<T> = object : JsonFactory<T> {
            override fun generate(): JsonObject? = null
        }
    }
}