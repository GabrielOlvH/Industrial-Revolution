package me.steven.indrev.components

interface ComponentProvider {
    fun <T> get(key: ComponentKey<T>): Any?
}

fun ensureIsProvider(any: Any) = any as? ComponentProvider ?: error("$any does not implement ComponentProvider!")