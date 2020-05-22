package me.steven.indrev.content

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.decorator.RangeDecoratorConfig
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig

val COPPER_FEATURE: ConfiguredFeature<*, *> = Feature.ORE.configure(OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, ItemRegistry.COPPER_ORE.defaultState, 8))
    .createDecoratedFeature(
        Decorator.COUNT_RANGE.configure(
            RangeDecoratorConfig(8, 0, 0, 64)
        )
    )


val TIN_FEATURE: ConfiguredFeature<*, *> = Feature.ORE.configure(OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, ItemRegistry.TIN_ORE.defaultState, 8))
    .createDecoratedFeature(
        Decorator.COUNT_RANGE.configure(
            RangeDecoratorConfig(8, 0, 0, 64)
        )
    )

fun registerWorldFeatures() {
    Registry.BIOME.forEach { handleBiome(it) }
    RegistryEntryAddedCallback.event(Registry.BIOME).register(RegistryEntryAddedCallback { _, _, biome -> handleBiome(biome) })
}

private fun handleBiome(biome: Biome) {
    if (biome.category !== Biome.Category.NETHER && biome.category !== Biome.Category.THEEND) {
        biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES,  COPPER_FEATURE)
        biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, TIN_FEATURE)
    }
}