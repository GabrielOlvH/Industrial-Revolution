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
import net.minecraft.util.Pair
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class PulverizerRecipe(private val id: Identifier, val processTime: Int, private val output: ItemStack, val extraOutput: Pair<ItemStack, Double>?, val input: Ingredient) : Recipe<Inventory> {
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
        val TYPE = object : RecipeType<PulverizerRecipe> {}
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("pulverize")

        class Serializer : RecipeSerializer<PulverizerRecipe> {
            override fun write(buf: PacketByteBuf, recipe: PulverizerRecipe) {
                recipe.input.write(buf)
                buf.writeInt(recipe.processTime)
                buf.writeItemStack(recipe.output)
                if (recipe.extraOutput != null) {
                    buf.writeBoolean(true)
                    buf.writeItemStack(recipe.extraOutput.left)
                    buf.writeDouble(recipe.extraOutput.right)
                } else buf.writeBoolean(false)
            }

            override fun read(id: Identifier, json: JsonObject): PulverizerRecipe {
                val input = Ingredient.fromJson(json.getAsJsonObject("ingredient"))
                val output = getItemStackFromJson(json.getAsJsonObject("output"))
                val ticks = json.get("processTime").asInt
                return if (json.has("extra")) {
                    val extra = json.getAsJsonObject("extra")
                    val extraOutputStack = getItemStackFromJson(extra)
                    val chance = extra.get("chance").asDouble
                    PulverizerRecipe(
                        id,
                        ticks,
                        output,
                        Pair(extraOutputStack, chance),
                        input
                    )
                } else PulverizerRecipe(id, ticks, output, null, input)
            }

            override fun read(id: Identifier, buf: PacketByteBuf): PulverizerRecipe {
                val input = Ingredient.fromPacket(buf)
                val processTime = buf.readInt()
                val output = buf.readItemStack()
                if (buf.readBoolean()) {
                    val extraOutputStack = buf.readItemStack()
                    val chance = buf.readDouble()
                    return PulverizerRecipe(id, processTime, output, Pair(extraOutputStack, chance), input)
                }
                return PulverizerRecipe(id, processTime, output, null, input)
            }
        }
    }
}