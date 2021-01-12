package me.steven.indrev.compat.rei

import me.shedaniel.rei.api.EntryRegistry
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import me.shedaniel.rei.plugin.information.DefaultInformationDisplay
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.compat.rei.categories.IRMachineRecipeCategory
import me.steven.indrev.compat.rei.categories.IRSawmillRecipeCategory
import me.steven.indrev.compat.rei.plugins.IRMachinePlugin
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.identifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object REIPlugin : REIPluginV0 {
    override fun getPluginIdentifier(): Identifier = ID

    override fun registerEntries(entryRegistry: EntryRegistry?) {
        fun registerCharged(vararg items: Item) {
            items.forEach { item ->
                entryRegistry?.registerEntriesAfter(EntryStack.create(item),
                    EntryStack.create(ItemStack(item).also { it.orCreateTag.putDouble("energy", energyOf(it)!!.energyCapacity) }))
            }
        }

        registerCharged(
            IRRegistry.MINING_DRILL_MK1,
            IRRegistry.MINING_DRILL_MK2,
            IRRegistry.MINING_DRILL_MK3,
            IRRegistry.MINING_DRILL_MK4,
            IRRegistry.MODULAR_ARMOR_HELMET,
            IRRegistry.MODULAR_ARMOR_CHEST,
            IRRegistry.MODULAR_ARMOR_LEGGINGS,
            IRRegistry.MODULAR_ARMOR_BOOTS,
            IRRegistry.PORTABLE_CHARGER_ITEM,
            IRRegistry.BATTERY
        )

        entryRegistry?.registerEntriesAfter(EntryStack.create(IRRegistry.GAMER_AXE_ITEM),
            EntryStack.create(ItemStack(IRRegistry.GAMER_AXE_ITEM).also {
                val tag = it.orCreateTag
                tag.putDouble("energy", energyOf(it)!!.energyCapacity)
                tag.putBoolean("Active", true)
                tag.putFloat("Progress", 1f)
            }))

    }

    override fun registerPluginCategories(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                PULVERIZING,
                EntryStack.create(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.pulverizing"
            )
        )

        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                INFUSING,
                EntryStack.create(MachineRegistry.INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.infusing"
            )
        )

        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                COMPRESSING,
                EntryStack.create(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.compressing"
            )
        )

        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                RECYCLING,
                EntryStack.create(MachineRegistry.RECYCLER_REGISTRY.block(Tier.MK2)),
                "indrev.category.rei.recycling"
            )
        )

        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                FLUID_INFUSER,
                EntryStack.create(MachineRegistry.FLUID_INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.fluid_infusing"
            )
        )

        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                CONDENSER,
                EntryStack.create(MachineRegistry.CONDENSER_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.condensing"
            )
        )

        recipeHelper?.registerCategory(
            IRMachineRecipeCategory(
                SMELTER,
                EntryStack.create(MachineRegistry.SMELTER_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.smelting"
            )
        )

        recipeHelper?.registerCategory(
            IRSawmillRecipeCategory(
                SAWMILL,
                EntryStack.create(MachineRegistry.SAWMILL_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.sawmill"
            )
        )
    }

    override fun registerRecipeDisplays(recipeHelper: RecipeHelper?) {
        recipeHelper?.registerRecipes(PULVERIZING, PulverizerRecipe::class.java) {
            IRMachinePlugin(it, PULVERIZING)
        }
        recipeHelper?.registerRecipes(INFUSING, InfuserRecipe::class.java) {
            IRMachinePlugin(it, INFUSING)
        }
        recipeHelper?.registerRecipes(COMPRESSING, CompressorRecipe::class.java) {
            IRMachinePlugin(it, COMPRESSING)
        }
        recipeHelper?.registerRecipes(RECYCLING, RecyclerRecipe::class.java) {
            IRMachinePlugin(it, RECYCLING)
        }
        recipeHelper?.registerRecipes(FLUID_INFUSER, FluidInfuserRecipe::class.java) {
            IRMachinePlugin(it, FLUID_INFUSER)
        }
        recipeHelper?.registerRecipes(SMELTER, SmelterRecipe::class.java) {
            IRMachinePlugin(it, SMELTER)
        }
        recipeHelper?.registerRecipes(CONDENSER, CondenserRecipe::class.java) {
            IRMachinePlugin(it, CONDENSER)
        }
        recipeHelper?.registerRecipes(SAWMILL, SawmillRecipe::class.java) {
            IRMachinePlugin(it, SAWMILL)
        }
    }

    override fun registerOthers(recipeHelper: RecipeHelper?) {
        MachineRegistry.PULVERIZER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                PULVERIZING,
                EntryStack.create(block)
            )
        }
        MachineRegistry.INFUSER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                INFUSING,
                EntryStack.create(block)
            )
        }
        MachineRegistry.COMPRESSOR_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                COMPRESSING,
                EntryStack.create(block)
            )
        }
        MachineRegistry.RECYCLER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                RECYCLING,
                EntryStack.create(block)
            )
        }
        MachineRegistry.FLUID_INFUSER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                FLUID_INFUSER,
                EntryStack.create(block)
            )
        }
        MachineRegistry.CONDENSER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                CONDENSER,
                EntryStack.create(block)
            )
        }
        MachineRegistry.SMELTER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                SMELTER,
                EntryStack.create(block)
            )
        }
        MachineRegistry.SAWMILL_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                SAWMILL,
                EntryStack.create(block)
            )
        }

        MachineRegistry.MAP.entries.distinctBy { (_, v) -> v }.forEach { (id, registry) ->
            if (registry.upgradeable && registry.tiers.size > 1) {
                registry.forEachBlock { tier, block ->
                    val entryStack = EntryStack.create(block)
                    if (tier != Tier.CREATIVE && tier != Tier.MK1 && recipeHelper?.getRecipesFor(entryStack)?.isEmpty() == true) {
                        val info = DefaultInformationDisplay.createFromEntry(entryStack, TranslatableText(block.translationKey))
                        info.lines(TranslatableText("indrev.category.rei.upgrading",
                            TranslatableText("item.indrev.tier_upgrade_" + tier.toString().toLowerCase()).formatted(Formatting.DARK_GRAY),
                            TranslatableText(registry.block(registry.tiers[registry.tiers.indexOf(tier) - 1]).translationKey).formatted(Formatting.DARK_GRAY),
                            tier.toString()))
                        recipeHelper.registerDisplay(info)
                    }
                }
            }
        }
    }

    private val ID = identifier("rei_plugin")
    private val PULVERIZING = identifier("plugins/pulverizing")
    private val INFUSING = identifier("plugins/infusing")
    private val COMPRESSING = identifier("plugins/compressing")
    private val RECYCLING = identifier("plugins/recycling")
    private val SMELTER = identifier("plugins/smelter")
    private val CONDENSER = identifier("plugins/condenser")
    private val FLUID_INFUSER = identifier("plugins/fluid_infusing")
    private val SAWMILL = identifier("plugins/sawmill")
}