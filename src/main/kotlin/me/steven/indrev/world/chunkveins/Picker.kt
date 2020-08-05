package me.steven.indrev.world.chunkveins

import net.minecraft.util.Identifier
import net.minecraft.util.collection.WeightedList

data class Picker(val biome: Identifier, val veins: WeightedList<Identifier>) {
    companion object {
        val PICKERS = mutableMapOf<Identifier, Picker>()
    }
}