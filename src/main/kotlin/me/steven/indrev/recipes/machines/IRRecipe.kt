package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.getFirstMatch
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.*

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
    override fun getPreviewInputs(): DefaultedList<Ingredient> = throw IllegalArgumentException("Unsupported method for Industrial Revolution's recipes")
    @Deprecated("Unsupported method for Industrial Revolution's recipes", replaceWith = ReplaceWith("matches(Inventory, FluidVolume?)"), DeprecationLevel.ERROR)
    override fun matches(inv: Inventory?, world: World?): Boolean = throw IllegalArgumentException("Unsupported method for Industrial Revolution's recipes")

    fun craft(random: Random?): Array<ItemStack> {
        val produced = mutableListOf<ItemStack>()
        outputs.forEach { (stack, chance) ->
            if (chance >= 1.0 || random != null && random.nextDouble() > chance) produced.add(stack.copy())
        }
        return produced.toTypedArray()
    }

    fun matches(inv: Array<ItemStack>, fluidVolume: FluidVolume?): Boolean {
        if (inv.isEmpty()) return true
        val remainder = input.map { it.copy() }.toMutableList()
        for (stack in inv) {
            val result = remainder.firstOrNull { (ingredient, count) -> ingredient.test(stack) && stack.count >= count } ?: continue
            result.count -= stack.count
            if (result.count <= 0) remainder.remove(result)
        }
        return remainder.isEmpty()
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
            val itemPath = json.get("item").asString
            if (itemPath == "empty") return null
            val item =
                if (itemPath.contains(":")) Registry.ITEM.get(Identifier(itemPath))
                else
                    getFirstMatch(
                        arrayOf(
                            Identifier(IndustrialRevolution.CONFIG.compatibility.targetModId, itemPath),
                            identifier(itemPath)
                        ), Registry.ITEM
                    )
            val output = ItemStack { item }
            output.count = JsonHelper.getInt(json, "count", 1)
            val chance = JsonHelper.getFloat(json, "chance", 1f).toDouble()
            return OutputEntry(output, chance)
        }
    }
}