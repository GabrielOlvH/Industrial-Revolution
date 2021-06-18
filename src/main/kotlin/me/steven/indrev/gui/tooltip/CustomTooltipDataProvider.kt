package me.steven.indrev.gui.tooltip

import net.minecraft.item.ItemStack

interface CustomTooltipDataProvider {
    fun getData(stack: ItemStack): List<CustomTooltipData>
}