package me.steven.indrev.items.rechargeable

import me.steven.indrev.utils.getShortEnergyDisplay
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergyTier

open class IRRechargeableItem(settings: Settings, private val maxStored: Double, val canOutput: Boolean = false) : Item(settings), EnergyHolder {

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip?.add(TranslatableText("gui.widget.energy"))
        val handler = Energy.of(stack)
        tooltip?.add(LiteralText("${getShortEnergyDisplay(handler.energy)} / ${getShortEnergyDisplay(handler.maxStored)} LF"))
        tooltip?.add(TranslatableText("item.indrev.rechargeable.tooltip").formatted(Formatting.ITALIC, Formatting.GRAY))
    }

    override fun canRepair(stack: ItemStack?, ingredient: ItemStack?): Boolean = false

    override fun getMaxStoredPower(): Double = maxStored

    override fun getTier(): EnergyTier = EnergyTier.LOW
}