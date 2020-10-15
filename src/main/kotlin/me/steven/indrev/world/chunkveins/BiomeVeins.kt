package me.steven.indrev.world.chunkveins

import net.minecraft.util.Identifier
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.Biome

data class BiomeVeins(val biome: RegistryKey<Biome>, val veins: WeightedList<Identifier>) {
    companion object {
        val BIOME_VEINS = mutableMapOf<RegistryKey<Biome>, BiomeVeins>()
    }
}