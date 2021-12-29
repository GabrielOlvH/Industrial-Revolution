package me.steven.indrev.recipes.machines

import com.google.gson.JsonObject
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.*
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

abstract class IRFluidRecipe : IRRecipe {
    abstract val fluidInput: Array<IRFluidAmount>
    abstract val fluidOutput: Array<IRFluidAmount>

    override fun matches(inv: List<ItemStack>, fluidVolume: List<IRFluidTank>): Boolean {
        return when {
            fluidInput.isNotEmpty() -> fluidVolume.isNotEmpty() && fluidInput.indices.all { index ->
                val vol = fluidVolume[index]
                vol.resource == fluidInput[index].resource && vol.amount >= fluidInput[index].amount() } && super.matches(inv, fluidVolume)
            else -> super.matches(inv, fluidVolume)
        }
    }

    override fun canStart(component: CraftingComponent<*>): Boolean {
        val fluidComponent = component.fluidComponent!!
        val outputTankVolume = fluidComponent.outputTanks
        if (fluidOutput.isNotEmpty() && fluidOutput.indices.any { index ->
                val vol = fluidComponent[outputTankVolume[index]]
                !vol.isEmpty && (vol.resource != fluidOutput[index].resource || vol.amount + fluidOutput[index].amount() > fluidComponent.limit)
            }
        )
            return false
        return super.canStart(component)
    }

    open class IRFluidRecipeSerializer<T : IRFluidRecipe>(private val factory: (Identifier, Array<InputEntry>, Array<OutputEntry>, Array<IRFluidAmount>, Array<IRFluidAmount>, Int) -> T) : RecipeSerializer<T> {
        override fun read(id: Identifier, buf: PacketByteBuf): T {
            val ticks = buf.readInt()
            val inputFluidsSize = buf.readInt()
            val inputFluids = mutableListOf<IRFluidAmount>()
            (0 until inputFluidsSize).forEach { _ ->
                inputFluids.add(fromPacket(buf))
            }

            val outputFluidsSize = buf.readInt()
            val outputFluids = mutableListOf<IRFluidAmount>()
            (0 until outputFluidsSize).forEach { _ ->
                outputFluids.add(fromPacket(buf))
            }
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
            return factory(id, ingredients.toTypedArray(), output.toTypedArray(), inputFluids.toTypedArray(), outputFluids.toTypedArray(), ticks)
        }

        override fun read(id: Identifier, json: JsonObject): T {
            val ingredients = IRRecipe.ingredientsFromElement(json["ingredients"])
            val ticks = json.get("processTime").asInt
            val output = IRRecipe.itemStacksFromElement(json["output"])
            val fluidInputJson = json.get("fluidInput")
            val fluidInput = if (fluidInputJson == null) emptyArray() else getFluidFromJson(fluidInputJson)

            val fluidOutputJson = json.get("fluidOutput")
            val fluidOutput = if (fluidOutputJson == null) emptyArray() else getFluidFromJson(fluidOutputJson)

            return factory(id, ingredients, output, fluidInput, fluidOutput, ticks)
        }

        override fun write(buf: PacketByteBuf, recipe: T) {
            buf.writeInt(recipe.ticks)
            buf.writeInt(recipe.fluidInput.size)
            recipe.fluidInput.forEach { fluidInput ->
                fluidInput.toPacket(buf)
            }
            buf.writeInt(recipe.fluidOutput.size)
            recipe.fluidOutput.forEach { fluidOutput ->
                fluidOutput.toPacket(buf)
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