package me.steven.indrev.registry

import com.google.common.collect.ImmutableList
import me.steven.indrev.config.IRConfig
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.features.IRConfiguredFeature
import me.steven.indrev.world.features.SulfurCrystalFeature
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.minecraft.block.Blocks
import net.minecraft.util.math.intprovider.UniformIntProvider
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryEntry
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.feature.*
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier
import net.minecraft.world.gen.stateprovider.BlockStateProvider

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

    fun addFeatures() {
        configuredFeatures.forEach { feature ->
            BiomeModifications.addFeature(
                { ctx -> feature.biomePredicate(ctx) },
                feature.step,
                feature.placedFeatureKey
            )
        }
    }

    private val tinTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.TIN_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_TIN_ORE().defaultState
        )
    )

    private val tinFeature =
        IRConfiguredFeature(
            identifier("tin_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ConfiguredFeature(Feature.ORE, OreFeatureConfig(tinTargets, 10)),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(14),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-48), YOffset.fixed(48))
                    )
                )
            },
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val leadTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.LEAD_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_LEAD_ORE().defaultState
        )
    )

    private val leadFeature =
        IRConfiguredFeature(
            identifier("lead_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ConfiguredFeature(Feature.ORE, OreFeatureConfig(leadTargets, 6)),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(11),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-32), YOffset.fixed(32))
                    )
                )
            },
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val silverTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.SILVER_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_SILVER_ORE().defaultState
        )
    )

    private val silverFeature =
        IRConfiguredFeature(
            identifier("silver_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ConfiguredFeature(Feature.ORE, OreFeatureConfig(silverTargets, 8)),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(9),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-32), YOffset.fixed(32))
                    )
                )
            },
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val tungstenTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_TUNGSTEN_ORE().defaultState
        )
    )

    private val tungstenFeature =
        IRConfiguredFeature(
            identifier("tungsten_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ConfiguredFeature(Feature.ORE, OreFeatureConfig(tungstenTargets, 5)),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(8),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-16), YOffset.fixed(16))
                    )
                )
            },
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val nikoliteTargets = ImmutableList.of(
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.STONE_ORE_REPLACEABLES,
            IRBlockRegistry.NIKOLITE_ORE().defaultState
        ),
        OreFeatureConfig.createTarget(
            OreConfiguredFeatures.DEEPSLATE_ORE_REPLACEABLES,
            IRBlockRegistry.DEEPSLATE_NIKOLITE_ORE().defaultState
        )
    )

    private val nikoliteFeature =
        IRConfiguredFeature(
            identifier("nikolite_ore"),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ConfiguredFeature(Feature.ORE, OreFeatureConfig(nikoliteTargets, 7)),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(8),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.trapezoid(YOffset.aboveBottom(-16), YOffset.fixed(16))
                    )
                )
            },
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
            ConfiguredFeature(sulfurCrystalFeature, DefaultFeatureConfig.INSTANCE),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(12),
                        HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(16))
                    )
                )
            },
            IRConfiguredFeature.IS_OVERWORLD
        )

    private val sulfurFeatureNether =
        IRConfiguredFeature(
            identifier("sulfur_crystal_nether"),
            GenerationStep.Feature.UNDERGROUND_DECORATION,
            ConfiguredFeature(sulfurCrystalFeature, DefaultFeatureConfig.INSTANCE),
            { feature ->
                PlacedFeature(
                    RegistryEntry.of(feature),
                    listOf(
                        CountPlacementModifier.of(20),
                        HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.getTop())
                    )
                )
            },
            IRConfiguredFeature.IS_NETHER
        )

    private val acidLakesFeature = IRConfiguredFeature(
        identifier("sulfuric_acid_lake"),
        GenerationStep.Feature.LAKES,
        ConfiguredFeature(
            Feature.LAKE,
            LakeFeature.Config(
                BlockStateProvider.of(IRFluidRegistry.SULFURIC_ACID.defaultState),
                BlockStateProvider.of(Blocks.COARSE_DIRT.defaultState)
            )
        ),
        { feature ->
            PlacedFeature(
                RegistryEntry.of(feature),
                listOf(
                    CountPlacementModifier.of(UniformIntProvider.create(0, 60))
                )
            )
        }
    ) { ctx -> ctx.biomeKey == BiomeKeys.SWAMP }
}