package me.steven.indrev.items.misc

import me.steven.indrev.api.OreDataCards
import me.steven.indrev.gui.tooltip.oredatacards.OreDataCardTooltipData
import me.steven.indrev.utils.itemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.item.TooltipData
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World
import java.util.*
import kotlin.math.roundToInt

class OreDataCardItem : Item(itemSettings()) {
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        val data = OreDataCards.readNbt(stack) ?: return
        if (!data.isValid()) {
            tooltip.add(LiteralText("Invalid data card!").formatted(Formatting.RED))
        }
    }

    override fun getName(stack: ItemStack): Text {
        return if (OreDataCards.readNbt(stack) == null) TranslatableText("item.indrev.empty_ore_data_card")
        else super.getName(stack)
    }

    override fun isItemBarVisible(stack: ItemStack): Boolean {
        return OreDataCards.readNbt(stack) != null
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        val data = OreDataCards.readNbt(stack) ?: return -1
        return if (!data.isValid()) return 0xff0000 else 0xffffff
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val data = OreDataCards.readNbt(stack) ?: return 0
        if (!data.isValid()) return 13
        return 13-((OreDataCards.MAX_SIZE - (data.maxCycles - data.used)) * 13.0f / OreDataCards.MAX_SIZE.toDouble()).roundToInt()
    }

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val data = OreDataCards.readNbt(stack)
        if (data?.isValid() != true) return Optional.empty()
        return Optional.of(OreDataCardTooltipData(data))
    }
}