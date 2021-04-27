package me.steven.indrev.components

interface ComponentProvider {
    fun <T> get(key: ComponentKey<T>): Any?
}