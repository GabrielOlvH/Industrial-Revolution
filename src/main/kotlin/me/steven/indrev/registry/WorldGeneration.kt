package me.steven.indrev.registry

import com.google.common.collect.ImmutableList
import me.steven.indrev.config.IRConfig
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.features.IRConfiguredFeature
import me.steven.indrev.world.features.SulfurCrystalFeature
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.feature.SingleStateFeatureConfig
import java.util.function.Supplier

object WorldGeneration {
    fun init() {
        val config = IRConfig.oregen

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

    private val tinTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.TIN_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_TIN_ORE().defaultState
        )
    )

    private val tinFeature =
        IRConfiguredFeature(
            identifier("tin_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(OreFeatureConfig(tinTargets, 10))
                .uniformRange(YOffset.getBottom(), YOffset.fixed(48))
                .spreadHorizontally()
                .repeat(14),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val leadTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.LEAD_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_LEAD_ORE().defaultState
        )
    )

    private val leadFeature =
        IRConfiguredFeature(
            identifier("lead_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(OreFeatureConfig(leadTargets, 6))
                .uniformRange(YOffset.getBottom(), YOffset.fixed(32))
                .spreadHorizontally()
                .repeat(12),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val silverTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.SILVER_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_SILVER_ORE().defaultState
        )
    )

    private val silverFeature =
        IRConfiguredFeature(
            identifier("silver_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(OreFeatureConfig(silverTargets, 8))
                .uniformRange(YOffset.getBottom(), YOffset.fixed(32))
                .spreadHorizontally()
                .repeat(8),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val tungstenTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_TUNGSTEN_ORE().defaultState
        )
    )

    private val tungstenFeature =
        IRConfiguredFeature(
            identifier("tungsten_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(OreFeatureConfig(tungstenTargets, 5))
                .uniformRange(YOffset.getBottom(), YOffset.fixed(0))
                .spreadHorizontally()
                .repeat(6),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val nikoliteTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.NIKOLITE_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_NIKOLITE_ORE().defaultState
        )
    )

    private val nikoliteFeature =
        IRConfiguredFeature(
            identifier("nikolite_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            Feature.ORE.configure(OreFeatureConfig(nikoliteTargets, 7))
                .uniformRange(YOffset.getBottom(), YOffset.fixed(16))
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
            sulfurCrystalFeature.configure(DefaultFeatureConfig.INSTANCE).uniformRange(YOffset.getBottom(), YOffset.fixed(8)).repeat(10),
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val sulfurFeatureNether =
        IRConfiguredFeature(
            identifier("sulfur_crystal_nether"),
            GenerationStep.Feature.UNDERGROUND_DECORATION,
            sulfurCrystalFeature.configure(DefaultFeatureConfig.INSTANCE).uniformRange(YOffset.getBottom(), YOffset.getTop()).repeat(20),
            IRConfiguredFeature.IS_NETHER
        )

    private val acidLakesFeature = IRConfiguredFeature(
        identifier("sulfuric_acid_lake"),
        GenerationStep.Feature.LAKES,
        Feature.LAKE.configure(
            SingleStateFeatureConfig(IRFluidRegistry.SULFURIC_ACID.defaultState)
        ).decorate(Decorator.LAVA_LAKE.configure(ChanceDecoratorConfig(60)))
    ) { biome -> biome.category == Biome.Category.SWAMP }
}