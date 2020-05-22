package me.steven.indrev.recipes

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.Pair
import net.minecraft.util.registry.Registry

class PulverizerRecipeSerializer : RecipeSerializer<PulverizerRecipe> {
    override fun write(buf: PacketByteBuf, recipe: PulverizerRecipe) {
        recipe.input.write(buf)
        buf.writeInt(recipe.processTime)
        buf.writeItemStack(recipe.output)
        buf.writeItemStack(recipe.extraOutput.left)
        buf.writeDouble(recipe.extraOutput.right)
    }

    override fun read(id: Identifier, json: JsonObject): PulverizerRecipe {
        val input = Ingredient.fromJson(json.getAsJsonObject("ingredient"))
        val result = json.get("output").asJsonObject
        val output = ItemStack {
            Registry.ITEM.getOrEmpty(Identifier(result.get("item").asString)).orElse(null)
                    ?: throw IllegalArgumentException("no such item $result")
        }
        output.count = result.get("count").asInt
        val ticks = json.get("processTime").asInt
        val extraOutputStack =ItemStack {
            Registry.ITEM.getOrEmpty(Identifier(json.getAsJsonObject("extra").get("item").asString)).orElse(null)
                    ?: throw IllegalArgumentException("no such item $result")
        }
        val chance = json.getAsJsonObject("extra").get("chance").asDouble
        return PulverizerRecipe(id, ticks, output, Pair(extraOutputStack, chance), input)
    }

    override fun read(id: Identifier, buf: PacketByteBuf): PulverizerRecipe {
        val input = Ingredient.fromPacket(buf)
        val processTime = buf.readInt()
        val output = buf.readItemStack()
        val extraOutputStack = buf.readItemStack()
        val chance = buf.readDouble()
        return PulverizerRecipe(id, processTime, output, Pair(extraOutputStack, chance), input)
    }
}