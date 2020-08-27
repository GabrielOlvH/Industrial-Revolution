package me.steven.indrev.world.features

import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature

class IRConfiguredFeature(
    val step: GenerationStep.Feature,
    val configuredFeature: RegistryKey<ConfiguredFeature<*,*>>,
    val biomePredicate: (Biome) -> Boolean
) {
    init {
        //BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier, configuredFeature)
    }

    companion object {
        val IS_OVERWORLD: (Biome) -> Boolean = { biome -> biome.category != Biome.Category.NETHER && biome.category != Biome.Category.THEEND }
        val IS_NETHER: (Biome) -> Boolean = { biome -> biome.category == Biome.Category.NETHER }
    }
}