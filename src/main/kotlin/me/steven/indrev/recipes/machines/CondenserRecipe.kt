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
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

class CondenserRecipe(
    private val id: Identifier,
    val processTime: Int,
    private val output: ItemStack,
    val fluid: FluidVolume
) : Recipe<Inventory> {
    override fun craft(inv: Inventory?): ItemStack = output.copy()

    override fun getId(): Identifier = id

    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    override fun getOutput(): ItemStack = output

    override fun matches(inv: Inventory?, world: World?): Boolean = false

    companion object {
        val IDENTIFIER = identifier("condenser")
        val TYPE = object : RecipeType<CondenserRecipe> {}
        val SERIALIZER = Serializer()

        class Serializer : RecipeSerializer<CondenserRecipe> {
            override fun write(buf: PacketByteBuf, recipe: CondenserRecipe) {
                buf.writeItemStack(recipe.output)
                buf.writeInt(recipe.processTime)
                buf.writeIdentifier(recipe.fluid.fluidKey.entry.id)
                recipe.fluid.amount().toMcBuffer(buf)
            }

            override fun read(id: Identifier, json: JsonObject): CondenserRecipe {
                val result = json.get("input").asJsonObject
                val output = ItemStack {
                    Registry.ITEM.getOrEmpty(Identifier(result.get("item").asString)).orElse(null)
                        ?: throw IllegalArgumentException("no such item $result")
                }
                output.count = result.get("count").asInt
                val processTime = json.get("processTime").asInt
                val fluidObj = json.getAsJsonObject("fluid")
                val fluidId = fluidObj.get("fluid").asString
                val fluidKey = FluidKeys.get(Registry.FLUID.get(Identifier(fluidId)))
                val amount = JsonHelper.getLong(fluidObj, "count", 1)
                val fluidAmount = when (val type = fluidObj.get("type").asString) {
                    "nugget" -> NUGGET_AMOUNT
                    "ingot" -> INGOT_AMOUNT
                    "block" -> BLOCK_AMOUNT
                    else -> throw IllegalArgumentException("unknown amount type $type")
                }.mul(amount)
                val fluidVolume = object : FluidVolume(fluidKey, fluidAmount) {}
                return CondenserRecipe(id, processTime, output, fluidVolume)
            }

            override fun read(id: Identifier, buf: PacketByteBuf): CondenserRecipe {
                val stack = buf.readItemStack()
                val processTime = buf.readInt()
                val fluidId = buf.readIdentifier()
                val fluidAmount = FluidAmount.fromMcBuffer(buf)
                val fluidKey = FluidKeys.get(Registry.FLUID.get(fluidId))
                val fluidVolume = object : FluidVolume(fluidKey, fluidAmount) {}

                return CondenserRecipe(id, processTime, stack, fluidVolume)
            }

        }
    }
}