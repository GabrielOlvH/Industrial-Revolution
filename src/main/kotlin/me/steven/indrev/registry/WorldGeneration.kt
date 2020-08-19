package me.steven.indrev.registry

import com.google.common.collect.ImmutableList
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.features.IRConfiguredFeature
import me.steven.indrev.world.features.SulfurCrystalFeature
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import java.util.function.Supplier

object WorldGeneration {
    fun init() {
        val config = IndustrialRevolution.CONFIG.oregen

        if (config.copper) {
            configuredFeatures.add(
                IRConfiguredFeature(
                    identifier("ore_copper"), GenerationStep.Feature.UNDERGROUND_ORES, copperFeature, IRConfiguredFeature.IS_OVERWORLD
                )
            )
        }
        if (config.tin) {
            configuredFeatures.add(
                IRConfiguredFeature(
                    identifier("ore_tin"), GenerationStep.Feature.UNDERGROUND_ORES, tinFeature, IRConfiguredFeature.IS_OVERWORLD
                )
            )
        }
        if (config.nikolite) {
            configuredFeatures.add(
                IRConfiguredFeature(
                    identifier("ore_nikolite"), GenerationStep.Feature.UNDERGROUND_ORES, nikoliteFeature, IRConfiguredFeature.IS_OVERWORLD
                )
            )
        }
        Registry.register(Registry.FEATURE, identifier("sulfur_crystal"), sulfurCrystalFeature)

        configuredFeatures.add(
            IRConfiguredFeature(
                identifier("sulfur_crystal"), GenerationStep.Feature.UNDERGROUND_DECORATION, sulfurFeature, IRConfiguredFeature.IS_OVERWORLD
            )
        )
        configuredFeatures.add(
            IRConfiguredFeature(
                identifier("sulfur_crystal_nether"), GenerationStep.Feature.UNDERGROUND_DECORATION, sulfurFeatureNether, IRConfiguredFeature.IS_NETHER
            )
        )
        configuredFeatures.add(
            IRConfiguredFeature(
                identifier("acid_lake"), GenerationStep.Feature.LAKES, acidLakesFeature
            ) { biome -> biome.category == Biome.Category.SWAMP }
        )
    }

    fun handleBiome(biome: Biome) {
        configuredFeatures.filter { it.biomePredicate(biome) }.forEach {
            val features = biome.generationSettings.features
            val stepIndex = it.step.ordinal
            while (features.size <= stepIndex) features.add(mutableListOf())
            var registeredFeatures = features[stepIndex]
            if (registeredFeatures is ImmutableList) {
                registeredFeatures = registeredFeatures.toMutableList()
                features[stepIndex] = registeredFeatures
            }
            registeredFeatures.add(Supplier { it.configuredFeature })
        }
    }

    val copperFeature: ConfiguredFeature<*, *> =
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

    val tinFeature: ConfiguredFeature<*, *> =
        Feature.ORE.configure(
            OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRRegistry.TIN_ORE().defaultState, 10)
        )
            .method_30377(48)
            .spreadHorizontally()
            .repeat(14)

    val nikoliteFeature: ConfiguredFeature<*, *> =
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

    private val sulfurCrystalFeature = SulfurCrystalFeature(SingleStateFeatureConfig.CODEC)

    val sulfurFeature: ConfiguredFeature<*, *> = sulfurCrystalFeature.configure(
        SingleStateFeatureConfig(
        IRRegistry.SULFUR_CRYSTAL_CLUSTER.defaultState)
    ).method_30377(16).repeat(10).spreadHorizontally()

    val sulfurFeatureNether: ConfiguredFeature<*, *> = sulfurCrystalFeature.configure(
        SingleStateFeatureConfig(
            IRRegistry.SULFUR_CRYSTAL_CLUSTER.defaultState)
    ).method_30377(100).repeat(10).spreadHorizontally()

    private val acidLakesFeature: ConfiguredFeature<*, *> = Feature.LAKE.configure(
        SingleStateFeatureConfig(IRRegistry.SULFURIC_ACID.defaultState)
    ).decorate(Decorator.WATER_LAKE.configure(ChanceDecoratorConfig(60)))


    private val configuredFeatures = mutableListOf<IRConfiguredFeature>()
}