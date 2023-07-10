package me.steven.indrev.world.features

import net.fabricmc.fabric.api.biome.v1.BiomeModification
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.dimension.DimensionOptions
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.ConfiguredFeatures
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.placementmodifier.PlacementModifier


class IRConfiguredFeature(
    val identifier: Identifier,
    val step: GenerationStep.Feature,
    val configuredFeature: ConfiguredFeature<*, *>,
    val modifiers: List<PlacementModifier>,
    val biomePredicate: (BiomeSelectionContext) -> Boolean
) {
    val configuredFeatureKey = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, identifier)
    val placedFeatureKey = RegistryKey.of(RegistryKeys.PLACED_FEATURE, identifier)
    init {

        /*BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, configuredFeatureKey.value, configuredFeature)
        BuiltinRegistries.add(BuiltinRegistries.PLACED_FEATURE, placedFeatureKey.value, placedFeature(configuredFeature))*/
    }

    companion object {
        //TODO
        val IS_OVERWORLD: (BiomeSelectionContext) -> Boolean = { ctx -> ctx.canGenerateIn(DimensionOptions.OVERWORLD) }
        val IS_NETHER: (BiomeSelectionContext) -> Boolean = { ctx -> ctx.canGenerateIn(DimensionOptions.NETHER) }
    }
}