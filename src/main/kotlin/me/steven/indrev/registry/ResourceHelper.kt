package me.steven.indrev.registry

import com.google.common.collect.ImmutableList
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import java.util.function.Supplier

class ResourceHelper(private val id: String, private val block: ResourceHelper.() -> Unit) {

    fun withItems(vararg variants: String): ResourceHelper {
        variants.forEach { variant ->
            Registry.register(Registry.ITEM, identifier("${id}_$variant"), Item(itemSettings()))
        }
        return this
    }

    fun withOre(feature: (() -> ConfiguredFeature<*, *>)?): ResourceHelper {
        val ore =
            Block(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, 1).strength(3f, 3f))
        val identifier = identifier("${id}_ore")
        Registry.register(Registry.BLOCK, identifier, ore)
        Registry.register(Registry.ITEM, identifier, BlockItem(ore, itemSettings()))
        if (feature != null) {
            oreFeatures.add(feature)
            BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_FEATURE, identifier, feature())
        }
        return this
    }

    fun withTools(pickaxe: PickaxeItem, axe: AxeItem, shovel: ShovelItem, sword: SwordItem, hoe: HoeItem) {
        Registry.register(Registry.ITEM, identifier("${id}_pickaxe"), pickaxe)
        Registry.register(Registry.ITEM, identifier("${id}_axe"), axe)
        Registry.register(Registry.ITEM, identifier("${id}_shovel"), shovel)
        Registry.register(Registry.ITEM, identifier("${id}_sword"), sword)
        Registry.register(Registry.ITEM, identifier("${id}_hoe"), hoe)
    }

    fun withArmor(material: ArmorMaterial) {
        Registry.register(Registry.ITEM, identifier("${id}_helmet"), ArmorItem(material, EquipmentSlot.HEAD, itemSettings()))
        Registry.register(Registry.ITEM, identifier("${id}_chestplate"), ArmorItem(material, EquipmentSlot.CHEST, itemSettings()))
        Registry.register(Registry.ITEM, identifier("${id}_leggings"), ArmorItem(material, EquipmentSlot.LEGS, itemSettings()))
        Registry.register(Registry.ITEM, identifier("${id}_boots"), ArmorItem(material, EquipmentSlot.FEET, itemSettings()))
    }

    fun withBlock(): ResourceHelper {
        val block =
            Block(FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(5f, 6f))
        val id = identifier("${id}_block")
        Registry.register(Registry.BLOCK, id, block)
        Registry.register(Registry.ITEM, id, BlockItem(block, itemSettings()))
        return this
    }

    fun register() = block()

    companion object {
        private val oreFeatures = mutableListOf<() -> ConfiguredFeature<*, *>>()
        fun registerFeatures(biome: Biome) {
            if (biome.category != Biome.Category.NETHER && biome.category != Biome.Category.THEEND)
                oreFeatures.forEach { featureSupplier ->
                    val feature = featureSupplier()
                    val features = biome.generationSettings.features
                    val stepIndex = GenerationStep.Feature.UNDERGROUND_ORES.ordinal
                    while (features.size <= stepIndex) features.add(mutableListOf())
                    if (features[stepIndex] is ImmutableList)
                        features[stepIndex] = features[stepIndex].toMutableList()
                    features[stepIndex].add(Supplier { feature })
                }
        }

        val COPPER_FEATURE = {
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
        }


        val TIN_FEATURE = {
            Feature.ORE.configure(
                OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, IRRegistry.TIN_ORE().defaultState, 10)
            )
                .method_30377(48)
                .spreadHorizontally()
                .repeat(14)
        }

        val NIKOLITE_FEATURE = {
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

    }
}