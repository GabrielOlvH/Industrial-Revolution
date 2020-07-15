package me.steven.indrev.registry

import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.decorator.Decorator
import net.minecraft.world.gen.decorator.RangeDecoratorConfig
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig

class ResourceHelper(private vararg val ids: String, private val block: ResourceHelper.() -> Unit) {

    fun withItems(vararg variants: String): ResourceHelper {
        variants.forEach { variant ->
            ids.forEach { id ->
                Registry.register(Registry.ITEM, identifier("${id}_$variant"), Item(itemSettings()))
            }
        }
        return this
    }

    fun withOre(config: (String) -> IROreFeature): ResourceHelper {
        ids.forEach {
            val ore =
                Block(FabricBlockSettings.of(Material.STONE).breakByTool(FabricToolTags.PICKAXES, 2).strength(3f, 3f))
            val id = identifier("${it}_ore")
            Registry.register(Registry.BLOCK, id, ore)
            Registry.register(Registry.ITEM, id, BlockItem(ore, itemSettings()))

            val netherOre =
                Block(FabricBlockSettings.of(Material.STONE).breakByTool(FabricToolTags.PICKAXES, 2).strength(3f, 3f))
            val netherId = identifier("${it}_nether_ore")
            Registry.register(Registry.BLOCK, netherId, netherOre)
            Registry.register(Registry.ITEM, netherId, BlockItem(netherOre, itemSettings()))
            oreFeatures.add(config(it))
        }
        return this
    }

    fun withBlock(): ResourceHelper {
        ids.forEach {
            val block =
                Block(FabricBlockSettings.of(Material.METAL).breakByTool(FabricToolTags.PICKAXES, 2).strength(5f, 6f))
            val id = identifier("${it}_block")
            Registry.register(Registry.BLOCK, id, block)
            Registry.register(Registry.ITEM, id, BlockItem(block, itemSettings()))
        }
        return this
    }

    fun register() = block()

    class IROreFeature(feature: (OreFeatureConfig.Target) -> ConfiguredFeature<*, *>) {
        val overworldFeature = feature(OreFeatureConfig.Target.NATURAL_STONE)
        val netherFeature: ConfiguredFeature<*, *>? = feature(OreFeatureConfig.Target.NETHER_ORE_REPLACEABLES)
    }

    companion object {
        private val oreFeatures = mutableListOf<IROreFeature>()
        fun registerFeatures(biome: Biome) {

            if (biome.category == Biome.Category.NETHER)
                oreFeatures.forEach {
                    val netherFeature = it.netherFeature
                    if (netherFeature != null)
                        biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, netherFeature)
                }
            else
                oreFeatures.forEach { biome.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, it.overworldFeature) }
        }

        val COPPER_FEATURE: IROreFeature = IROreFeature { target ->
            Feature.ORE.configure(
                OreFeatureConfig(
                    target,
                    if (target == OreFeatureConfig.Target.NETHER_ORE_REPLACEABLES)
                        IRRegistry.COPPER_NETHER_ORE.defaultState
                    else
                        IRRegistry.COPPER_ORE.defaultState,
                    10
                )
            )
                .createDecoratedFeature(Decorator.COUNT_RANGE.configure(RangeDecoratorConfig(20, 0, 0, 128)))
        }

        val TIN_FEATURE: IROreFeature = IROreFeature { target ->
            Feature.ORE.configure(
                OreFeatureConfig(
                    target,
                    if (target == OreFeatureConfig.Target.NETHER_ORE_REPLACEABLES)
                        IRRegistry.TIN_NETHER_ORE.defaultState
                    else
                        IRRegistry.TIN_ORE.defaultState,
                    10
                )
            )
                .createDecoratedFeature(Decorator.COUNT_RANGE.configure(RangeDecoratorConfig(20, 0, 0, 64)))
        }

        val NIKOLITE_FEATURE: IROreFeature = IROreFeature { target ->
            Feature.ORE.configure(
                OreFeatureConfig(
                    target,
                    if (target == OreFeatureConfig.Target.NETHER_ORE_REPLACEABLES)
                        IRRegistry.NIKOLITE_NETHER_ORE.defaultState
                    else
                        IRRegistry.NIKOLITE_ORE.defaultState,
                    10
                )
            )
                .createDecoratedFeature(Decorator.COUNT_RANGE.configure(RangeDecoratorConfig(20, 0, 0, 64)))
        }
    }
}