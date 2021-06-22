package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonObject
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.getFluidFromJson
import me.steven.indrev.utils.identifier
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class ElectrolysisRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidInput: FluidVolume,
    override val fluidOutput: FluidVolume,
    val secondFluidOutput: FluidVolume,
    override val ticks: Int
) : IRFluidRecipe() {

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    override fun canStart(component: CraftingComponent<*>): Boolean {
        val firstOutput = component.fluidComponent!!.tanks[1]
        if (!firstOutput.isEmpty && (firstOutput.fluidKey != fluidOutput.fluidKey || firstOutput.amount()
                .add(fluidOutput.amount()) > component.fluidComponent!!.limit)
        )
            return false
        val secondOutput = component.fluidComponent!!.tanks[2]
        if (!secondOutput.isEmpty && (secondOutput.fluidKey != secondFluidOutput.fluidKey || secondOutput.amount()
                .add(secondFluidOutput.amount()) > component.fluidComponent!!.limit)
        )
            return false
        return true
    }

    companion object {
        val IDENTIFIER = identifier("electrolysis")
        val TYPE = IRRecipeType<ElectrolysisRecipe>(IDENTIFIER)
        val SERIALIZER = Serializer()

        class Serializer : RecipeSerializer<ElectrolysisRecipe> {
            override fun read(id: Identifier, buf: PacketByteBuf): ElectrolysisRecipe {
                val ticks = buf.readInt()

                val fluidId = buf.readIdentifier()
                val fluidAmount = FluidAmount.fromMcBuffer(buf)
                val fluidKey = FluidKeys.get(Registry.FLUID.get(fluidId))
                val fluidInput = fluidKey.withAmount(fluidAmount)

                val outputFluidId = buf.readIdentifier()
                val outputFluidAmount = FluidAmount.fromMcBuffer(buf)
                val outputFluidKey = FluidKeys.get(Registry.FLUID.get(outputFluidId))
                val fluidOutput = outputFluidKey.withAmount(outputFluidAmount)

                val secondOutputFluidId = buf.readIdentifier()
                val secondOutputFluidAmount = FluidAmount.fromMcBuffer(buf)
                val secondOutputFluidKey = FluidKeys.get(Registry.FLUID.get(secondOutputFluidId))
                val fluidSecondOutput = secondOutputFluidKey.withAmount(secondOutputFluidAmount)

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
                return ElectrolysisRecipe(id, ingredients.toTypedArray(), output.toTypedArray(), fluidInput, fluidOutput, fluidSecondOutput, ticks)
            }

            override fun read(id: Identifier, json: JsonObject): ElectrolysisRecipe {
                val ingredients = IRRecipe.ingredientsFromElement(json["ingredients"])
                val ticks = json.get("processTime").asInt
                val output = IRRecipe.itemStacksFromElement(json["output"])
                val fluidInputJson = json.getAsJsonObject("fluidInput")
                val fluidInput = getFluidFromJson(fluidInputJson)

                val fluidOutputJson = json.getAsJsonObject("fluidOutput")
                val fluidOutput = if (fluidOutputJson == null) FluidKeys.EMPTY.withAmount(FluidAmount.ZERO) else getFluidFromJson(fluidOutputJson)

                val secondFluidOutputJson = json.getAsJsonObject("secondFluidOutput")
                val secondFluidOutput = if (secondFluidOutputJson == null) FluidKeys.EMPTY.withAmount(FluidAmount.ZERO) else getFluidFromJson(secondFluidOutputJson)

                return ElectrolysisRecipe(id, ingredients, output, fluidInput, fluidOutput, secondFluidOutput, ticks)
            }

            override fun write(buf: PacketByteBuf, recipe: ElectrolysisRecipe) {
                buf.writeInt(recipe.ticks)
                buf.writeIdentifier(recipe.fluidInput.fluidKey.entry.id)
                recipe.fluidInput.amount().toMcBuffer(buf)
                buf.writeIdentifier(recipe.fluidOutput.fluidKey.entry.id)
                recipe.fluidOutput.amount().toMcBuffer(buf)
                buf.writeIdentifier(recipe.secondFluidOutput.fluidKey.entry.id)
                recipe.secondFluidOutput.amount().toMcBuffer(buf)
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
}