package me.steven.indrev.datagen

import me.steven.indrev.items.*
import me.steven.indrev.recipes.ALLOY_SMELTER_RECIPE_TYPE
import me.steven.indrev.recipes.CHEMICAL_INFUSER_RECIPE_TYPE
import me.steven.indrev.recipes.COMPRESSOR_RECIPE_TYPE
import me.steven.indrev.recipes.PULVERIZER_RECIPE_TYPE
import me.steven.indrev.utils.Material
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.Models
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class IndustrialRevolutionDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider { output, _ -> DefaultModelGenerator(output) }
        pack.addProvider { output, _ -> DefaultLangGenerator(output) }
        pack.addProvider { output, future -> DefaultItemTagGenerator(output, future) }
        pack.addProvider { output, future -> DefaultBlockTagGenerator(output, future) }
        pack.addProvider { output, _ -> DefaultCraftingGenerator(output) }
    }

    class DefaultItemTagGenerator(output: FabricDataOutput, registriesFuture: CompletableFuture<WrapperLookup>) : FabricTagProvider<Item>(output, RegistryKeys.ITEM, registriesFuture) {
        override fun configure(arg: WrapperLookup) {
            Material.Type.values().forEach { type ->
                arrayOf(NIKOLITE, ENRICHED_NIKOLITE, COPPER, TIN, LEAD, TUNGSTEN, SILVER, BRONZE, ELECTRUM, STEEL, IRON, GOLD, COAL, DIAMOND).forEach { material ->
                    if (material.types.contains(type)) {
                        val item = material.types[type]
                        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier("c:" + type.tag.format(material.material)))).add(item)
                    }
                }
            }
        }
    }

    class DefaultBlockTagGenerator(output: FabricDataOutput, registriesFuture: CompletableFuture<WrapperLookup>) : FabricTagProvider<Block>(output, RegistryKeys.BLOCK, registriesFuture) {
        override fun configure(arg: WrapperLookup) {
            Material.Type.values().forEach { type ->
                arrayOf(NIKOLITE, ENRICHED_NIKOLITE, COPPER, TIN, LEAD, TUNGSTEN, SILVER, BRONZE, ELECTRUM, STEEL, IRON, GOLD, COAL, DIAMOND).forEach { material ->
                    if (material.types.contains(type)) {
                        val item = material.types[type]
                        if (item is BlockItem) {
                            getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK, Identifier("c:" + type.tag.format(material.material)))).add(item.block)
                        }
                    }
                }
            }
        }
    }

    class DefaultCraftingGenerator(output: FabricDataOutput) : FabricRecipeProvider(output) {
        override fun generate(exporter: Consumer<RecipeJsonProvider>) {
            listOf(TIN, LEAD, TUNGSTEN, SILVER).forEach { materials ->
                offerSmelting(exporter, listOf(materials.types[Material.Type.ORE], materials.types[Material.Type.DEEPSLATE_ORE]), RecipeCategory.MISC, materials.types[Material.Type.INGOT], 0.7f, 200, materials.material + "_ingot")
                offerBlasting(exporter, listOf(materials.types[Material.Type.ORE], materials.types[Material.Type.DEEPSLATE_ORE]), RecipeCategory.MISC, materials.types[Material.Type.INGOT], 0.7f, 100, materials.material + "_ingot")

                if (materials.types.containsKey(Material.Type.RAW_ORE)) {
                    offerSmelting(exporter, listOf(materials.types[Material.Type.RAW_ORE]), RecipeCategory.MISC, materials.types[Material.Type.INGOT], 0.7f, 200, materials.material + "_ingot")
                    offerBlasting(exporter, listOf(materials.types[Material.Type.RAW_ORE]), RecipeCategory.MISC, materials.types[Material.Type.INGOT], 0.7f, 100, materials.material + "_ingot")
                }

                offerCompactingRecipe(exporter, RecipeCategory.MISC, materials.types[Material.Type.BLOCK], materials.types[Material.Type.INGOT])
                offerCompactingRecipe(exporter, RecipeCategory.MISC, materials.types[Material.Type.RAW_ORE], materials.types[Material.Type.RAW_BLOCK_ORE])

                MachineRecipeJsonBuilder.dustIngotConversions(exporter, materials.types[Material.Type.INGOT]!!, materials.types[Material.Type.DUST]!!)

                MachineRecipeJsonBuilder.compress(exporter, materials.types[Material.Type.INGOT]!!, materials.types[Material.Type.PLATE]!!)
                MachineRecipeJsonBuilder.pulverize(exporter, materials.types[Material.Type.PLATE]!!, materials.types[Material.Type.DUST]!!)
            }

            MachineRecipeJsonBuilder.dustIngotConversions(exporter, Items.IRON_INGOT, IRON.types[Material.Type.DUST]!!)
            MachineRecipeJsonBuilder.dustIngotConversions(exporter, Items.COPPER_INGOT, COPPER.types[Material.Type.DUST]!!)
            MachineRecipeJsonBuilder.dustIngotConversions(exporter, Items.GOLD_INGOT, GOLD.types[Material.Type.DUST]!!)
            MachineRecipeJsonBuilder.pulverize(exporter, Items.COAL, COAL.types[Material.Type.DUST]!!)
            MachineRecipeJsonBuilder.pulverize(exporter, Items.DIAMOND, DIAMOND.types[Material.Type.DUST]!!)

            MachineRecipeJsonBuilder.alloy(exporter, NIKOLITE.types[Material.Type.DUST]!!, Items.IRON_INGOT, NIKOLITE.types[Material.Type.INGOT]!!)
            MachineRecipeJsonBuilder.alloy(exporter, NIKOLITE.types[Material.Type.DUST]!!, 1, DIAMOND.types[Material.Type.DUST]!!, 1, ENRICHED_NIKOLITE.types[Material.Type.DUST]!!, 2)
            MachineRecipeJsonBuilder.alloy(exporter, NIKOLITE.types[Material.Type.DUST]!!, NIKOLITE.types[Material.Type.INGOT]!!, ENRICHED_NIKOLITE.types[Material.Type.INGOT]!!)

            MachineRecipeJsonBuilder.alloy(exporter, commonTag("copper_dusts"), 3, commonTag("tin_dusts"), 1, BRONZE.types[Material.Type.DUST]!!, 4)
            MachineRecipeJsonBuilder.alloy(exporter, commonTag("iron_dusts"), 1, commonTag("coal_dusts"), 1, STEEL.types[Material.Type.DUST]!!, 2)
            MachineRecipeJsonBuilder.alloy(exporter, commonTag("gold_dusts"), 1, commonTag("silver_ingots"), 1, ELECTRUM.types[Material.Type.DUST]!!, 2)
        }

        private fun commonTag(tag: String): TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier("c:$tag"))
    }

    class DefaultLangGenerator(output: FabricDataOutput) : FabricLanguageProvider(output) {
        override fun generateTranslations(translationBuilder: TranslationBuilder) {
            ALL_ITEMS.forEach { item ->
                val name = Registries.ITEM.getId(item).path.split("_")
                    .joinToString(" ") { c -> c.substring(0, 1).uppercase() + c.substring(1) }
                translationBuilder.add(item, name)
            }

            arrayOf(PULVERIZER_RECIPE_TYPE, CHEMICAL_INFUSER_RECIPE_TYPE, ALLOY_SMELTER_RECIPE_TYPE, COMPRESSOR_RECIPE_TYPE).forEach { type ->
                val path = Registries.RECIPE_TYPE.getId(type)!!.path
                translationBuilder.add("indrev.rei.title.$path",
                    path.split("_").joinToString(" ") { c -> c.substring(0, 1).uppercase() + c.substring(1) })
            }
        }
    }

    class DefaultModelGenerator(output: FabricDataOutput) : FabricModelProvider(output) {
        override fun generateBlockStateModels(blockStateModelGenerator: BlockStateModelGenerator) {
            arrayOf(NIKOLITE, ENRICHED_NIKOLITE, COPPER, TIN, LEAD, TUNGSTEN, SILVER, BRONZE, ELECTRUM, STEEL).forEach { material ->
                material.types.forEach { (_, item) ->
                    if (item is BlockItem) {
                        blockStateModelGenerator.registerSimpleCubeAll(item.block)
                    }
                }
            }

        }

        override fun generateItemModels(itemModelGenerator: ItemModelGenerator) {
            arrayOf(NIKOLITE, ENRICHED_NIKOLITE, COPPER, TIN, LEAD, TUNGSTEN, SILVER, BRONZE, ELECTRUM, STEEL, IRON, GOLD, COAL, DIAMOND).forEach { material ->
                material.types.forEach { (_, item) ->
                    if (item !is BlockItem)
                        itemModelGenerator.register(item, Models.GENERATED)
                }
            }
        }
    }
}