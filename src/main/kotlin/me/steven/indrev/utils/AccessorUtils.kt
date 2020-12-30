package me.steven.indrev.utils

import me.steven.indrev.mixin.*
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.screen.Property
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.collection.WeightedList

val <T> WeightedList<T>.entries: MutableList<WeightedList.Entry<T>>
    get() = (this as AccessorWeightedList<T>).entries

val <T> WeightedList.Entry<T>.weight: Int
    get() = (this as AccessorWeightedListEntry).weight

fun <T : Recipe<Inventory>> RecipeManager.getAllOfType(type: RecipeType<T>): Map<Identifier, T>
        = (this as AccessorRecipeManager).indrev_getAllOfType(type) as Map<Identifier, T>

fun RecipeManager.getRecipes(): Map<RecipeType<*>, Map<Identifier, Recipe<*>>> = (this as AccessorRecipeManager).recipes

val ScreenHandler.properties: List<Property>
    get() = (this as AccessorScreenHandler).properties

val AbstractCookingRecipe.input: Ingredient
    get() = (this as AccessorAbstractCookingRecipe).input

fun AnimalEntity.eat(player: PlayerEntity, stack: ItemStack) = (this as AccessorAnimalEntity).callEat(player, stack)