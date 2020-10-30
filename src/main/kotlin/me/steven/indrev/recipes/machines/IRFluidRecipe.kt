package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonObject
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.getFluidFromJson
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

abstract class IRFluidRecipe : IRRecipe {
    abstract val fluidInput: FluidVolume?
    abstract val fluidOutput: FluidVolume?

    override fun matches(inv: Array<ItemStack>, fluidVolume: FluidVolume?): Boolean {
        return when {
            fluidVolume == null -> false
            fluidInput != null -> fluidVolume.fluidKey == fluidInput!!.fluidKey && fluidVolume.amount() >= fluidInput!!.amount() && super.matches(inv, fluidVolume)
            else -> super.matches(inv, fluidVolume)
        }
    }

    open class IRFluidRecipeSerializer<T : IRFluidRecipe>(private val factory: (Identifier, Array<InputEntry>, Array<OutputEntry>, FluidVolume?, FluidVolume?, Int) -> T) : RecipeSerializer<T> {
        override fun read(id: Identifier, buf: PacketByteBuf): T {
            val ticks = buf.readInt()
            val hasInputFluid = buf.readBoolean()
            val fluidInput = if (hasInputFluid) {
                val fluidId = buf.readIdentifier()
                val fluidAmount = FluidAmount.fromMcBuffer(buf)
                val fluidKey = FluidKeys.get(Registry.FLUID.get(fluidId))
                fluidKey.withAmount(fluidAmount)
            } else null
            val hasOutputFluid = buf.readBoolean()
            val fluidOutput = if (hasOutputFluid) {
                val fluidId = buf.readIdentifier()
                val fluidAmount = FluidAmount.fromMcBuffer(buf)
                val fluidKey = FluidKeys.get(Registry.FLUID.get(fluidId))
                fluidKey.withAmount(fluidAmount)
            } else null
            val size = buf.readInt()
            val ingredients = mutableListOf<InputEntry>()
            (0 until size).forEach { _ ->
                val ingredient = Ingredient.fromPacket(buf)
                val count = buf.readInt()
                ingredients.add(InputEntry(ingredient, count))
            }
            val outputSize = buf.readInt()
            val output = mutableListOf<OutputEntry>()
            (0 until outputSize).forEach { _ ->
                val stack = buf.readItemStack()
                val chance = buf.readDouble()
                output.add(OutputEntry(stack, chance))
            }
            return factory(id, ingredients.toTypedArray(), output.toTypedArray(), fluidInput, fluidOutput, ticks)
        }

        override fun read(id: Identifier, json: JsonObject): T {
            val ingredients = IRRecipe.ingredientsFromElement(json["ingredients"])
            val ticks = json.get("processTime").asInt
            val output = IRRecipe.itemStacksFromElement(json["output"])
            val fluidInputJson = json.getAsJsonObject("fluidInput")
            val fluidInput = if (fluidInputJson == null) null else getFluidFromJson(fluidInputJson)

            val fluidOutputJson = json.getAsJsonObject("fluidOutput")
            val fluidOutput = if (fluidOutputJson == null) null else getFluidFromJson(fluidOutputJson)

            return factory(id, ingredients, output, fluidInput, fluidOutput, ticks)
        }

        override fun write(buf: PacketByteBuf, recipe: T) {
            buf.writeInt(recipe.ticks)
            buf.writeBoolean(recipe.fluidInput != null)
            if (recipe.fluidInput != null) {
                buf.writeIdentifier(recipe.fluidInput!!.fluidKey.entry.id)
                recipe.fluidInput!!.amount().toMcBuffer(buf)
            }
            buf.writeBoolean(recipe.fluidOutput != null)
            if (recipe.fluidOutput != null) {
                buf.writeIdentifier(recipe.fluidOutput!!.fluidKey.entry.id)
                recipe.fluidOutput!!.amount().toMcBuffer(buf)
            }
            buf.writeInt(recipe.input.size)
            recipe.input.forEach { (ingredient, count) ->
                ingredient.write(buf)
                buf.writeInt(count)
            }
            buf.writeInt(recipe.outputs.size)
            recipe.outputs.forEach { (stack, chance) ->
                buf.writeItemStack(stack)
                buf.writeDouble(chance)
            }
        }
    }
}