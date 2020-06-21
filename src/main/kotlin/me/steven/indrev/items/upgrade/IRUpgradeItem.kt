package me.steven.indrev.items.upgrade

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRUpgradeItem(settings: Settings, val upgrade: Upgrade) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.${upgrade.toString().toLowerCase()}_upgrade.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC))
        super.appendTooltip(stack, world, tooltip, context)
    }
}