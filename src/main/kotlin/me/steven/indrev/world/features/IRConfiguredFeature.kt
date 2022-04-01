package me.steven.indrev.world.features

import net.fabricmc.fabric.api.biome.v1.BiomeModification
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.tag.BiomeTags
import net.minecraft.util.Identifier
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.PlacedFeature


class IRConfiguredFeature(
    val identifier: Identifier,
    val step: GenerationStep.Feature,
    val configuredFeature: ConfiguredFeature<*, *>,
    val placedFeature: (ConfiguredFeature<*, *>) -> PlacedFeature,
    val biomePredicate: (BiomeSelectionContext) -> Boolean
) {
    val configuredFeatureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, identifier)
    val placedFeatureKey = RegistryKey.of(Registry.PLACED_FEATURE_KEY, identifier)
    init {
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, configuredFeatureKey.value, configuredFeature)
        BuiltinRegistries.add(BuiltinRegistries.PLACED_FEATURE, placedFeatureKey.value, placedFeature(configuredFeature))
    }

    companion object {
        //TODO
        val IS_OVERWORLD: (BiomeSelectionContext) -> Boolean = { ctx -> ctx.canGenerateIn(DimensionOptions.OVERWORLD) }
        val IS_NETHER: (BiomeSelectionContext) -> Boolean = { ctx -> ctx.canGenerateIn(DimensionOptions.NETHER) }
    }
}