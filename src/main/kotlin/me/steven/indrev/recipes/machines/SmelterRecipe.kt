package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import com.google.gson.JsonObject
import me.steven.indrev.utils.BLOCK_AMOUNT
import me.steven.indrev.utils.INGOT_AMOUNT
import me.steven.indrev.utils.NUGGET_AMOUNT
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

class SmelterRecipe(
    private val id: Identifier,
    val processTime: Int,
    val input: Ingredient,
    val fluid: FluidVolume
) : Recipe<Inventory> {
    override fun craft(inv: Inventory?): ItemStack = ItemStack.EMPTY

    override fun getId(): Identifier = id

    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    override fun getOutput(): ItemStack = ItemStack.EMPTY

    override fun getPreviewInputs(): DefaultedList<Ingredient> = DefaultedList.of<Ingredient>().also { it.add(input) }

    override fun matches(inv: Inventory?, world: World?): Boolean = input.test(inv?.getStack(0))

    companion object {
        val IDENTIFIER = identifier("smelter")
        val TYPE = object : RecipeType<SmelterRecipe> {}
        val SERIALIZER = Serializer()

        class Serializer : RecipeSerializer<SmelterRecipe> {
            override fun write(buf: PacketByteBuf, recipe: SmelterRecipe) {
                recipe.input.write(buf)
                buf.writeInt(recipe.processTime)
                buf.writeIdentifier(recipe.fluid.fluidKey.entry.id)
                recipe.fluid.amount().toMcBuffer(buf)
            }

            override fun read(id: Identifier, json: JsonObject): SmelterRecipe {
                val input = Ingredient.fromJson(json.getAsJsonObject("ingredient"))
                val processTime = json.get("processTime").asInt
                val fluidObj = json.getAsJsonObject("fluid")
                val fluidId = fluidObj.get("fluid").asString
                val fluidKey = FluidKeys.get(Registry.FLUID.get(Identifier(fluidId)))
                val amount = JsonHelper.getLong(fluidObj, "amount", 1)
                val fluidAmount = when (val type = fluidObj.get("type").asString) {
                    "nugget" -> NUGGET_AMOUNT
                    "ingot" -> INGOT_AMOUNT
                    "block" -> BLOCK_AMOUNT
                    else -> throw IllegalArgumentException("unknown amount type $type")
                }.mul(amount)
                val fluidVolume = object : FluidVolume(fluidKey, fluidAmount) {}
                return SmelterRecipe(id, processTime, input, fluidVolume)
            }

            override fun read(id: Identifier, buf: PacketByteBuf): SmelterRecipe {
                val stack = Ingredient.fromPacket(buf)
                val processTime = buf.readInt()
                val fluidId = buf.readIdentifier()
                val fluidAmount = FluidAmount.fromMcBuffer(buf)
                val fluidKey = FluidKeys.get(Registry.FLUID.get(fluidId))
                val fluidVolume = object : FluidVolume(fluidKey, fluidAmount) {}

                return SmelterRecipe(id, processTime, stack, fluidVolume)
            }

        }
    }
}