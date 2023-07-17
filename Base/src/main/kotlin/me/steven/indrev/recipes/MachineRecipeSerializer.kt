package me.steven.indrev.recipes

import com.google.gson.JsonObject
import me.steven.indrev.utils.bucket
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class MachineRecipeSerializer : RecipeSerializer<MachineRecipe> {

    override fun read(id: Identifier, json: JsonObject): MachineRecipe {
        val typeId = json.get("type").asString
        val type = Registries.RECIPE_TYPE.get(Identifier(typeId))!!
        val itemInput = JsonHelper.getArray(json, "input", null)?.map { elem ->
            val obj = elem.asJsonObject
            val ingredient = Ingredient.fromJson(obj)
            val count = JsonHelper.getInt(obj, "count", 1)
            val chance = JsonHelper.getDouble(obj, "chance", 1.0)
            MachineRecipe.RecipeItemInput(ingredient, count, chance)
        }?.toTypedArray() ?: emptyArray()

        val itemOutput = JsonHelper.getArray(json, "output", null)?.map { elem ->
            val obj = elem.asJsonObject
            val itemId = obj.get("item").asString
            val item = Registries.ITEM.get(Identifier(itemId))
            val count = JsonHelper.getInt(obj, "count", 1)
            val chance = JsonHelper.getDouble(obj, "chance", 1.0)
            MachineRecipe.RecipeItemOutput(item, count, chance)
        }?.toTypedArray() ?: emptyArray()

        val fluidInput = JsonHelper.getArray(json, "fluidInput", null)?.map { elem ->
            val obj = elem.asJsonObject
            val fluidId = obj.get("fluid").asString
            val fluid = Registries.FLUID.get(Identifier(fluidId))
            val amount = JsonHelper.getLong(obj, "amount", bucket)
            val chance = JsonHelper.getDouble(obj, "chance", 1.0)
            MachineRecipe.RecipeFluidInput(fluid, amount, chance)
        }?.toTypedArray() ?: emptyArray()

        val fluidOutput = JsonHelper.getArray(json, "fluidOutput", null)?.map { elem ->
            val obj = elem.asJsonObject
            val fluidId = obj.get("fluid").asString
            val fluid = Registries.FLUID.get(Identifier(fluidId))
            val amount = JsonHelper.getLong(obj, "amount", bucket)
            val chance = JsonHelper.getDouble(obj, "chance", 1.0)
            MachineRecipe.RecipeFluidOutput(fluid, amount, chance)
        }?.toTypedArray() ?: emptyArray()

        val ticks = JsonHelper.getInt(json, "ticks", 1)
        val cost = JsonHelper.getLong(json, "cost", 1)

        return MachineRecipe(id, type, itemInput, itemOutput, fluidInput, fluidOutput, ticks, cost)
    }

    override fun write(buf: PacketByteBuf, recipe: MachineRecipe) {
        buf.writeIdentifier(Registries.RECIPE_TYPE.getId(recipe.type))

        buf.writeInt(recipe.itemInput.size)
        recipe.itemInput.forEach { input ->
            input.ingredient.write(buf)
            buf.writeInt(input.count)
            buf.writeDouble(input.chance)
        }

        buf.writeInt(recipe.itemOutput.size)
        recipe.itemOutput.forEach { output ->
            buf.writeIdentifier(Registries.ITEM.getId(output.item))
            buf.writeInt(output.count)
            buf.writeDouble(output.chance)
        }

        buf.writeInt(recipe.fluidInput.size)
        recipe.fluidInput.forEach { input ->
            buf.writeIdentifier(Registries.FLUID.getId(input.fluid))
            buf.writeLong(input.amount)
            buf.writeDouble(input.chance)
        }

        buf.writeInt(recipe.fluidOutput.size)
        recipe.fluidOutput.forEach { output ->
            buf.writeIdentifier(Registries.FLUID.getId(output.fluid))
            buf.writeLong(output.amount)
            buf.writeDouble(output.chance)
        }

        buf.writeInt(recipe.ticks)
        buf.writeLong(recipe.cost)
    }


    override fun read(id: Identifier, buf: PacketByteBuf): MachineRecipe {
        val type = Registries.RECIPE_TYPE.getOrEmpty(buf.readIdentifier()).orElseThrow()

        val inputItems = (0 until buf.readInt()).map {
            val ing = Ingredient.fromPacket(buf)
            val count = buf.readInt()
            val chance = buf.readDouble()
            MachineRecipe.RecipeItemInput(ing, count, chance)
        }.toTypedArray()

        val outputItems = (0 until buf.readInt()).map {
            val item = Registries.ITEM.get(buf.readIdentifier())
            val count = buf.readInt()
            val chance = buf.readDouble()
            MachineRecipe.RecipeItemOutput(item, count, chance)
        }.toTypedArray()

        val inputFluids = (0 until buf.readInt()).map {
            val fluid = Registries.FLUID.get(buf.readIdentifier())
            val count = buf.readLong()
            val chance = buf.readDouble()
            MachineRecipe.RecipeFluidInput(fluid, count, chance)
        }.toTypedArray()

        val outputFluids = (0 until buf.readInt()).map {
            val fluid = Registries.FLUID.get(buf.readIdentifier())
            val count = buf.readLong()
            val chance = buf.readDouble()
            MachineRecipe.RecipeFluidOutput(fluid, count, chance)
        }.toTypedArray()

        val ticks = buf.readInt()
        val cost = buf.readLong()
        return MachineRecipe(id, type, inputItems, outputItems, inputFluids, outputFluids, ticks ,cost)
    }


}