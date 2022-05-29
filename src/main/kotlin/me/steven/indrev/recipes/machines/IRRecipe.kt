package me.steven.indrev.recipes.machines

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.IRFluidTank
import me.steven.indrev.utils.asMutableList
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.random.Random
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

interface IRRecipe : Recipe<Inventory> {
    val identifier: Identifier
    val input: Array<InputEntry>
    val outputs: Array<OutputEntry>
    val ticks: Int

    override fun getId(): Identifier = identifier

    @Deprecated("Unsupported method for Industrial Revolution's recipes", replaceWith = ReplaceWith("craft(Random)"), DeprecationLevel.ERROR)
    override fun craft(inv: Inventory?): ItemStack = throw IllegalArgumentException("Unsupported method for Industrial Revolution's recipes")
    @Deprecated("Unsupported method for Industrial Revolution's recipes", replaceWith = ReplaceWith("output"), DeprecationLevel.ERROR)
    override fun getOutput(): ItemStack = outputs.firstOrNull()?.stack ?: ItemStack.EMPTY
    @Deprecated("Unsupported method for Industrial Revolution's recipes", replaceWith = ReplaceWith("input"), DeprecationLevel.ERROR)
    override fun getIngredients(): DefaultedList<Ingredient> = DefaultedList.of()
    @Deprecated("Unsupported method for Industrial Revolution's recipes", replaceWith = ReplaceWith("matches(Inventory, FluidVolume?)"), DeprecationLevel.ERROR)
    override fun matches(inv: Inventory?, world: World?): Boolean = throw IllegalArgumentException("Unsupported method for Industrial Revolution's recipes")

    override fun getType(): IRRecipeType<*>

    fun canStart(component: CraftingComponent<*>): Boolean {
        return component.outputSlots!!.isEmpty() || outputs.all { component.fits(it.stack) }
    }

    override fun isEmpty(): Boolean {
        return input.isEmpty() || input.any { entry -> entry.ingredient.matchingStacks.isEmpty() }
    }

    override fun isIgnoredInRecipeBook(): Boolean = true

    fun craft(random: Random?): List<ItemStack> {
        val produced = ArrayList<ItemStack>(outputs.size)
        outputs.forEach { (stack, chance) ->
            if (chance >= 1.0 || random != null && random.nextDouble() > chance) produced.add(stack.copy())
        }
        return produced
    }

    fun matches(inv: List<ItemStack>, fluidVolume: List<IRFluidTank>): Boolean {
        if (inv.isEmpty()) return true
        else if (inv.size == 1 && input.size == 1) return matches(inv.first(), fluidVolume)
        val remainder = input.map { it.copy() }.asMutableList()
        for (stack in inv) {
            val result = remainder.firstOrNull { (ingredient, count) -> ingredient.test(stack) && stack.count >= count } ?: continue
            result.count -= stack.count
            if (result.count <= 0) remainder.remove(result)
        }
        return remainder.isEmpty()
    }

    fun matches(stack: ItemStack, fluidVolume: List<IRFluidTank>): Boolean {
        assert(input.size == 1)
        val (ingredient, count) = input.first()
        if (ingredient.test(stack) && stack.count >= count) return true
        return false
    }

    open class IRRecipeSerializer<T : IRRecipe>(private val factory: (Identifier, Array<InputEntry>, Array<OutputEntry>, Int) -> T) : RecipeSerializer<T> {
        override fun read(id: Identifier, json: JsonObject): T {
            val ingredients = ingredientsFromElement(json["ingredients"])
            val ticks = json.get("processTime").asInt
            val output = itemStacksFromElement(json["output"])
            return factory(id, ingredients, output, ticks)
        }

        override fun read(id: Identifier, buf: PacketByteBuf): T {
            val size = buf.readInt()
            val ingredients = mutableListOf<InputEntry>()
            (0 until size).forEach { _ ->
                val ingredient = Ingredient.fromPacket(buf)
                val count = buf.readInt()
                ingredients.add(InputEntry(ingredient, count))
            }
            val ticks = buf.readInt()
            val outputSize = buf.readInt()
            val output = mutableListOf<OutputEntry>()
            (0 until outputSize).forEach { _ ->
                val stack = buf.readItemStack()
                val chance = buf.readDouble()
                output.add(OutputEntry(stack, chance))
            }
            return factory(id, ingredients.toTypedArray(), output.toTypedArray(), ticks)
        }

        override fun write(buf: PacketByteBuf, recipe: T) {
            buf.writeInt(recipe.input.size)
            recipe.input.forEach { (ingredient, count) ->
                ingredient.write(buf)
                buf.writeInt(count)
            }
            buf.writeInt(recipe.ticks)
            buf.writeInt(recipe.outputs.size)
            recipe.outputs.forEach { (stack, chance) ->
                buf.writeItemStack(stack)
                buf.writeDouble(chance)
            }
        }
    }

    companion object {

        fun ingredientsFromElement(jsonElement: JsonElement?): Array<InputEntry> {
            return when (jsonElement) {
                is JsonArray ->
                    jsonElement.map { ingredientFromJson(it.asJsonObject) }.toTypedArray()
                is JsonObject ->
                    arrayOf(ingredientFromJson(jsonElement))
                null -> return emptyArray()
                else -> throw IllegalArgumentException("expected json object or array at 'ingredients', received $jsonElement")
            }
        }

        fun itemStacksFromElement(jsonElement: JsonElement?): Array<OutputEntry> {
            return when (jsonElement) {
                is JsonArray ->
                    jsonElement.map { itemStackFromJson(it.asJsonObject)!! }.sortedByDescending { it.chance }.toTypedArray()
                is JsonObject ->
                    listOf(itemStackFromJson(jsonElement) ?: return emptyArray()).toTypedArray()
                null -> return emptyArray()
                else -> throw IllegalArgumentException("expected json object or array at 'ingredients', received $jsonElement")
            }
        }

        fun ingredientFromJson(json: JsonObject): InputEntry {
            val ing = Ingredient.fromJson(json)
            val count = JsonHelper.getInt(json, "count", 1)
            return InputEntry(ing, count)
        }

        fun itemStackFromJson(json: JsonObject): OutputEntry? {
            val itemId = json.get("item").asString
            if (itemId == "empty") return null
            val item = Registry.ITEM.get(Identifier(itemId))
            val output = ItemStack { item }
            if (output.isEmpty) println("empty $itemId")
            output.count = JsonHelper.getInt(json, "count", 1)
            val chance = JsonHelper.getFloat(json, "chance", 1f).toDouble()
            return OutputEntry(output, chance)
        }
    }
}