package me.steven.indrev.compat.rei

import me.shedaniel.rei.api.EntryRegistry
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.RecipeHelper
import me.shedaniel.rei.api.plugins.REIPluginV0
import me.shedaniel.rei.plugin.information.DefaultInformationDisplay
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.compat.rei.categories.IRMachineRecipeCategory
import me.steven.indrev.compat.rei.categories.IRModuleCraftingRecipeCategory
import me.steven.indrev.compat.rei.categories.IRSawmillRecipeCategory
import me.steven.indrev.compat.rei.plugins.IRMachinePlugin
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.entries
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.chunkveins.VeinType
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

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
            IRItemRegistry.MINING_DRILL_MK1,
            IRItemRegistry.MINING_DRILL_MK2,
            IRItemRegistry.MINING_DRILL_MK3,
            IRItemRegistry.MINING_DRILL_MK4,
            IRItemRegistry.MODULAR_ARMOR_HELMET,
            IRItemRegistry.MODULAR_ARMOR_CHEST,
            IRItemRegistry.MODULAR_ARMOR_LEGGINGS,
            IRItemRegistry.MODULAR_ARMOR_BOOTS,
            IRItemRegistry.PORTABLE_CHARGER_ITEM,
            IRItemRegistry.BATTERY
        )

        entryRegistry?.registerEntriesAfter(EntryStack.create(IRItemRegistry.GAMER_AXE_ITEM),
            EntryStack.create(ItemStack(IRItemRegistry.GAMER_AXE_ITEM).also {
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
                EntryStack.create(MachineRegistry.SOLID_INFUSER_REGISTRY.block(Tier.MK1)),
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

        recipeHelper?.registerCategory(
            IRModuleCraftingRecipeCategory(
                MODULE,
                EntryStack.create(MachineRegistry.MODULAR_WORKBENCH_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.module"
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
        recipeHelper?.registerRecipes(MODULE, ModuleRecipe::class.java) {
            IRMachinePlugin(it, MODULE)
        }
    }

    override fun registerOthers(recipeHelper: RecipeHelper?) {
        MachineRegistry.PULVERIZER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                PULVERIZING,
                EntryStack.create(block)
            )
        }
        MachineRegistry.SOLID_INFUSER_REGISTRY.forEachBlock { _, block ->
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
        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                MODULE,
                EntryStack.create(block)
            )
        }

        MachineRegistry.MAP.entries.distinctBy { (_, v) -> v }.forEach { (_, registry) ->
            if (registry.upgradeable && registry.tiers.size > 1) {
                registry.forEachBlock { tier, block ->
                    val entryStack = EntryStack.create(block)
                    if (tier != Tier.CREATIVE && tier != Tier.MK1 && recipeHelper?.getRecipesFor(entryStack)?.isEmpty() == true) {
                        val info = DefaultInformationDisplay.createFromEntry(entryStack, TranslatableText(block.translationKey))
                        info.lines(TranslatableText("indrev.category.rei.upgrading",
                            TranslatableText("item.indrev.tier_upgrade_" + tier.toString()
                                .lowercase(Locale.getDefault())).formatted(Formatting.DARK_GRAY),
                            TranslatableText(registry.block(registry.tiers[registry.tiers.indexOf(tier) - 1]).translationKey).formatted(Formatting.DARK_GRAY),
                            tier.toString()))
                        recipeHelper.registerDisplay(info)
                    }
                }
            }
        }

        val recipeHelpers = mutableMapOf<Block, DefaultInformationDisplay>()
        VeinType.REGISTERED.forEach { (id, type) ->
            val sum = type.outputs.entries.sumOf<T>({ it.weight })
            type.outputs.entries.forEach { entry ->
                val block = entry.element
                val info = recipeHelpers.computeIfAbsent(block) {
                    val info = DefaultInformationDisplay.createFromEntry(EntryStack.create(block), TranslatableText(block.translationKey))
                    info.line(LiteralText("This can be mined by Industrial Revolution's miner in the following veins:"))
                    info.line(LiteralText.EMPTY)
                    info
                }
                val chance = entry.weight * 100 / sum.toDouble()
                val chanceString = ((chance * 100.0).roundToInt() / 100.0).toString()
                info.line(TranslatableText("vein.${id.namespace}.${id.path}").append(" (").append(LiteralText(chanceString)).append("%)"))
            }
        }
        recipeHelpers.forEach { (_, info) -> recipeHelper?.registerDisplay(info) }
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
    private val MODULE = identifier("plugins/module")
}