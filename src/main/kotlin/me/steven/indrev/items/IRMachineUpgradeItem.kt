package me.steven.indrev.items

import me.steven.indrev.utils.Tier
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRMachineUpgradeItem(settings: Settings, val from: Tier, val to: Tier) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.tier_upgrade_${to.toString().toLowerCase()}.tooltip").formatted(Formatting.ITALIC, Formatting.BLUE))
        super.appendTooltip(stack, world, tooltip, context)
    }
}