package me.steven.indrev.registry

import com.google.common.collect.ImmutableList
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.features.SulfurCrystalFeature
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import java.util.function.Supplier

object WorldGeneration {
    fun init() {
        val config = IndustrialRevolution.CONFIG.oregen

        if (config.copper) {
            BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier("ore_copper"), copperFeature)
        }
        if (config.tin) {
            BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier("ore_tin"), tinFeature)
        }
        if (config.nikolite) {
            BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier("ore_nikolite"), nikoliteFeature)
        }
        Registry.register(Registry.FEATURE, identifier("sulfur_crystal"), sulfurCrystalFeature)
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier("sulfur_crystal"), sulfurFeature)
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier("acid_lake"), acidLakesFeature)
    }

    fun handleBiome(biome: Biome) {
        if (biome.category != Biome.Category.NETHER && biome.category != Biome.Category.THEEND) {
            addOres(biome)
            if (biome.category == Biome.Category.SWAMP)
                addLake(biome)
            addCrystal(biome)
        }
    }

    fun addLake(biome: Biome) {
        val features = biome.generationSettings.features
        val stepIndex = GenerationStep.Feature.LAKES.ordinal
        while (features.size <= stepIndex) features.add(mutableListOf())
        var lakes = features[stepIndex]
        if (lakes is ImmutableList) {
            lakes = lakes.toMutableList()
            features[stepIndex] = lakes
        }
        lakes.add(Supplier { acidLakesFeature })
    }

    fun addOres(biome: Biome) {
        val config = IndustrialRevolution.CONFIG.oregen
        val features = biome.generationSettings.features
        val stepIndex = GenerationStep.Feature.UNDERGROUND_ORES.ordinal
        while (features.size <= stepIndex) features.add(mutableListOf())
        var ores = features[stepIndex]
        if (ores is ImmutableList) {
            ores = ores.toMutableList()
            features[stepIndex] = ores
        }
        if (config.copper)
            ores.add(Supplier { copperFeature })
        if (config.tin)
            ores.add(Supplier { tinFeature })
        if (config.nikolite)
            ores.add(Supplier { nikoliteFeature })
    }

    fun addCrystal(biome: Biome) {
        val config = IndustrialRevolution.CONFIG.oregen
        val features = biome.generationSettings.features
        val stepIndex = GenerationStep.Feature.UNDERGROUND_DECORATION.ordinal
        while (features.size <= stepIndex) features.add(mutableListOf())
        var decoration = features[stepIndex]
        if (decoration is ImmutableList) {
            decoration = decoration.toMutableList()
            features[stepIndex] = decoration
        }
        decoration.add(Supplier { sulfurFeature })
    }

    val copperFeature =
        Feature.ORE.configure(
            OreFeatureConfig(
                OreFeatureConfig.Rules.BASE_STONE_OVERWORLD,
                IRRegistry.COPPER_ORE().defaultState,
                10
            )
        )
            .method_30377(64)
            .spreadHorizontally()
            .repeat(14)

    val tinFeature =
        Feature.ORE.configure(
            OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRRegistry.TIN_ORE().defaultState, 10)
        )
            .method_30377(48)
            .spreadHorizontally()
            .repeat(14)

    val nikoliteFeature =
        Feature.ORE.configure(
            OreFeatureConfig(
                OreFeatureConfig.Rules.BASE_STONE_OVERWORLD,
                IRRegistry.NIKOLITE_ORE().defaultState,
                7
            )
        )
            .method_30377(16)
            .spreadHorizontally()
            .repeat(8)

    val sulfurCrystalFeature = SulfurCrystalFeature(SingleStateFeatureConfig.CODEC)

    val sulfurFeature = sulfurCrystalFeature.configure(
        SingleStateFeatureConfig(
        IRRegistry.SULFUR_CRYSTAL_CLUSTER.defaultState)
    ).method_30377(16).repeat(30)

    val acidLakesFeature = Feature.LAKE.configure(
        SingleStateFeatureConfig(IRRegistry.SULFURIC_ACID.defaultState)
    ).decorate(Decorator.WATER_LAKE.configure(ChanceDecoratorConfig(60)))

}