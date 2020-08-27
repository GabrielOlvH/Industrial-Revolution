package me.steven.indrev.registry

import me.shedaniel.cloth.api.dynamic.registry.v1.BiomesRegistry
import me.shedaniel.cloth.api.dynamic.registry.v1.DynamicRegistryCallback
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.features.IRConfiguredFeature
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep

object WorldGeneration {
    fun init() {
        val config = IndustrialRevolution.CONFIG.oregen

        if (config.copper) {
            val copperFeature = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, identifier("copper_ore"))
            configuredFeatures.add(
                IRConfiguredFeature(
                    GenerationStep.Feature.UNDERGROUND_ORES, copperFeature, IRConfiguredFeature.IS_OVERWORLD
                )
            )
        }
        if (config.tin) {
            val tinFeature = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, identifier("tin_ore"))
            configuredFeatures.add(
                IRConfiguredFeature(
                    GenerationStep.Feature.UNDERGROUND_ORES, tinFeature, IRConfiguredFeature.IS_OVERWORLD
                )
            )
        }
        if (config.nikolite) {
            val nikoliteFeature = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, identifier("nikolite_ore"))
            configuredFeatures.add(
                IRConfiguredFeature(
                    GenerationStep.Feature.UNDERGROUND_ORES, nikoliteFeature, IRConfiguredFeature.IS_OVERWORLD
                )
            )
        }
        if (config.sulfurCrystals) {
            val sulfurFeatureOverworld = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, identifier("sulfur_crystal_overworld"))
            configuredFeatures.add(
                IRConfiguredFeature(
                    GenerationStep.Feature.UNDERGROUND_DECORATION, sulfurFeatureOverworld, IRConfiguredFeature.IS_OVERWORLD
                )
            )
            val sulfurFeatureNether = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, identifier("sulfur_crystal_nether"))
            configuredFeatures.add(
                IRConfiguredFeature(
                    GenerationStep.Feature.UNDERGROUND_DECORATION, sulfurFeatureNether, IRConfiguredFeature.IS_NETHER
                )
            )
        }
        if (config.sulfuricAcidLake) {
            val acidLakesFeature = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, identifier("sulfuric_acid_lake"))
            configuredFeatures.add(
                IRConfiguredFeature(
                    GenerationStep.Feature.LAKES, acidLakesFeature
                ) { biome -> biome.category == Biome.Category.SWAMP }
            )
        }
    }

    fun registerCallback() {
        DynamicRegistryCallback.callback(Registry.BIOME_KEY).register(DynamicRegistryCallback { manager, key, biome ->
            configuredFeatures.forEach { feature ->
                if (feature.biomePredicate(biome)) {
                    BiomesRegistry.registerFeature(manager, biome, feature.step, feature.configuredFeature)
                }
            }
        })

    }

    private val configuredFeatures = mutableListOf<IRConfiguredFeature>()
}