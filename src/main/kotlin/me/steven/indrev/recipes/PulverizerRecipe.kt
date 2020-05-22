package me.steven.indrev.recipes

import me.steven.indrev.identifier
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.util.Pair
import net.minecraft.world.World

class PulverizerRecipe(private val id: Identifier, val processTime: Int, private val output: ItemStack, val extraOutput: Pair<ItemStack, Double>, val input: Ingredient) : Recipe<Inventory> {
    override fun craft(inv: Inventory?): ItemStack = output.copy()

    override fun getId(): Identifier = id

    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    override fun getOutput(): ItemStack = output

    override fun matches(inv: Inventory?, world: World?): Boolean = input.test(inv?.getInvStack(0))

    companion object {
        val TYPE = object : RecipeType<PulverizerRecipe> {}
        val SERIALIZER = PulverizerRecipeSerializer()
        val IDENTIFIER = identifier("pulverize")
    }
}