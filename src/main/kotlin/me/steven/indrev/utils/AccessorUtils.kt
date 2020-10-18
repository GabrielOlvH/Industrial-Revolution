package me.steven.indrev.utils

import me.steven.indrev.mixin.*
import me.steven.indrev.recipes.machines.IRRecipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.Property
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.collection.WeightedList
import team.reborn.energy.EnergyHandler
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergySide

val <T> WeightedList<T>.entries: MutableList<WeightedList.Entry<T>>
    get() = (this as AccessorWeightedList<T>).entries

val <T> WeightedList.Entry<T>.weight: Int
    get() = (this as AccessorWeightedListEntry).weight

val EnergyHandler.holder: EnergyHolder
    get() = (this as AccessorEnergyHandler).holder

val EnergyHandler.side: EnergySide
    get() = (this as AccessorEnergyHandler).side

fun <T : IRRecipe> RecipeManager.getAllOfType(type: RecipeType<T>): Map<Identifier, T>
        = (this as AccessorRecipeManager).indrev_getAllOfType(type) as Map<Identifier, T>

val ScreenHandler.properties: List<Property>
    get() = (this as AccessorScreenHandler).properties