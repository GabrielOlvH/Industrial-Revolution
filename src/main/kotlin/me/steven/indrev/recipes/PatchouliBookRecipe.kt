package me.steven.indrev.recipes

import com.google.gson.JsonObject
import me.steven.indrev.utils.identifier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList

class PatchouliBookRecipe(id: Identifier, group: String, output: ItemStack, input: DefaultedList<Ingredient>) :
    ShapelessRecipe(id, group, output, input) {

    companion object {
        val TYPE = object : RecipeType<PatchouliBookRecipe> {}
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("patchouli_recipe")

        class Serializer : ShapelessRecipe.Serializer() {
            override fun read(identifier: Identifier, jsonObject: JsonObject?): PatchouliBookRecipe {
                val shapeless = super.read(identifier, jsonObject)
                val output = shapeless.output
                output.tag = CompoundTag().also { it.putString("patchouli:book", "indrev:indrev") }
                return PatchouliBookRecipe(identifier, shapeless.group, output, shapeless.previewInputs)
            }
        }
    }
}