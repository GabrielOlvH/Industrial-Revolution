package me.steven.indrev.recipes.compatibility

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.recipes.ExperienceRewardRecipe
import me.steven.indrev.utils.getFirstMatch
import me.steven.indrev.utils.identifier
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.registry.Registry

class IRSmeltingRecipe(
    id: Identifier,
    group: String,
    input: Ingredient,
    output: ItemStack,
    override val amount: Float,
    cookTime: Int
) : SmeltingRecipe(id, group, input, output, amount, cookTime),
    ExperienceRewardRecipe {

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("smelting")
        val TYPE = object : RecipeType<IRSmeltingRecipe> {}
        val SERIALIZER =
            Serializer()

        class Serializer : RecipeSerializer<IRSmeltingRecipe> {
            override fun read(identifier: Identifier, jsonObject: JsonObject): IRSmeltingRecipe {
                val string = JsonHelper.getString(jsonObject, "group", "")
                val jsonElement = if (JsonHelper.hasArray(jsonObject, "ingredient")) JsonHelper.getArray(
                    jsonObject,
                    "ingredient"
                ) else JsonHelper.getObject(jsonObject, "ingredient")
                val ingredient = Ingredient.fromJson(jsonElement as JsonElement)
                val itemPath = jsonObject.get("result").asString
                val item =
                    if (itemPath.contains(":")) Registry.ITEM.get(Identifier(itemPath))
                    else
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

            override fun read(identifier: Identifier, packetByteBuf: PacketByteBuf): IRSmeltingRecipe {
                val string = packetByteBuf.readString(32767)
                val ingredient = Ingredient.fromPacket(packetByteBuf)
                val itemStack = packetByteBuf.readItemStack()
                val f = packetByteBuf.readFloat()
                val i = packetByteBuf.readVarInt()
                return IRSmeltingRecipe(identifier, string, ingredient, itemStack, f, i)
            }

            override fun write(packetByteBuf: PacketByteBuf, abstractCookingRecipe: IRSmeltingRecipe) {
                packetByteBuf.writeString(abstractCookingRecipe.group)
                abstractCookingRecipe.input.write(packetByteBuf)
                packetByteBuf.writeItemStack(abstractCookingRecipe.output)
                packetByteBuf.writeFloat(abstractCookingRecipe.experience)
                packetByteBuf.writeVarInt(abstractCookingRecipe.cookTime)
            }

        }
    }
}