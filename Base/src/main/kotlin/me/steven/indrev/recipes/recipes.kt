package me.steven.indrev.recipes

import me.steven.indrev.utils.createRecipeType
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeType

val FURNACE_RECIPE_PROVIDER = MachineRecipeProvider(RecipeType.SMELTING)

val PULVERIZER_RECIPE_TYPE = identifier("pulverizer").createRecipeType(MachineRecipeType())
val CHEMICAL_INFUSER_RECIPE_TYPE = identifier("chemical_infuser").createRecipeType(MachineRecipeType())
val ALLOY_SMELTER_RECIPE_TYPE = identifier("alloy_smelter").createRecipeType(MachineRecipeType())
val COMPRESSOR_RECIPE_TYPE = identifier("compressor").createRecipeType(MachineRecipeType())