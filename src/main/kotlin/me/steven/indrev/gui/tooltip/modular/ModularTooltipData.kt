package me.steven.indrev.gui.tooltip.modular

import me.steven.indrev.gui.tooltip.CustomTooltipData
import me.steven.indrev.tools.modular.Module
import net.minecraft.client.gui.tooltip.TooltipComponent

class ModularTooltipData(val modules: List<Module>, val levelProvider: (Module) -> Int) : CustomTooltipData {
    override fun toComponent(): TooltipComponent = ModularTooltipComponent(this)
}