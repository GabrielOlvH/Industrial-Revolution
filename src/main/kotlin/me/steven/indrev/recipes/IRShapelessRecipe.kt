package me.steven.indrev.recipes

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.getFirstMatch
import me.steven.indrev.utils.identifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry

class IRShapelessRecipe(
    id: Identifier,
    group: String,
    output: ItemStack,
    input: DefaultedList<Ingredient>
) : ShapelessRecipe(id, group, output, input) {

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("crafting_shapeless")
        val TYPE = object : RecipeType<IRShapelessRecipe> {}
        val SERIALIZER = Serializer()

        class Serializer : ShapelessRecipe.Serializer() {
            override fun read(
                identifier: Identifier,
                jsonObject: JsonObject
            ): ShapelessRecipe? {
                val string = JsonHelper.getString(jsonObject, "group", "")
                val defaultedList =
                    getIngredients(
                        JsonHelper.getArray(
                            jsonObject,
                            "ingredients"
                        )
                    )
                return when {
                    defaultedList.isEmpty() -> {
                        throw JsonParseException("No ingredients for shapeless recipe")
                    }
                    defaultedList.size > 9 -> {
                        throw JsonParseException("Too many ingredients for shapeless recipe")
                    }
                    else -> {
                        val itemPath = jsonObject.get("result").asJsonObject.get("item").asString
                        val item = getFirstMatch(
                            arrayOf(
                                Identifier(
                                    IndustrialRevolution.CONFIG.compatibility.targetModId,
                                    itemPath
                                ), identifier(itemPath)
                            ), Registry.ITEM
                        )
                        val itemStack = getItemStack(JsonHelper.getObject(jsonObject, "result"), item)
                        ShapelessRecipe(identifier, string, itemStack, defaultedList)
                    }
                }
            }
        }

        private fun getIngredients(json: JsonArray): DefaultedList<Ingredient> {
            val defaultedList = DefaultedList.of<Ingredient>()
            for (i in 0 until json.size()) {
                val ingredient = Ingredient.fromJson(json[i])
                if (!ingredient.isEmpty) {
                    defaultedList.add(ingredient)
                }
            }
            return defaultedList
        }

        fun getItemStack(json: JsonObject, item: Item): ItemStack {
            return if (json.has("data")) {
                throw JsonParseException("Disallowed data tag found")
            } else {
                val i = JsonHelper.getInt(json, "count", 1)
                ItemStack(item, i)
            }
        }
    }
}