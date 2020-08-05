package me.steven.indrev.recipes.compatibility

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.recipes.ExperienceRewardRecipe
import me.steven.indrev.utils.getFirstMatch
import me.steven.indrev.utils.identifier
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.BlastingRecipe
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.registry.Registry

class IRBlastingRecipe(
    id: Identifier,
    group: String,
    input: Ingredient,
    output: ItemStack,
    override val amount: Float,
    cookTime: Int
) : BlastingRecipe(id, group, input, output, amount, cookTime), ExperienceRewardRecipe {

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("blasting")
        val TYPE = object : RecipeType<IRSmeltingRecipe> {}
        val SERIALIZER =
            Serializer()

        class Serializer : RecipeSerializer<IRBlastingRecipe> {
            override fun read(identifier: Identifier, jsonObject: JsonObject): IRBlastingRecipe {
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
                val experience = JsonHelper.getFloat(jsonObject, "experience", 0.0f)
                val cookingTime = JsonHelper.getInt(jsonObject, "cookingtime", 200)
                return IRBlastingRecipe(
                    identifier,
                    string,
                    ingredient,
                    itemStack,
                    experience,
                    cookingTime
                )
            }

            override fun read(identifier: Identifier, packetByteBuf: PacketByteBuf): IRBlastingRecipe {
                val string = packetByteBuf.readString(32767)
                val ingredient = Ingredient.fromPacket(packetByteBuf)
                val itemStack = packetByteBuf.readItemStack()
                val experience = packetByteBuf.readFloat()
                val cookingTime = packetByteBuf.readVarInt()
                return IRBlastingRecipe(
                    identifier,
                    string,
                    ingredient,
                    itemStack,
                    experience,
                    cookingTime
                )
            }

            override fun write(packetByteBuf: PacketByteBuf, abstractCookingRecipe: IRBlastingRecipe) {
                packetByteBuf.writeString(abstractCookingRecipe.group)
                abstractCookingRecipe.input.write(packetByteBuf)
                packetByteBuf.writeItemStack(abstractCookingRecipe.output)
                packetByteBuf.writeFloat(abstractCookingRecipe.experience)
                packetByteBuf.writeVarInt(abstractCookingRecipe.cookTime)
            }

        }
    }
}