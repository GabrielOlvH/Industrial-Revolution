package me.steven.indrev.recipes

import com.google.gson.JsonObject
import me.steven.indrev.FabricRecipeRemainder
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList

class SelfRemainderRecipe(id: Identifier, group: String, output: ItemStack, ingredients: DefaultedList<Ingredient>) :
    ShapelessRecipe(id, group, CraftingRecipeCategory.MISC, output, ingredients) {

    override fun craft(
        recipeInputInventory: RecipeInputInventory?,
        dynamicRegistryManager: DynamicRegistryManager?
    ): ItemStack {
        return getOutput(dynamicRegistryManager).copy()
    }

    override fun getRemainder(input: RecipeInputInventory): DefaultedList<ItemStack> {
        val defaultedList = DefaultedList.ofSize(input.size(), ItemStack.EMPTY)
        for (i in 0 until input.size()) {
            val invStack: ItemStack = input.getStack(i)
            if (invStack.item is FabricRecipeRemainder) {
                val remainder =
                    (invStack.item as FabricRecipeRemainder).getRemainder(invStack.copy(), input, null)
                defaultedList[i] = remainder
            }
        }
        return defaultedList
    }

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("selfremainder")
        val SERIALIZER = Serializer()

        class Serializer : ShapelessRecipe.Serializer() {
            override fun read(identifier: Identifier, jsonObject: JsonObject): SelfRemainderRecipe {
                val shaped = super.read(identifier, jsonObject)!!
                val group = JsonHelper.getString(jsonObject, "group", "")
                return SelfRemainderRecipe(shaped.id, group!!, shaped.getOutput(null), shaped.ingredients)
            }

            override fun read(identifier: Identifier?, packetByteBuf: PacketByteBuf?): SelfRemainderRecipe {
                val shaped = super.read(identifier, packetByteBuf)
                return SelfRemainderRecipe(shaped.id, shaped.group, shaped.getOutput(null), shaped.ingredients)
            }
        }
    }
}