package me.steven.indrev.blockentities

import me.steven.indrev.components.ComponentProvider

interface Syncable : ComponentProvider {
    fun markForUpdate(condition: () -> Boolean = { true })
}