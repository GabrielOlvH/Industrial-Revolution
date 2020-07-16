package me.steven.indrev.recipes

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.getFirstMatch
import me.steven.indrev.utils.identifier
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.registry.Registry

class IRSmeltingRecipe(
    id: Identifier,
    group: String,
    input: Ingredient,
    output: ItemStack,
    experience: Float,
    cookTime: Int
) : SmeltingRecipe(id, group, input, output, experience, cookTime) {

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("smelting")
        val TYPE = object : RecipeType<IRSmeltingRecipe> {}
        val SERIALIZER = Serializer()

        class Serializer : CookingRecipeSerializer<IRSmeltingRecipe>(::IRSmeltingRecipe, 200) {
            override fun read(identifier: Identifier, jsonObject: JsonObject): IRSmeltingRecipe {
                val string = JsonHelper.getString(jsonObject, "group", "")
                val jsonElement = if (JsonHelper.hasArray(jsonObject, "ingredient")) JsonHelper.getArray(
                    jsonObject,
                    "ingredient"
                ) else JsonHelper.getObject(jsonObject, "ingredient")
                val ingredient = Ingredient.fromJson(jsonElement as JsonElement)
                val itemPath = jsonObject.get("result").asString
                val item =
                    getFirstMatch(
                        arrayOf(
                            Identifier(IndustrialRevolution.CONFIG.compatibility.targetModId, itemPath),
                            identifier(itemPath)
                        ), Registry.ITEM
                    )
                val itemStack = ItemStack { item }
                val f = JsonHelper.getFloat(jsonObject, "experience", 0.0f)
                val i = JsonHelper.getInt(jsonObject, "cookingtime", 200)
                return IRSmeltingRecipe(identifier, string, ingredient, itemStack, f, i)
            }
        }
    }
}