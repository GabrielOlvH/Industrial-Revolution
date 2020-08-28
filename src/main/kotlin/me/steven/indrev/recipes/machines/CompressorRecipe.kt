package me.steven.indrev.recipes.machines

import com.google.gson.JsonObject
import me.steven.indrev.utils.getItemStackFromJson
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class CompressorRecipe(private val id: Identifier, val processTime: Int, private val output: ItemStack, val input: Ingredient) : Recipe<Inventory> {
    override fun craft(inv: Inventory?): ItemStack = output.copy()

    override fun getId(): Identifier = id

    override fun getType(): RecipeType<*> =
        TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> =
        SERIALIZER

    override fun getOutput(): ItemStack = output

    override fun getPreviewInputs(): DefaultedList<Ingredient> = DefaultedList.of<Ingredient>().also { it.add(input) }

    override fun matches(inv: Inventory?, world: World?): Boolean = input.test(inv?.getStack(0))

    companion object {
        val TYPE = object : RecipeType<CompressorRecipe> {}
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("compress")

        class Serializer : RecipeSerializer<CompressorRecipe> {
            override fun write(buf: PacketByteBuf, recipe: CompressorRecipe) {
                recipe.input.write(buf)
                buf.writeInt(recipe.processTime)
                buf.writeItemStack(recipe.output)
            }

            override fun read(id: Identifier, json: JsonObject): CompressorRecipe {
                val input = Ingredient.fromJson(json.getAsJsonObject("ingredient"))
                val output = getItemStackFromJson(json.getAsJsonObject("output"))
                val ticks = json.get("processTime").asInt
                return CompressorRecipe(id, ticks, output, input)
            }

            override fun read(id: Identifier, buf: PacketByteBuf): CompressorRecipe {
                val input = Ingredient.fromPacket(buf)
                val processTime = buf.readInt()
                val output = buf.readItemStack()
                return CompressorRecipe(
                    id,
                    processTime,
                    output,
                    input
                )
            }

        }
    }
}