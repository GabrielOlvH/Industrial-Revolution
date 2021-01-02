package me.steven.indrev.datagen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.datagen.generators.*
import me.steven.indrev.datagen.utils.MetalModel
import me.steven.indrev.datagen.utils.MetalSpriteRegistry
import me.steven.indrev.registry.IRRegistry
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.io.File
import kotlin.system.exitProcess

class DataGeneratorManager(namespace: String) {

    val root = File("generated")

    val lootTableGenerator = LootTableGenerator(root, namespace, LootTableGenerator.SELF_DROP)
    val itemModelGenerator = ItemModelGenerator(root, namespace, ItemModelGenerator.DEFAULT_ITEM)
    val blockModelGenerator = BlockModelGenerator(root, namespace) { JsonFactory.nullFactory() }
    val materialRecipeGenerator = MaterialRecipeGenerator(root, namespace) { JsonFactory.nullFactory() }
    val materialTagGenerator = MaterialTagGenerator(root, namespace) { JsonFactory.nullFactory() }
    val metalSpriteGenerator = MetalSpriteGenerator(root, namespace) { ImageFactory.nullFactory() }

    init {
        root.mkdir()

        arrayOf("copper", "tin", "lead", "tungsten", "silver").forEach { material ->
            materialRecipeGenerator.register(
                "${material}_ore",
                pulverizeOre("c:${material}_ores", "indrev:${material}_dust")
            )
            materialRecipeGenerator.register(
                "${material}_ingot",
                pulverizeIngot("c:${material}_ingots", "indrev:${material}_dust")
            )
            arrayOf("ore", "plate", "dust", "ingot").forEach { suffix ->
                materialTagGenerator.register("${material}_$suffix", createTag("indrev:${material}_$suffix"))
            }
        }

        arrayOf("iron", "gold").forEach { material ->
            materialRecipeGenerator.register(
                "${material}_ore",
                pulverizeOre("c:${material}_ores", "indrev:${material}_dust")
            )
            materialRecipeGenerator.register(
                "${material}_ingot",
                pulverizeIngot("c:${material}_ingots", "indrev:${material}_dust")
            )
            materialTagGenerator.register("${material}_ore", createTag("minecraft:${material}_ore"))
        }
        arrayOf("diamond", "coal").forEach { material ->
            materialRecipeGenerator.register(
                "${material}_ore",
                pulverizeOre("c:${material}_ores", "minecraft:${material}")
            )
            materialRecipeGenerator.register(
                material,
                pulverizeIngot("minecraft:$material", "indrev:${material}_dust", fileSuffix = "dust")
            )
            materialTagGenerator.register("${material}_dust", createTag("indrev:${material}_dust"))
            materialTagGenerator.register("${material}_ore", createTag("minecraft:${material}_ore"))
        }

        itemModelGenerator.register(IRRegistry.GAMER_AXE_ITEM, JsonFactory.nullFactory())
        itemModelGenerator.register(IRRegistry.DRILL_BOTTOM.asItem(), JsonFactory.nullFactory())

        MetalSpriteRegistry.MATERIAL_PROVIDERS.forEach { (id, model) ->
            val itemId = Identifier(id.namespace, id.path)
            val item = Registry.ITEM.get(itemId)
            if (item != Items.AIR) {
                if (item is BlockItem) {
                    blockModelGenerator.register(item.block, BlockModelGenerator.CUBE_ALL(item.block))
                }
                metalSpriteGenerator.register(id, ImageFactory.simpleFactory<Identifier>()(id))
                val factory =
                    if (model.type == MetalModel.TransformationType.HANDHELD) ItemModelGenerator.HANDHELD
                    else ItemModelGenerator.DEFAULT_ITEM
                itemModelGenerator.register(item, factory(item))
            }
        }
    }
    
    fun generate() {
        val lootTablesGenerated = lootTableGenerator.generate()
        IndustrialRevolution.LOGGER.info("Generated $lootTablesGenerated loot tables.")
        val itemModelsGenerated = itemModelGenerator.generate()
        IndustrialRevolution.LOGGER.info("Generated $itemModelsGenerated item models.")
        val blockModelsGenerated = blockModelGenerator.generate()
        IndustrialRevolution.LOGGER.info("Generated $blockModelsGenerated block models.")
        val recipesGenerated = materialRecipeGenerator.generate()
        IndustrialRevolution.LOGGER.info("Generated $recipesGenerated recipes.")
        val tagsGenerated = materialTagGenerator.generate()
        IndustrialRevolution.LOGGER.info("Generated $tagsGenerated tags.")
        val spritesGenerated = metalSpriteGenerator.generate()
        IndustrialRevolution.LOGGER.info("Generated $spritesGenerated sprites.")

        IndustrialRevolution.LOGGER.info("Generated ${lootTablesGenerated + itemModelsGenerated + blockModelsGenerated + recipesGenerated + tagsGenerated + spritesGenerated } files in total.")
        exitProcess(0)
    }

    operator fun Int.plus(boolean: Boolean): Int {
        return if (boolean) this + 1 else this
    }

    private fun pulverizeTagged(inputId: String, outputId: String, count: Int, time: Int, fileSuffix: String) : JsonFactory<String> {
        return object : JsonFactory<String> {
            override fun generate(): JsonObject {
                val json = JsonObject()
                json.addProperty("type", "indrev:pulverize")
                val ingredients = JsonObject()
                ingredients.addProperty("tag", inputId)
                json.add("ingredients", ingredients)
                val output = JsonObject()
                output.addProperty("item", outputId)
                output.addProperty("count", count)
                json.add("output", output)
                json.addProperty("processTime", time)
                return json
            }

            override fun getFileName(t: String, id: Identifier): String {
                var fileName = super.getFileName(t, id)
                val index = fileName.indexOf("_")
                if (index != -1) fileName = fileName.substring(0, index)
                return "pulverizer/${fileName}_$fileSuffix"
            }
        }
    }

    private fun pulverizeItem(inputId: String, outputId: String, count: Int, time: Int, fileSuffix: String) : JsonFactory<String> {
        return object : JsonFactory<String> {
            override fun generate(): JsonObject {
                val json = JsonObject()
                json.addProperty("type", "indrev:pulverize")
                val ingredients = JsonObject()
                ingredients.addProperty("item", inputId)
                json.add("ingredients", ingredients)
                val output = JsonObject()
                output.addProperty("item", outputId)
                output.addProperty("count", count)
                json.add("output", output)
                json.addProperty("processTime", time)
                return json
            }

            override fun getFileName(t: String, id: Identifier): String = "pulverizer/${super.getFileName(t, id)}_$fileSuffix"
        }
    }

    private fun pulverizeOre(inputId: String, outputId: String, count: Int = 2, time: Int = 200, fileSuffix: String = "dust_from_ore") : JsonFactory<String> {
        return pulverizeTagged(inputId, outputId, count, time, fileSuffix)
    }

    private fun pulverizeIngot(inputId: String, outputId: String, count: Int = 1, time: Int = 150, fileSuffix: String = "dust_from_ingot") : JsonFactory<String> {
        return pulverizeTagged(inputId, outputId, count, time, fileSuffix)
    }

    private fun createTag(item: String): JsonFactory<String> {
        return object : JsonFactory<String> {
            override fun generate(): JsonObject {
                val obj = JsonObject()
                obj.addProperty("replace", false)
                val values = JsonArray()
                values.add(item)
                obj.add("values", values)
                return obj
            }

            override fun getFileName(t: String, id: Identifier): String = "${super.getFileName(t, id)}s"
        }
    }
}