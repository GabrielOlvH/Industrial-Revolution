package me.steven.indrev.gui.tooltip.modular

import me.steven.indrev.gui.tooltip.CustomTooltipData
import me.steven.indrev.gui.tooltip.CustomTooltipDataProvider
import me.steven.indrev.tools.modular.IRModularItem
import net.minecraft.item.ItemStack

interface ModularTooltipDataProvider : CustomTooltipDataProvider {
    override fun getData(stack: ItemStack): List<CustomTooltipData> {
        val item = stack.item as? IRModularItem<*> ?: return emptyList()
        val modules = item.getInstalled(stack)
        return listOf(ModularTooltipData(modules) { it.getLevel(stack) })
    }
}