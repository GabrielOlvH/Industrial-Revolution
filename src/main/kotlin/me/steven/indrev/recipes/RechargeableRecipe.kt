package me.steven.indrev.recipes

import com.google.gson.JsonObject
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import team.reborn.energy.Energy

class RechargeableRecipe(id: Identifier, group: String, width: Int, height: Int, ingredients: DefaultedList<Ingredient>, output: ItemStack) : ShapedRecipe(id, group, width, height, ingredients, output) {
    override fun craft(craftingInventory: CraftingInventory?): ItemStack {
        val result: ItemStack = super.craft(craftingInventory)
        if (Energy.valid(result) && craftingInventory != null) {
            val handler = Energy.of(result)
            for (i in 0 until craftingInventory.size()) {
                val stack = craftingInventory.getStack(i)
                if (Energy.valid(stack)) Energy.of(stack).into(handler).move()
            }
        }
        return result
    }

    override fun getType(): RecipeType<*> = RecipeType.CRAFTING

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("crafting_shaped")
        val SERIALIZER = Serializer()

        class Serializer : ShapedRecipe.Serializer() {
            override fun read(identifier: Identifier?, jsonObject: JsonObject?): RechargeableRecipe {
                val shaped = super.read(identifier, jsonObject)
                val group = JsonHelper.getString(jsonObject, "group", "")
                return RechargeableRecipe(shaped.id, group, shaped.width, shaped.height, shaped.previewInputs, shaped.output)
            }

            override fun read(identifier: Identifier?, packetByteBuf: PacketByteBuf?): RechargeableRecipe {
                val shaped = super.read(identifier, packetByteBuf)
                return RechargeableRecipe(shaped.id, shaped.group, shaped.width, shaped.height, shaped.previewInputs, shaped.output)
            }
        }
    }
}