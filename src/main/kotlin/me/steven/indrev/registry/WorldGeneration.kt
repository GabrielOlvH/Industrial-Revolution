package me.steven.indrev.registry

import com.google.common.collect.ImmutableList
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.identifier
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
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
    }

    fun handleBiome(biome: Biome) {
        val config = IndustrialRevolution.CONFIG.oregen
        if (biome.category != Biome.Category.NETHER && biome.category != Biome.Category.THEEND) {
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
}