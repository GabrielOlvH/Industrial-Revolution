package me.steven.indrev.compat.rei

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.compat.rei.categories.IRMachineRecipeCategory
import me.steven.indrev.compat.rei.categories.IRModuleCraftingRecipeCategory
import me.steven.indrev.compat.rei.categories.IRSawmillRecipeCategory
import me.steven.indrev.compat.rei.plugins.IRMachinePlugin
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import me.steven.indrev.world.chunkveins.VeinType
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.AbstractCookingRecipe
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import java.util.*
import kotlin.math.roundToInt

object REIPlugin : REIClientPlugin {

    override fun registerEntries(entryRegistry: EntryRegistry?) {
        fun registerCharged(vararg items: Item) {
            items.forEach { item ->
                entryRegistry?.addEntriesAfter(EntryStacks.of(item),
                    EntryStacks.of(ItemStack(item).also { it.orCreateNbt.putLong("energy", energyOf(it)!!.capacity) }))
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

        entryRegistry?.addEntriesAfter(EntryStacks.of(IRItemRegistry.GAMER_AXE_ITEM),
            EntryStacks.of(ItemStack(IRItemRegistry.GAMER_AXE_ITEM).also {
                val tag = it.orCreateNbt
                tag.putLong("energy", energyOf(it)!!.capacity)
                tag.putBoolean("Active", true)
                tag.putFloat("Progress", 1f)
            }))

        entryRegistry?.removeEntryIf { e -> hide(e.identifier!!) }
    }

    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(
            IRMachineRecipeCategory(
                PulverizerRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.pulverizing"
            )
        )

        registry.add(
            IRMachineRecipeCategory(
                InfuserRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.SOLID_INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.infusing"
            )
        )

        registry.add(
            IRMachineRecipeCategory(
                CompressorRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.COMPRESSOR_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.compressing"
            )
        )

        registry.add(
            IRMachineRecipeCategory(
                FluidInfuserRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.FLUID_INFUSER_REGISTRY.block(Tier.MK1)),
                "indrev.category.rei.fluid_infusing"
            )
        )

        registry.add(
            IRMachineRecipeCategory(
                CondenserRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.CONDENSER_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.condensing"
            )
        )

        registry.add(
            IRMachineRecipeCategory(
                SmelterRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.SMELTER_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.smelting"
            )
        )

        registry.add(
            IRSawmillRecipeCategory(
                SawmillRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.SAWMILL_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.sawmill"
            )
        )

        registry.add(
            IRModuleCraftingRecipeCategory(
                ModuleRecipe.IDENTIFIER,
                EntryStacks.of(MachineRegistry.MODULAR_WORKBENCH_REGISTRY.block(Tier.MK4)),
                "indrev.category.rei.module"
            )
        )
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        registry.recipeManager.getRecipes().keys.forEach { type ->
            if (type is IRRecipeType<*> && type.id.namespace == IndustrialRevolution.MOD_ID)
                registry.registerFiller(IRRecipe::class.java, { r -> r is IRRecipe && r !is AbstractCookingRecipe && r.type == type }) { recipe -> IRMachinePlugin(recipe) }
        }

        MachineRegistry.MAP.entries.distinctBy { (_, v) -> v }.forEach { (_, machineRegistry) ->
            if (machineRegistry.upgradeable && machineRegistry.tiers.size > 1) {
                machineRegistry.forEachBlock { tier, block ->
                    val entryStack = EntryStacks.of(block)
                    if (tier != Tier.CREATIVE && tier != Tier.MK1) {
                        val info = DefaultInformationDisplay.createFromEntry(entryStack, TranslatableText(block.translationKey))
                        info.lines(TranslatableText("indrev.category.rei.upgrading",
                            TranslatableText("item.indrev.tier_upgrade_" + tier.toString()
                                .lowercase(Locale.getDefault())).formatted(Formatting.DARK_GRAY),
                            TranslatableText(machineRegistry.block(machineRegistry.tiers[machineRegistry.tiers.indexOf(tier) - 1]).translationKey).formatted(Formatting.DARK_GRAY),
                            tier.toString()))
                        registry.add(info)
                    }
                }
            }
        }

        val recipeHelpers = mutableMapOf<Block, DefaultInformationDisplay>()
        VeinType.REGISTERED.forEach { (id, type) ->
            val sum = type.outputs.entries.sumOf { it.weight }
            type.outputs.entries.forEach { entry ->
                val block = entry.element
                val info = recipeHelpers.computeIfAbsent(block) {
                    val info = DefaultInformationDisplay.createFromEntry(EntryStacks.of(block), TranslatableText(block.translationKey))
                    info.line(LiteralText("This can be mined by Industrial Revolution's miner in the following veins:"))
                    info.line(LiteralText.EMPTY)
                    info
                }
                val chance = entry.weight * 100 / sum.toDouble()
                val chanceString = ((chance * 100.0).roundToInt() / 100.0).toString()
                info.line(TranslatableText("vein.${id.namespace}.${id.path}").append(" (").append(LiteralText(chanceString)).append("%)"))
            }
        }
        recipeHelpers.forEach { (_, info) -> registry.add(info) }
    }

    /*override fun registerOthers(recipeHelper: RecipeHelper?) {
        MachineRegistry.PULVERIZER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                PULVERIZING,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.SOLID_INFUSER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                INFUSING,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.COMPRESSOR_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                COMPRESSING,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.RECYCLER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                RECYCLING,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.FLUID_INFUSER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                FLUID_INFUSER,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.CONDENSER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                CONDENSER,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.SMELTER_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                SMELTER,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.SAWMILL_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                SAWMILL,
                EntryStacks.of(block)
            )
        }
        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.forEachBlock { _, block ->
            recipeHelper?.registerWorkingStations(
                MODULE,
                EntryStacks.of(block)
            )
        }
    }*/
}