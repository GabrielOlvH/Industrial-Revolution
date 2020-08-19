package me.steven.indrev.compat.rei

import me.shedaniel.rei.api.EntryRegistry
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import me.steven.indrev.compat.rei.categories.BaseMachineRecipeCategory
import me.steven.indrev.compat.rei.categories.CondenserRecipeCategory
import me.steven.indrev.compat.rei.categories.FluidInfuserRecipeCategory
import me.steven.indrev.compat.rei.categories.SmelterRecipeCategory
import me.steven.indrev.compat.rei.plugins.BaseMachinePlugin
import me.steven.indrev.compat.rei.plugins.CondenserMachinePlugin
import me.steven.indrev.compat.rei.plugins.FluidInfuserMachinePlugin
import me.steven.indrev.compat.rei.plugins.SmelterMachinePlugin
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.minecraft.util.Identifier
import vazkii.patchouli.api.PatchouliAPI

object REIPlugin : REIPluginV0 {
    override fun getPluginIdentifier(): Identifier = ID

    override fun registerEntries(entryRegistry: EntryRegistry) {
        entryRegistry.registerEntry(EntryStack.create(PatchouliAPI.instance.getBookStack(identifier("indrev"))))
    }

    override fun registerPluginCategories(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerCategory(
            BaseMachineRecipeCategory(
                PULVERIZING,
                EntryStack.create(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.pulverizing"
            )
        )

        recipeHelper?.registerCategory(
            BaseMachineRecipeCategory(
                INFUSING,
                EntryStack.create(MachineRegistry.INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.infusing"
            )
        )

        recipeHelper?.registerCategory(
            BaseMachineRecipeCategory(
                COMPRESSING,
                EntryStack.create(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.compressing"
            )
        )

        recipeHelper?.registerCategory(
            BaseMachineRecipeCategory(
                RECYCLING,
                EntryStack.create(MachineRegistry.RECYCLER_REGISTRY.block(Tier.MK2)),
                "indrev.category.rei.recycling"
            )
        )

        recipeHelper?.registerCategory(
            FluidInfuserRecipeCategory(
                FLUID_INFUSER,
                EntryStack.create(MachineRegistry.FLUID_INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.fluid_infusing"
            )
        )

        recipeHelper?.registerCategory(
            CondenserRecipeCategory(
                CONDENSER,
                EntryStack.create(MachineRegistry.CONDENSER_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.condensing"
            )
        )

        recipeHelper?.registerCategory(
            SmelterRecipeCategory(
                SMELTER,
                EntryStack.create(MachineRegistry.SMELTER_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.smelting"
            )
        )
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerRecipes(PULVERIZING, PulverizerRecipe::class.java) {
            BaseMachinePlugin(
                it,
                PULVERIZING
            )
        }
        recipeHelper?.registerRecipes(INFUSING, InfuserRecipe::class.java) {
            BaseMachinePlugin(
                it,
                INFUSING
            )
        }
        recipeHelper?.registerRecipes(COMPRESSING, CompressorRecipe::class.java) {
            BaseMachinePlugin(
                it,
                COMPRESSING
            )
        }
        recipeHelper?.registerRecipes(RECYCLING, RecyclerRecipe::class.java) {
            BaseMachinePlugin(
                it,
                RECYCLING
            )
        }
        recipeHelper?.registerRecipes(FLUID_INFUSER, FluidInfuserRecipe::class.java) {
            FluidInfuserMachinePlugin(it, FLUID_INFUSER)
        }
        recipeHelper?.registerRecipes(SMELTER, SmelterRecipe::class.java) {
            SmelterMachinePlugin(it, SMELTER)
        }

        recipeHelper?.registerRecipes(CONDENSER, CondenserRecipe::class.java) {
            CondenserMachinePlugin(it, CONDENSER)
        }
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
        recipeHelper?.registerWorkingStations(
            FLUID_INFUSER,
            EntryStack.create(MachineRegistry.FLUID_INFUSER_REGISTRY.block(Tier.MK1))
        )
        recipeHelper?.registerWorkingStations(
            CONDENSER,
            EntryStack.create(MachineRegistry.CONDENSER_REGISTRY.block(Tier.MK4))
        )
        recipeHelper?.registerWorkingStations(
            SMELTER,
            EntryStack.create(MachineRegistry.SMELTER_REGISTRY.block(Tier.MK4))
        )
    }

    private val ID = identifier("rei_plugin")
    private val PULVERIZING = identifier("plugins/pulverizing")
    private val INFUSING = identifier("plugins/infusing")
    private val COMPRESSING = identifier("plugins/compressing")
    private val RECYCLING = identifier("plugins/recycling")
    private val SMELTER = identifier("plugins/smelter")
    private val CONDENSER = identifier("plugins/condenser")
    private val FLUID_INFUSER = identifier("plugins/fluid_infusing")
}