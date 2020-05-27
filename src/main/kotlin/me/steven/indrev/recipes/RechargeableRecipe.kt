package me.steven.indrev.recipes

import com.google.gson.JsonObject
import me.steven.indrev.items.rechargeable.Rechargeable
import me.steven.indrev.utils.identifier
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.util.DefaultedList
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf

class RechargeableRecipe(id: Identifier, group: String, width: Int, height: Int, ingredients: DefaultedList<Ingredient>, output: ItemStack) : ShapedRecipe(id, group, width, height, ingredients, output) {
    override fun craft(craftingInventory: CraftingInventory?): ItemStack {
        val result: ItemStack = super.craft(craftingInventory)
        if (result.item is Rechargeable && craftingInventory != null) {
            var damage = 0
            for (i in 0 until craftingInventory.invSize) {
                val stack = craftingInventory.getInvStack(i)
                if (stack.item is Rechargeable) damage += stack.maxDamage - stack.damage
            }
            result.damage = result.maxDamage - damage.coerceAtMost(result.maxDamage)
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
                return RechargeableRecipe(shaped.id, shaped.group, shaped.width, shaped.height, shaped.previewInputs, shaped.output)
            }

            override fun read(identifier: Identifier?, packetByteBuf: PacketByteBuf?): RechargeableRecipe {
                val shaped = super.read(identifier, packetByteBuf)
                return RechargeableRecipe(shaped.id, shaped.group, shaped.width, shaped.height, shaped.previewInputs, shaped.output)
            }
        }
    }
}