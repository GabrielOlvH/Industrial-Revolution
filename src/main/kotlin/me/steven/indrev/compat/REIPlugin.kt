package me.steven.indrev.compat

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import me.steven.indrev.compat.plugins.MachinePlugin
import me.steven.indrev.compat.plugins.MachineRecipeCategory
import me.steven.indrev.recipes.CompressorRecipe
import me.steven.indrev.recipes.InfuserRecipe
import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.recipes.RecyclerRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.minecraft.util.Identifier

object REIPlugin : REIPluginV0 {
    override fun getPluginIdentifier(): Identifier = ID

    override fun registerPluginCategories(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerCategory(
            MachineRecipeCategory(
                PULVERIZING,
                EntryStack.create(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.pulverizing"
            )
        )

        recipeHelper?.registerCategory(
            MachineRecipeCategory(
                INFUSING,
                EntryStack.create(MachineRegistry.INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.infusing")
        )

        recipeHelper?.registerCategory(
            MachineRecipeCategory(
                COMPRESSING,
                EntryStack.create(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.compressing")
        )

        recipeHelper?.registerCategory(
            MachineRecipeCategory(
                RECYCLING,
                EntryStack.create(MachineRegistry.RECYCLER_REGISTRY.block(Tier.MK2)),
                "indrev.category.rei.recycling")
        )
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerRecipes(PULVERIZING, PulverizerRecipe::class.java) { MachinePlugin(it, PULVERIZING) }
        recipeHelper?.registerRecipes(INFUSING, InfuserRecipe::class.java) { MachinePlugin(it, INFUSING) }
        recipeHelper?.registerRecipes(COMPRESSING, CompressorRecipe::class.java) { MachinePlugin(it, COMPRESSING) }
        recipeHelper?.registerRecipes(RECYCLING, RecyclerRecipe::class.java) { MachinePlugin(it, RECYCLING) }
    }

    override fun registerOthers(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerWorkingStations(
            PULVERIZING,
            EntryStack.create(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1))
        )
        recipeHelper?.registerWorkingStations(
            INFUSING,
            EntryStack.create(MachineRegistry.INFUSER_REGISTRY.block(Tier.MK1))
        )
        recipeHelper?.registerWorkingStations(
            COMPRESSING,
            EntryStack.create(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1))
        )
        recipeHelper?.registerWorkingStations(
            RECYCLING,
            EntryStack.create(MachineRegistry.RECYCLER_REGISTRY.block(Tier.MK2))
        )
    }

    private val ID = identifier("rei_plugin")
    private val PULVERIZING = identifier("plugins/pulverizing")
    private val INFUSING = identifier("plugins/infusing")
    private val COMPRESSING = identifier("plugins/compressing")
    private val RECYCLING = identifier("plugins/recycling")
}