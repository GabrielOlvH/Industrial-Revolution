package me.steven.indrev.recipes

import net.minecraft.fluid.Fluid
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier
import net.minecraft.world.World

open class MachineRecipe(
    private val id: Identifier,
    private val type: RecipeType<*>,
    val itemInput: Array<RecipeItemInput>,
    val itemOutput: Array<RecipeItemOutput>,
    val fluidInput: Array<RecipeFluidInput>,
    val fluidOutput: Array<RecipeFluidOutput>,
    val ticks: Int,
    val cost: Long
) : Recipe<Inventory> {

    override fun matches(inventory: Inventory, world: World): Boolean = throw UnsupportedOperationException()

    override fun craft(inventory: Inventory?): ItemStack = throw UnsupportedOperationException()

    override fun fits(width: Int, height: Int): Boolean = throw UnsupportedOperationException()

    override fun isIgnoredInRecipeBook(): Boolean = true

    override fun getOutput(): ItemStack {
        return ItemStack(itemOutput[0].item, itemOutput[0].count)
    }

    override fun getId(): Identifier = id

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    override fun getType(): RecipeType<*> = type

    data class RecipeItemInput(val ingredient: Ingredient, val count: Int, val chance: Double)
    data class RecipeItemOutput(val item: Item, val count: Int, val chance: Double)
    data class RecipeFluidInput(val fluid: Fluid, val amount: Long, val chance: Double)
    data class RecipeFluidOutput(val fluid: Fluid, val amount: Long, val chance: Double)

    companion object {
        val SERIALIZER = MachineRecipeSerializer()
    }
}