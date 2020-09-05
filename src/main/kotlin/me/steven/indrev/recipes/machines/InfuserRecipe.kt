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

class InfuserRecipe(private val id: Identifier, val processTime: Int, private val output: ItemStack, val ingredients: DefaultedList<Ingredient>) : Recipe<Inventory> {
    override fun craft(inv: Inventory?): ItemStack = output.copy()

    override fun getId(): Identifier = id

    override fun getType(): RecipeType<*> =
        TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> =
        SERIALIZER

    override fun getOutput(): ItemStack = output

    override fun getPreviewInputs(): DefaultedList<Ingredient> = ingredients

    override fun matches(inv: Inventory, world: World?): Boolean {
        val matched = Array(ingredients.size) { false }
        for (slot in 0 until inv.size()) {
            for ((index, ingredient) in ingredients.withIndex()) {
                if (!matched[index] && ingredient.test(inv.getStack(slot))) {
                    matched[index] = true
                    break
                }
            }
        }
        return matched.all { it }
    }

    companion object {
        val TYPE = object : RecipeType<InfuserRecipe> {}
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("infuse")

        class Serializer : RecipeSerializer<InfuserRecipe> {
            override fun write(buf: PacketByteBuf, recipe: InfuserRecipe) {
                buf.writeInt(recipe.ingredients.size)
                recipe.ingredients.forEach { it.write(buf) }
                buf.writeInt(recipe.processTime)
                buf.writeItemStack(recipe.output)
            }

            override fun read(id: Identifier, json: JsonObject): InfuserRecipe {
                val inputArray = json.getAsJsonArray("ingredients")
                val ingredients = inputArray.map { Ingredient.fromJson(it.asJsonObject) }.let { list ->
                    val ing = DefaultedList.ofSize(inputArray.size(), Ingredient.EMPTY)
                    list.forEachIndexed { index, ingredient -> ing[index] = ingredient }
                    ing
                }
                val output = getItemStackFromJson(json.getAsJsonObject("output"))
                val ticks = json.get("processTime").asInt
                return InfuserRecipe(id, ticks, output, ingredients)
            }

            override fun read(id: Identifier, buf: PacketByteBuf): InfuserRecipe {
                val size = buf.readInt()
                val input = DefaultedList.ofSize(size, Ingredient.EMPTY)
                (0 until size).forEach { i -> input[i] = Ingredient.fromPacket(buf) }
                val processTime = buf.readInt()
                val output = buf.readItemStack()
                return InfuserRecipe(id, processTime, output, input)
            }

        }
    }
}