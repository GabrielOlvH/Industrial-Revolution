package me.steven.indrev.gui.tooltip.energy

import me.steven.indrev.gui.tooltip.CustomTooltipData
import me.steven.indrev.gui.tooltip.CustomTooltipDataProvider
import me.steven.indrev.utils.energyOf
import net.minecraft.item.ItemStack

interface EnergyTooltipDataProvider : CustomTooltipDataProvider {
    override fun getData(stack: ItemStack): List<CustomTooltipData> {
        val handler = energyOf(stack) ?: return emptyList()
        return listOf(EnergyTooltipData(handler.amount, handler.capacity))
    }
}