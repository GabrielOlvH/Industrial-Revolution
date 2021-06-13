package me.steven.indrev.world.features

import net.minecraft.util.Identifier
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature


class IRConfiguredFeature(
    val identifier: Identifier,
    val step: GenerationStep.Feature,
    val configuredFeature: ConfiguredFeature<*,*>,
    val biomePredicate: (Biome) -> Boolean
) {
    val key = RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, identifier)
    init {
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, key.value, configuredFeature)
    }

    companion object {
        val IS_OVERWORLD: (Biome) -> Boolean = { biome -> biome.category != Biome.Category.NETHER && biome.category != Biome.Category.THEEND }
        val IS_NETHER: (Biome) -> Boolean = { biome -> biome.category == Biome.Category.NETHER }
    }
}