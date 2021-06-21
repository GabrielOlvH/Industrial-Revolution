package me.steven.indrev.datagen

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.datagen.generators.*
import me.steven.indrev.datagen.utils.MetalModel
import me.steven.indrev.datagen.utils.MetalSpriteRegistry
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.util.DyeColor
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

        arrayOf("tin", "lead", "tungsten", "silver").forEach { material ->
            materialRecipeGenerator.register("raw_${material}", rawOreIntoBlock(material))
            materialRecipeGenerator.register("raw_${material}_block", rawOreBlockIntoRawItem(material))
        }

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

        DyeColor.values().forEach {
            val name = it.getName()
            materialRecipeGenerator.register("harden_${name}_concrete_powder", hardenConcretePowder(name))
        }

        itemModelGenerator.register(IRItemRegistry.GAMER_AXE_ITEM, JsonFactory.nullFactory())
        itemModelGenerator.register(IRBlockRegistry.DRILL_BOTTOM.asItem(), JsonFactory.nullFactory())

        Registry.BLOCK.forEach { block ->
            val id = Registry.BLOCK.getId(block)
            if (id.namespace == namespace && id.path.contains("ore") && !id.path.contains("purified"))
                lootTableGenerator.register(block, LootTableGenerator.ORE_DROP(block))
        }

        MetalSpriteRegistry.MATERIAL_PROVIDERS.forEach { (id, model) ->
            val itemId = Identifier(id.namespace, id.path)
            val item = Registry.ITEM.get(itemId)
            if (item != Items.AIR) {
                if (item is BlockItem) {
                    blockModelGenerator.register(item.block, BlockModelGenerator.CUBE_ALL(item.block))
                }
                if (id.toString().contains("sword"))
                    metalSpriteGenerator.register(id, ImageFactory.simpleFactory0<Identifier>()(id))
                else
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

    private fun rawOreIntoBlock(ore: String): JsonFactory<String> {
        return object : JsonFactory<String> {
            override fun generate(): JsonObject {
                val json = JsonObject()
                json.addProperty("type", "crafting_shaped")
                val pattern = JsonArray()
                repeat(3) { pattern.add("###") }
                json.add("pattern", pattern)
                val key = JsonObject()
                key.add("#", JsonObject().also { it.addProperty("item", "indrev:raw_${ore}") })
                json.add("key", key)
                val result = JsonObject()
                result.addProperty("item", "indrev:raw_${ore}_block")
                json.add("result", result)
                return json
            }

            override fun getFileName(t: String, id: Identifier): String = "shaped/raw_${ore}_block"
        }
    }

    private fun rawOreBlockIntoRawItem(ore: String): JsonFactory<String> {
        return object : JsonFactory<String> {
            override fun generate(): JsonObject {
                val json = JsonObject()
                json.addProperty("type", "crafting_shapeless")
                val ingredients = JsonObject()
                ingredients.addProperty("item", "indrev:raw_${ore}_block")
                json.add("ingredients", ingredients)
                val output = JsonObject()
                output.addProperty("item", "indrev:raw_${ore}")
                output.addProperty("count", 9)
                json.add("result", output)
                return json
            }

            override fun getFileName(t: String, id: Identifier): String = "shapeless/raw_${ore}"
        }
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

    private fun hardenConcretePowder(color: String): JsonFactory<String> {
        return object : JsonFactory<String> {
            override fun generate(): JsonObject {
                val json = JsonObject()
                json.addProperty("type", "indrev:fluid_infuse")
                val ingredients = JsonObject()
                ingredients.addProperty("item", "minecraft:${color}_concrete_powder")
                json.add("ingredients", ingredients)

                val fluidInput = JsonObject()
                fluidInput.addProperty("fluid", "minecraft:water")
                fluidInput.addProperty("type", "mb")
                fluidInput.addProperty("count", 100)
                json.add("fluidInput", fluidInput)


                val output = JsonObject()
                output.addProperty("item", "minecraft:${color}_concrete")
                output.addProperty("count", 1)
                json.add("output", output)

                json.addProperty("processTime", 200)
                return json
            }

            override fun getFileName(t: String, id: Identifier): String = "fluid_infusing/harden_${color}_concrete_powder"
        }
    }
}