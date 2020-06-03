package me.steven.indrev.registry

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.block.Block
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.decorator.ConfiguredDecorator
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.decorator.RangeDecoratorConfig
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig

class WorldGeneration {

    private val nikoliteFeature = configureOre(ModRegistry.NIKOLITE_ORE, Decorator.COUNT_RANGE.configure(RangeDecoratorConfig(8, 0, 0, 16)))

    private fun configureOre(ore: Block, decorator: ConfiguredDecorator<*>): ConfiguredFeature<*, *> {
        return Feature.ORE.configure(
            OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE, ore.defaultState, 8)
        ).createDecoratedFeature(decorator)
    }

    private fun addFeature(feature: ConfiguredFeature<*, *>, step: GenerationStep.Feature, biome: Biome) {
        biome.addFeature(step, feature)
    }

    private fun handleBiome(biome: Biome) {
        arrayOf(nikoliteFeature).forEach {
            if (biome.category !== Biome.Category.NETHER && biome.category !== Biome.Category.THEEND)
                addFeature(it, GenerationStep.Feature.UNDERGROUND_ORES, biome)
        }
    }

    fun registerAll() {
        Registry.BIOME.forEach { handleBiome(it) }
        RegistryEntryAddedCallback.event(Registry.BIOME).register(RegistryEntryAddedCallback { _, _, biome -> handleBiome(biome) })
    }
}