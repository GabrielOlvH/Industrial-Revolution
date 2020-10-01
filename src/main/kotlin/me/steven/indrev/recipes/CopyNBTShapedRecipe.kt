package me.steven.indrev.recipes

import com.google.gson.JsonObject
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry

class CopyNBTShapedRecipe(
    id: Identifier,
    group: String,
    width: Int,
    height: Int,
    ingredients: DefaultedList<Ingredient>,
    output: ItemStack,
    private val copyFrom: Item
): ShapedRecipe(id, group, width, height, ingredients, output) {

    override fun craft(craftingInventory: CraftingInventory): ItemStack {
        val stack = super.craft(craftingInventory)
        val parent = (0 until craftingInventory.size()).map { craftingInventory.getStack(it) }.first { it.item == copyFrom }
        stack.tag = parent.tag?.copy()
        return stack
    }

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("copy_nbt_crafting_shaped")
        val SERIALIZER = Serializer()
        class Serializer : ShapedRecipe.Serializer() {
            override fun read(identifier: Identifier?, jsonObject: JsonObject?): CopyNBTShapedRecipe {
                val shaped = super.read(identifier, jsonObject)
                val copyNbtFrom = JsonHelper.getString(jsonObject?.getAsJsonObject("result"), "copyNbt")
                val item = Registry.ITEM.get(Identifier(copyNbtFrom))
                return CopyNBTShapedRecipe(shaped.id, shaped.group, shaped.width, shaped.height, shaped.previewInputs, shaped.output, item)
            }

            override fun write(packetByteBuf: PacketByteBuf?, shapedRecipe: ShapedRecipe?) {
                super.write(packetByteBuf, shapedRecipe)
                packetByteBuf?.writeIdentifier(Registry.ITEM.getId((shapedRecipe as CopyNBTShapedRecipe).copyFrom))
            }

            override fun read(identifier: Identifier?, packetByteBuf: PacketByteBuf): CopyNBTShapedRecipe {
                val shaped = super.read(identifier, packetByteBuf)
                val copyNbtFrom = packetByteBuf.readIdentifier()
                val item = Registry.ITEM.get(copyNbtFrom)
                return CopyNBTShapedRecipe(shaped.id, shaped.group, shaped.width, shaped.height, shaped.previewInputs, shaped.output, item)
            }
        }
    }
}