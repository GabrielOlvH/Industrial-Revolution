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
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import java.util.function.Supplier

object WorldGeneration {
    fun init() {
        val config = IndustrialRevolution.CONFIG.oregen

        if (config.copper) {
            configuredFeatures.add(copperFeature)
        }
        if (config.tin) {
            configuredFeatures.add(tinFeature)
        }
        if (config.nikolite) {
            configuredFeatures.add(nikoliteFeature)
        }
        if (config.lead) {
            configuredFeatures.add(leadFeature)
        }
        if (config.tungsten) {
            configuredFeatures.add(tungstenFeature)
        }
        if (config.silver) {
            configuredFeatures.add(silverFeature)
        }
        if (config.sulfurCrystals) {
            configuredFeatures.add(sulfurFeatureOverworld)
            configuredFeatures.add(sulfurFeatureNether)
        }
        if (config.sulfuricAcidLake) {
            configuredFeatures.add(acidLakesFeature)
        }
    }

    private val configuredFeatures = mutableListOf<IRConfiguredFeature>()

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

    private val copperFeature =
        IRConfiguredFeature(
            identifier("copper_ore"), GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE.configure(
            OreFeatureConfig(
                OreFeatureConfig.Rules.BASE_STONE_OVERWORLD,
                IRBlockRegistry.COPPER_ORE().defaultState,
                10
            )
        )
            .rangeOf(64)
            .spreadHorizontally()
            .repeat(14), IRConfiguredFeature.IS_OVERWORLD)

    private val tinFeature =
        IRConfiguredFeature(
            identifier("tin_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(
                OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRBlockRegistry.TIN_ORE().defaultState, 10)
            )
                .rangeOf(48)
                .spreadHorizontally()
                .repeat(14),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val leadFeature =
        IRConfiguredFeature(
            identifier("lead_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(
                OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRBlockRegistry.LEAD_ORE().defaultState, 6)
            )
                .rangeOf(32)
                .spreadHorizontally()
                .repeat(12),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val silverFeature =
        IRConfiguredFeature(
            identifier("silver_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(
                OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRBlockRegistry.SILVER_ORE().defaultState, 8)
            )
                .rangeOf(32)
                .spreadHorizontally()
                .repeat(8),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val tungstenFeature =
        IRConfiguredFeature(
            identifier("tungsten_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(
                OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRBlockRegistry.TUNGSTEN_ORE().defaultState, 5)
            )
                .rangeOf(16)
                .spreadHorizontally()
                .repeat(5),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val nikoliteFeature =
        IRConfiguredFeature(
            identifier("nikolite_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(
                OreFeatureConfig(
                    OreFeatureConfig.Rules.BASE_STONE_OVERWORLD,
                    IRBlockRegistry.NIKOLITE_ORE().defaultState,
                    7
                )
            )
                .rangeOf(16)
                .spreadHorizontally()
                .repeat(6),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val sulfurCrystalFeature: SulfurCrystalFeature = Registry.register(
        Registry.FEATURE,
        identifier("sulfur_crystal"),
        SulfurCrystalFeature(DefaultFeatureConfig.CODEC)
    )

    private val sulfurFeatureOverworld =
        IRConfiguredFeature(
            identifier("sulfur_crystal_overworld"),
            GenerationStep.Feature.UNDERGROUND_DECORATION,
            sulfurCrystalFeature.configure(DefaultFeatureConfig.INSTANCE).rangeOf(16).repeat(10),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val sulfurFeatureNether =
        IRConfiguredFeature(
            identifier("sulfur_crystal_nether"),
            GenerationStep.Feature.UNDERGROUND_DECORATION,
            sulfurCrystalFeature.configure(DefaultFeatureConfig.INSTANCE).rangeOf(100).repeat(20),
            IRConfiguredFeature.IS_NETHER
        )

    private val acidLakesFeature = IRConfiguredFeature(
        identifier("sulfuric_acid_lake"),
        GenerationStep.Feature.LAKES,
        Feature.LAKE.configure(
            SingleStateFeatureConfig(IRFluidRegistry.SULFURIC_ACID.defaultState)
        ).decorate(Decorator.WATER_LAKE.configure(ChanceDecoratorConfig(60)))
    ) { biome -> biome.category == Biome.Category.SWAMP }
}