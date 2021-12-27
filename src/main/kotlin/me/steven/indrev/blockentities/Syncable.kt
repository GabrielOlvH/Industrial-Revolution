package me.steven.indrev.blockentities

interface Syncable {
    fun markForUpdate(condition: () -> Boolean = { true })
}