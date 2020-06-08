package me.steven.indrev.compat

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import me.steven.indrev.compat.plugins.PulverizerCategory
import me.steven.indrev.compat.plugins.PulverizerPlugin
import me.steven.indrev.recipes.CompressorRecipe
import me.steven.indrev.recipes.InfuserRecipe
import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.recipes.RecyclerRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.minecraft.util.Identifier


class REIPlugin : REIPluginV0 {
    override fun getPluginIdentifier(): Identifier = ID

    override fun registerPluginCategories(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerCategory(
            PulverizerCategory(
                PULVERIZING,
                EntryStack.create(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.pulverizing")
        )

        recipeHelper?.registerCategory(
            PulverizerCategory(
                INFUSING,
                EntryStack.create(MachineRegistry.INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.infusing")
        )

        recipeHelper?.registerCategory(
            PulverizerCategory(
                COMPRESSING,
                EntryStack.create(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.compressing")
        )

        recipeHelper?.registerCategory(
            PulverizerCategory(
                RECYCLING,
                EntryStack.create(MachineRegistry.RECYCLER_REGISTRY.block(Tier.MK2)),
                "indrev.category.rei.recycling")
        )
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerRecipes(PULVERIZING, PulverizerRecipe::class.java) { PulverizerPlugin(it) }
        recipeHelper?.registerRecipes(INFUSING, InfuserRecipe::class.java) { PulverizerPlugin(it) }
        recipeHelper?.registerRecipes(COMPRESSING, CompressorRecipe::class.java) { PulverizerPlugin(it) }
        recipeHelper?.registerRecipes(RECYCLING, RecyclerRecipe::class.java) { PulverizerPlugin(it) }
    }

    override fun registerOthers(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerWorkingStations(PULVERIZING, EntryStack.create(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1)))
        recipeHelper?.registerWorkingStations(INFUSING, EntryStack.create(MachineRegistry.INFUSER_REGISTRY.block(Tier.MK1)))
        recipeHelper?.registerWorkingStations(COMPRESSING, EntryStack.create(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1)))
        recipeHelper?.registerWorkingStations(RECYCLING, EntryStack.create(MachineRegistry.RECYCLER_REGISTRY.block(Tier.MK2)))
    }

    companion object {
        val ID = identifier("rei_plugin")
        val PULVERIZING = identifier("plugins/pulverizing")
        val INFUSING = identifier("plugins/infusing")
        val COMPRESSING = identifier("plugins/compressing")
        val RECYCLING = identifier("plugins/recycling")
    }
}